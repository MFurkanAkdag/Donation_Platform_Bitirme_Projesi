package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.organization.AddContactRequest;
import com.seffafbagis.api.dto.request.organization.UpdateContactRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationContactResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationContact;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.OrganizationContactRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationContactService {

    private final OrganizationContactRepository contactRepository;
    private final OrganizationRepository organizationRepository; // To fetch organization
    private final OrganizationMapper mapper;

    public List<OrganizationContactResponse> getContacts(UUID organizationId) {
        // Validation: can user see these contacts?
        // Owner or Admin.
        // Assuming controller handles basic role check, but here we can check specific
        // ownership if needed.
        // For 'my/contacts' usage, organizationId usually comes from context, but if
        // passed explicitly:
        return mapper.toContactResponseList(
                contactRepository.findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(organizationId));
    }

    public OrganizationContactResponse addContact(AddContactRequest request) {
        Organization organization = getCurrentUserOrganization();

        OrganizationContact contact = mapper.toEntity(request, organization);

        // If this is the first contact, make it primary
        if (contactRepository.countByOrganizationId(organization.getId()) == 0) {
            contact.setPrimary(true);
        } else if (Boolean.TRUE.equals(request.getIsPrimary())) {
            // Unset previous primary
            contactRepository.findByOrganizationIdAndIsPrimaryTrue(organization.getId())
                    .ifPresent(primary -> {
                        primary.setPrimary(false);
                        contactRepository.save(primary);
                    });
        }

        contact = contactRepository.save(contact);
        return mapper.toResponse(contact);
    }

    public OrganizationContactResponse updateContact(UUID contactId, UpdateContactRequest request) {
        OrganizationContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        validateOwnership(contact);

        mapper.updateEntity(contact, request);

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            // Unset others
            contactRepository.findByOrganizationIdAndIsPrimaryTrue(contact.getOrganization().getId())
                    .filter(c -> !c.getId().equals(contactId))
                    .ifPresent(primary -> {
                        primary.setPrimary(false);
                        contactRepository.save(primary);
                    });
            contact.setPrimary(true);
        }

        contact = contactRepository.save(contact);
        return mapper.toResponse(contact);
    }

    public void deleteContact(UUID contactId) {
        OrganizationContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        validateOwnership(contact);

        long count = contactRepository.countByOrganizationId(contact.getOrganization().getId());
        if (count <= 1) {
            throw new BadRequestException("Cannot delete the last contact");
        }

        if (Boolean.TRUE.equals(contact.getPrimary())) {
            throw new BadRequestException(
                    "Cannot delete primary contact. Please set another contact as primary first.");
        }

        contactRepository.delete(contact);
    }

    public OrganizationContactResponse setPrimaryContact(UUID contactId) {
        OrganizationContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        validateOwnership(contact);

        if (Boolean.TRUE.equals(contact.getPrimary())) {
            return mapper.toResponse(contact);
        }

        // Unset previous
        contactRepository.findByOrganizationIdAndIsPrimaryTrue(contact.getOrganization().getId())
                .ifPresent(primary -> {
                    primary.setPrimary(false);
                    contactRepository.save(primary);
                });

        contact.setPrimary(true);
        contact = contactRepository.save(contact);

        return mapper.toResponse(contact);
    }

    // Helper
    private Organization getCurrentUserOrganization() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have an organization"));
    }

    private void validateOwnership(OrganizationContact contact) {
        Organization org = getCurrentUserOrganization();
        if (!contact.getOrganization().getId().equals(org.getId())) {
            throw new ResourceNotFoundException("Contact not found"); // Mask existence logic
        }
    }
}
