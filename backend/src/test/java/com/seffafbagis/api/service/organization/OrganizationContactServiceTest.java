package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.organization.AddContactRequest;
import com.seffafbagis.api.dto.request.organization.UpdateContactRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationContactResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationContact;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.OrganizationContactRepository;
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
class OrganizationContactServiceTest {

    @Mock
    private OrganizationContactRepository contactRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper mapper;

    @InjectMocks
    private OrganizationContactService contactService;

    private User user;
    private Organization organization;
    private OrganizationContact contact;

    @BeforeEach
    void setUp() {
        // Setup User
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@org.com");
        user.setRole(UserRole.FOUNDATION);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        // Setup Organization
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setUser(user);

        // Setup Contact
        contact = new OrganizationContact();
        contact.setId(UUID.randomUUID());
        contact.setOrganization(organization);
        contact.setContactName("John Doe");
        contact.setPrimary(false);

        // Setup Security Context
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getContacts_ShouldReturnList() {
        when(contactRepository.findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(organization.getId()))
                .thenReturn(List.of(contact));
        when(mapper.toContactResponseList(any())).thenReturn(List.of(new OrganizationContactResponse()));

        List<OrganizationContactResponse> result = contactService.getContacts(organization.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(contactRepository).findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(organization.getId());
    }

    @Test
    void addContact_WhenFirstContact_ShouldSetPrimary() {
        AddContactRequest request = new AddContactRequest();
        request.setContactName("Jane Doe");

        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(mapper.toEntity(any(AddContactRequest.class), eq(organization))).thenReturn(contact);
        when(contactRepository.countByOrganizationId(organization.getId())).thenReturn(0L);
        when(contactRepository.save(any(OrganizationContact.class))).thenReturn(contact);
        when(mapper.toResponse(any(OrganizationContact.class))).thenReturn(new OrganizationContactResponse());

        contactService.addContact(request);

        assertTrue(contact.getPrimary());
        verify(contactRepository).save(contact);
    }

    @Test
    void addContact_WhenExplicitPrimary_ShouldUnsetPrevious() {
        AddContactRequest request = new AddContactRequest();
        request.setIsPrimary(true);

        OrganizationContact existingPrimary = new OrganizationContact();
        existingPrimary.setPrimary(true);

        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(mapper.toEntity(any(AddContactRequest.class), eq(organization))).thenReturn(contact);
        when(contactRepository.countByOrganizationId(organization.getId())).thenReturn(1L);
        when(contactRepository.findByOrganizationIdAndIsPrimaryTrue(organization.getId()))
                .thenReturn(Optional.of(existingPrimary));
        when(contactRepository.save(any(OrganizationContact.class))).thenReturn(contact);
        when(mapper.toResponse(any(OrganizationContact.class))).thenReturn(new OrganizationContactResponse());

        contactService.addContact(request);

        assertFalse(existingPrimary.getPrimary());
        verify(contactRepository, times(2)).save(any(OrganizationContact.class)); // 1 for old primary, 1 for new
    }

    @Test
    void updateContact_WhenNotOwner_ShouldThrowException() {
        Organization otherOrg = new Organization();
        otherOrg.setId(UUID.randomUUID());
        contact.setOrganization(otherOrg);

        UpdateContactRequest request = new UpdateContactRequest();

        when(contactRepository.findById(contact.getId())).thenReturn(Optional.of(contact));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));

        assertThrows(ResourceNotFoundException.class, () -> contactService.updateContact(contact.getId(), request));
    }

    @Test
    void updateContact_WhenSettingPrimary_ShouldUnsetOthers() {
        UpdateContactRequest request = new UpdateContactRequest();
        request.setIsPrimary(true);

        OrganizationContact otherContact = new OrganizationContact();
        otherContact.setId(UUID.randomUUID());
        otherContact.setPrimary(true);

        when(contactRepository.findById(contact.getId())).thenReturn(Optional.of(contact));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        doAnswer(invocation -> {
            OrganizationContact c = invocation.getArgument(0);
            UpdateContactRequest r = invocation.getArgument(1);
            // Simulate mapper update
            return null;
        }).when(mapper).updateEntity(any(OrganizationContact.class), any(UpdateContactRequest.class));

        when(contactRepository.findByOrganizationIdAndIsPrimaryTrue(organization.getId()))
                .thenReturn(Optional.of(otherContact));
        when(contactRepository.save(any(OrganizationContact.class))).thenReturn(contact);
        when(mapper.toResponse(any(OrganizationContact.class))).thenReturn(new OrganizationContactResponse());

        contactService.updateContact(contact.getId(), request);

        assertFalse(otherContact.getPrimary());
        assertTrue(contact.getPrimary());
        verify(contactRepository, times(2)).save(any());
    }

    @Test
    void deleteContact_WhenLastContact_ShouldThrowException() {
        when(contactRepository.findById(contact.getId())).thenReturn(Optional.of(contact));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(contactRepository.countByOrganizationId(organization.getId())).thenReturn(1L);

        assertThrows(BadRequestException.class, () -> contactService.deleteContact(contact.getId()));
    }

    @Test
    void deleteContact_WhenPrimaryContact_ShouldThrowException() {
        contact.setPrimary(true);
        when(contactRepository.findById(contact.getId())).thenReturn(Optional.of(contact));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(contactRepository.countByOrganizationId(organization.getId())).thenReturn(2L);

        assertThrows(BadRequestException.class, () -> contactService.deleteContact(contact.getId()));
    }

    @Test
    void deleteContact_Success() {
        when(contactRepository.findById(contact.getId())).thenReturn(Optional.of(contact));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(contactRepository.countByOrganizationId(organization.getId())).thenReturn(2L);

        contactService.deleteContact(contact.getId());

        verify(contactRepository).delete(contact);
    }
}
