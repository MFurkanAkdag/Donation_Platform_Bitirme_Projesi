package com.seffafbagis.api.service.application;

import com.seffafbagis.api.dto.request.application.DocumentRequest;
import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.entity.application.ApplicationDocument;
import com.seffafbagis.api.repository.ApplicationDocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationDocumentService {

    private final ApplicationDocumentRepository documentRepository;

    @Transactional
    public ApplicationDocument addDocument(Application application, DocumentRequest request) {
        ApplicationDocument document = new ApplicationDocument();
        document.setApplication(application);
        document.setDocumentType(request.getDocumentType());
        document.setFileName(request.getFileName());
        document.setFileUrl(request.getFileUrl());
        document.setUploadedAt(OffsetDateTime.now());
        document.setIsVerified(false);

        return documentRepository.save(document);
    }

    @Transactional
    public void removeDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new EntityNotFoundException("Document not found");
        }
        documentRepository.deleteById(documentId);
    }

    @Transactional
    public void verifyDocument(UUID documentId) {
        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));
        document.setIsVerified(true);
        documentRepository.save(document);
    }

    public List<ApplicationDocument> getDocumentsByApplicationId(UUID applicationId) {
        return documentRepository.findByApplicationId(applicationId);
    }
}
