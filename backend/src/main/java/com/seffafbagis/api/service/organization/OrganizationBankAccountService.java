package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.organization.AddBankAccountRequest;
import com.seffafbagis.api.dto.request.organization.UpdateBankAccountRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationBankAccountResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ConflictException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.OrganizationBankAccountRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.validator.IbanValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationBankAccountService {

    private final OrganizationBankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper mapper;

    public List<OrganizationBankAccountResponse> getBankAccounts(UUID organizationId) {
        return mapper.toBankAccountResponseList(
                bankAccountRepository.findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(organizationId));
    }

    public OrganizationBankAccountResponse addBankAccount(AddBankAccountRequest request) {
        Organization organization = getCurrentUserOrganization();

        // Validate IBAN
        IbanValidator validator = new IbanValidator(request.getIban());
        validator.validateOrThrow(); // Throws BadRequestException if invalid

        String normalizedIban = validator.normalize();
        validateIbanUniqueness(normalizedIban, null);

        OrganizationBankAccount account = mapper.toEntity(request, organization);
        account.setIban(normalizedIban);

        // If first account, make primary
        if (bankAccountRepository.countByOrganizationId(organization.getId()) == 0) {
            account.setPrimary(true);
        } else if (Boolean.TRUE.equals(request.getIsPrimary())) {
            unsetPrimary(organization.getId());
        }

        account = bankAccountRepository.save(account);
        return mapper.toResponse(account);
    }

    public OrganizationBankAccountResponse updateBankAccount(UUID accountId, UpdateBankAccountRequest request) {
        OrganizationBankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        validateOwnership(account);

        if (request.getIban() != null) {
            IbanValidator validator = new IbanValidator(request.getIban());
            validator.validateOrThrow();
            String normalizedIban = validator.normalize();

            if (!normalizedIban.equals(account.getIban())) {
                validateIbanUniqueness(normalizedIban, accountId);
                account.setIban(normalizedIban);
                // Changing IBAN resets verification status usually
                account.setVerified(false);
            }
        }

        mapper.updateEntity(account, request);

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            // Unset others
            bankAccountRepository.findByOrganizationIdAndIsPrimaryTrue(account.getOrganization().getId())
                    .filter(a -> !a.getId().equals(accountId))
                    .ifPresent(primary -> {
                        primary.setPrimary(false);
                        bankAccountRepository.save(primary);
                    });
            account.setPrimary(true);
        }

        account = bankAccountRepository.save(account);
        return mapper.toResponse(account);
    }

    public void deleteBankAccount(UUID accountId) {
        OrganizationBankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        validateOwnership(account);

        long count = bankAccountRepository.countByOrganizationId(account.getOrganization().getId());
        if (count <= 1) {
            throw new BadRequestException("Cannot delete the last bank account");
        }

        if (Boolean.TRUE.equals(account.getPrimary())) {
            throw new BadRequestException(
                    "Cannot delete primary bank account. Please set another account as primary first.");
        }

        bankAccountRepository.delete(account);
    }

    public OrganizationBankAccountResponse setPrimaryBankAccount(UUID accountId) {
        OrganizationBankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        validateOwnership(account);

        if (Boolean.TRUE.equals(account.getPrimary())) {
            return mapper.toResponse(account);
        }

        unsetPrimary(account.getOrganization().getId());

        account.setPrimary(true);
        account = bankAccountRepository.save(account);
        return mapper.toResponse(account);
    }

    // Helper
    private void unsetPrimary(UUID organizationId) {
        bankAccountRepository.findByOrganizationIdAndIsPrimaryTrue(organizationId)
                .ifPresent(primary -> {
                    primary.setPrimary(false);
                    bankAccountRepository.save(primary);
                });
    }

    private Organization getCurrentUserOrganization() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have an organization"));
    }

    private void validateOwnership(OrganizationBankAccount account) {
        Organization org = getCurrentUserOrganization();
        if (!account.getOrganization().getId().equals(org.getId())) {
            throw new ResourceNotFoundException("Bank account not found");
        }
    }

    private void validateIbanUniqueness(String iban, UUID excludeId) {
        boolean exists;
        if (excludeId == null) {
            exists = bankAccountRepository.existsByIban(iban);
        } else {
            exists = bankAccountRepository.existsByIbanAndIdNot(iban, excludeId);
        }

        if (exists) {
            throw new ConflictException("OrganizationBankAccount", "iban", iban);
        }
    }
}
