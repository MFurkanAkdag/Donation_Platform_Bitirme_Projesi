package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.organization.CreateOrganizationRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.VerificationStatus;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.audit.AuditLogService;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

        @Mock
        private OrganizationRepository organizationRepository;

        @Mock
        private OrganizationMapper organizationMapper;

        @Mock
        private UserRepository userRepository;

        @Mock
        private AuditLogService auditLogService;

        @InjectMocks
        private OrganizationService organizationService;

        private User currentUser;
        private Organization organization;

        @BeforeEach
        void setUp() {
                // Setup User
                currentUser = new User();
                currentUser.setId(UUID.randomUUID());
                currentUser.setEmail("test@foundation.com");
                currentUser.setRole(UserRole.FOUNDATION);

                // Setup Organization
                organization = new Organization();
                organization.setId(UUID.randomUUID());
                organization.setUser(currentUser);
                organization.setLegalName("Test Foundation");
                organization.setVerificationStatus(VerificationStatus.PENDING);

                // Setup Security Context
                CustomUserDetails userDetails = CustomUserDetails.fromUser(currentUser);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);
        }

        @Test
        void createOrganization_WhenUserIsFoundation_ShouldCreateOrganization() {
                // Arrange
                CreateOrganizationRequest request = new CreateOrganizationRequest();
                request.setLegalName("Test Foundation");
                request.setOrganizationType(OrganizationType.FOUNDATION);
                request.setTaxNumber("1234567890");

                when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
                when(organizationRepository.findByUserId(currentUser.getId())).thenReturn(Optional.empty());
                when(organizationRepository.existsByTaxNumber(request.getTaxNumber())).thenReturn(false);
                when(organizationMapper.toEntity(any(CreateOrganizationRequest.class), any(User.class)))
                                .thenReturn(organization);
                when(organizationRepository.save(any(Organization.class))).thenReturn(organization);

                OrganizationResponse responseDTO = OrganizationResponse.builder()
                                .id(organization.getId())
                                .legalName("Test Foundation")
                                .build();
                when(organizationMapper.toResponse(organization)).thenReturn(responseDTO);

                // Act
                OrganizationResponse result = organizationService.createOrganization(request);

                // Assert
                assertNotNull(result);
                assertEquals("Test Foundation", result.getLegalName());
                verify(organizationRepository).save(any(Organization.class));
                verify(auditLogService).logAction(eq(currentUser.getId()), eq("CREATE_ORGANIZATION"), anyString(),
                                anyString());
        }

        @Test
        void submitForVerification_WhenValid_ShouldUpdateStatus() {
                // Arrange
                organization.setVerificationStatus(VerificationStatus.PENDING);
                // Mock contacts and bank accounts exist by adding to list (if helper method
                // checks list size)
                // Since my validateCanSubmit uses org.getContacts(), I need to ensure they are
                // not empty locally if possible,
                // or just mock the helper method logic if I could (private method).
                // Since I can't mock private method easily, I should populate the organization
                // object.
                organization.setContacts(
                                java.util.List.of(new com.seffafbagis.api.entity.organization.OrganizationContact()));
                organization.setBankAccounts(
                                java.util.List.of(
                                                new com.seffafbagis.api.entity.organization.OrganizationBankAccount()));
                organization
                                .setDocuments(java.util.List.of(
                                                new com.seffafbagis.api.entity.organization.OrganizationDocument()));

                when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
                when(organizationRepository.findByUserId(currentUser.getId())).thenReturn(Optional.of(organization));
                when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArguments()[0]);

                OrganizationResponse responseDTO = OrganizationResponse.builder()
                                .verificationStatus(VerificationStatus.IN_REVIEW).build();
                when(organizationMapper.toResponse(any(Organization.class))).thenReturn(responseDTO);

                // Act
                OrganizationResponse result = organizationService.submitForVerification();

                // Assert
                assertEquals(VerificationStatus.IN_REVIEW, result.getVerificationStatus());
                verify(organizationRepository).save(organization);
        }
}
