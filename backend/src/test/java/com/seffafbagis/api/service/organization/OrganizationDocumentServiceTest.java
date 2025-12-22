package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.organization.AddDocumentRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationDocumentResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationDocument;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.OrganizationDocumentRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationDocumentServiceTest {

    @Mock
    private OrganizationDocumentRepository documentRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationMapper mapper;

    @InjectMocks
    private OrganizationDocumentService documentService;

    private User user;
    private Organization organization;
    private OrganizationDocument document;

    @BeforeEach
    void setUp() {
        // Setup User
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@org.com");
        user.setRole(UserRole.FOUNDATION);

        // Setup Organization
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setUser(user);

        // Setup Document
        document = new OrganizationDocument();
        document.setId(UUID.randomUUID());
        document.setOrganization(organization);
        document.setVerified(false);

        // Setup Security Context
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getDocuments_ShouldReturnList() {
        when(documentRepository.findByOrganizationIdOrderByUploadedAtDesc(organization.getId()))
                .thenReturn(List.of(document));
        when(mapper.toDocumentResponseList(any())).thenReturn(List.of(new OrganizationDocumentResponse()));

        List<OrganizationDocumentResponse> result = documentService.getDocuments(organization.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void addDocument_ShouldCreate() {
        AddDocumentRequest request = new AddDocumentRequest();
        request.setDocumentType("TAX_PLATE");
        request.setFileUrl("https://example.com/file.pdf");

        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));
        when(mapper.toEntity(any(AddDocumentRequest.class), eq(organization))).thenReturn(document);
        when(documentRepository.save(any(OrganizationDocument.class))).thenReturn(document);
        when(mapper.toResponse(any(OrganizationDocument.class))).thenReturn(new OrganizationDocumentResponse());

        documentService.addDocument(request);

        verify(documentRepository).save(any(OrganizationDocument.class));
    }

    @Test
    void deleteDocument_VerifiedDocument_ShouldThrowBadRequest() {
        document.setVerified(true);
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));

        assertThrows(BadRequestException.class, () -> documentService.deleteDocument(document.getId()));
    }

    @Test
    void deleteDocument_Success() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(organizationRepository.findByUserId(user.getId())).thenReturn(Optional.of(organization));

        documentService.deleteDocument(document.getId());

        verify(documentRepository).delete(document);
    }

    @Test
    void verifyDocument_WhenAdmin_ShouldVerify() {
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);

        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        documentService.verifyDocument(document.getId(), adminId);

        assertTrue(document.getVerified());
        assertNotNull(document.getVerifiedAt());
        assertEquals(admin, document.getVerifiedBy());
        verify(documentRepository).save(document);
    }
}
