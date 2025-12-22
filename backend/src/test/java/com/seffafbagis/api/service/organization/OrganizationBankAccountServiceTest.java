package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.organization.AddBankAccountRequest;
import com.seffafbagis.api.dto.request.organization.UpdateBankAccountRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationBankAccountResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ConflictException;

import com.seffafbagis.api.repository.OrganizationBankAccountRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationBankAccountServiceTest {

    @Mock
    private OrganizationBankAccountRepository bankAccountRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper mapper;

    @InjectMocks
    private OrganizationBankAccountService bankAccountService;

    private User user;
    private Organization organization;
    private OrganizationBankAccount bankAccount;
    // Valid TR IBAN
    private static final String VALID_IBAN = "TR670004600000000000012345";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@org.com");
        user.setRole(UserRole.FOUNDATION);

        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setUser(user);

        bankAccount = new OrganizationBankAccount();
        bankAccount.setId(UUID.randomUUID());
        bankAccount.setOrganization(organization);
        bankAccount.setIban(VALID_IBAN);
        bankAccount.setPrimary(false);

        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getBankAccounts_ShouldReturnList() {
        when(bankAccountRepository.findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(organization.getId()))
                .thenReturn(List.of(bankAccount));
        when(mapper.toBankAccountResponseList(any())).thenReturn(List.of(new OrganizationBankAccountResponse()));

        List<OrganizationBankAccountResponse> result = bankAccountService.getBankAccounts(organization.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void addBankAccount_ValidRequest_ShouldCreate() {
        AddBankAccountRequest request = new AddBankAccountRequest();
        request.setIban(VALID_IBAN);
        request.setAccountHolder("Test Foundation");

        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(bankAccountRepository.existsByIban(VALID_IBAN)).thenReturn(false);
        when(mapper.toEntity(any(AddBankAccountRequest.class), eq(organization))).thenReturn(bankAccount);
        when(bankAccountRepository.countByOrganizationId(organization.getId())).thenReturn(1L);
        when(bankAccountRepository.save(any(OrganizationBankAccount.class))).thenReturn(bankAccount);
        when(mapper.toResponse(any(OrganizationBankAccount.class))).thenReturn(new OrganizationBankAccountResponse());

        bankAccountService.addBankAccount(request);

        verify(bankAccountRepository).save(any(OrganizationBankAccount.class));
    }

    @Test
    void addBankAccount_DuplicateIban_ShouldThrowConflict() {
        AddBankAccountRequest request = new AddBankAccountRequest();
        request.setIban(VALID_IBAN);

        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(bankAccountRepository.existsByIban(VALID_IBAN)).thenReturn(true);

        assertThrows(ConflictException.class, () -> bankAccountService.addBankAccount(request));
    }

    @Test
    void updateBankAccount_UpdateIban_ShouldResetVerification() {
        UpdateBankAccountRequest request = new UpdateBankAccountRequest();
        // Use VALID_IBAN but slightly modified to still be valid?
        // Or just another valid IBAN: TR670004600000000000012345
        // Needs a different one.
        // TR000004600000000000012345 -> T=29 R=27 000004600000000000012345292700
        // I'll assume current IBAN is DIFFERENT from valid one to test update.
        // Let's change bankAccount iban to something else first.
        String OLD_IBAN = "TR880004600000000000012345"; // Just a dummy string for old one, doesn't matter if invalid
                                                        // validation checks specifically checking NEW iban.
        // Actually validation checks normalization.
        // I'll just use request IBAN as VALID_IBAN and ensure bankAccount has OLD_IBAN.

        bankAccount.setIban(OLD_IBAN);
        bankAccount.setVerified(true);
        request.setIban(VALID_IBAN);

        when(bankAccountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(bankAccountRepository.existsByIbanAndIdNot(VALID_IBAN, bankAccount.getId())).thenReturn(false);
        when(bankAccountRepository.save(any(OrganizationBankAccount.class))).thenReturn(bankAccount);
        doAnswer(i -> null).when(mapper).updateEntity(any(OrganizationBankAccount.class),
                any(UpdateBankAccountRequest.class));
        when(mapper.toResponse(any(OrganizationBankAccount.class))).thenReturn(new OrganizationBankAccountResponse());

        bankAccountService.updateBankAccount(bankAccount.getId(), request);

        // Assert verification reset
        assertFalse(bankAccount.getVerified());
        assertEquals(VALID_IBAN, bankAccount.getIban());
    }

    @Test
    void deleteBankAccount_LastAccount_ShouldThrowBadRequest() {
        when(bankAccountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(bankAccountRepository.countByOrganizationId(organization.getId())).thenReturn(1L);

        assertThrows(BadRequestException.class, () -> bankAccountService.deleteBankAccount(bankAccount.getId()));
    }

    @Test
    void deleteBankAccount_PrimaryAccount_ShouldThrowBadRequest() {
        bankAccount.setPrimary(true);
        when(bankAccountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(bankAccountRepository.countByOrganizationId(organization.getId())).thenReturn(2L);

        assertThrows(BadRequestException.class, () -> bankAccountService.deleteBankAccount(bankAccount.getId()));
    }

    @Test
    void deleteBankAccount_Success() {
        when(bankAccountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(bankAccountRepository.countByOrganizationId(organization.getId())).thenReturn(2L);

        bankAccountService.deleteBankAccount(bankAccount.getId());

        verify(bankAccountRepository).delete(bankAccount);
    }
}
