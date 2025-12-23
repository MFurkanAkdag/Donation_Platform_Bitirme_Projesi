package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.organization.AddDocumentRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationDocumentResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationDocument;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.OrganizationDocumentRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationDocumentService {

    private final OrganizationDocumentRepository documentRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationMapper mapper;

    public List<OrganizationDocumentResponse> getDocuments(UUID organizationId) {
        return mapper.toDocumentResponseList(
                documentRepository.findByOrganizationIdOrderByUploadedAtDesc(organizationId));
    }

    public OrganizationDocumentResponse addDocument(AddDocumentRequest request) {
        Organization organization = getCurrentUserOrganization();

        OrganizationDocument document = mapper.toEntity(request, organization);
        document = documentRepository.save(document);

        return mapper.toResponse(document);
    }

    public void deleteDocument(UUID documentId) {
        OrganizationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        validateOwnership(document);

        if (Boolean.TRUE.equals(document.getVerified())) {
            throw new BadRequestException("Cannot delete a verified document");
        }

        documentRepository.delete(document);
    }

    public List<OrganizationDocumentResponse> getUnverifiedDocuments(UUID organizationId) {
        return mapper.toDocumentResponseList(
                documentRepository.findByOrganizationIdAndIsVerifiedFalse(organizationId));
    }

    // Admin methods
    public void verifyDocument(UUID documentId, UUID adminUserId) {
        OrganizationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        document.setVerified(true);
        document.setVerifiedAt(LocalDateTime.now());
        document.setVerifiedBy(admin);

        documentRepository.save(document);
    }

    // Helper
    private Organization getCurrentUserOrganization() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have an organization"));
    }

    private void validateOwnership(OrganizationDocument document) {
        Organization org = getCurrentUserOrganization();
        if (!document.getOrganization().getId().equals(org.getId())) {
            throw new ResourceNotFoundException("Document not found");
        }
    }
}
