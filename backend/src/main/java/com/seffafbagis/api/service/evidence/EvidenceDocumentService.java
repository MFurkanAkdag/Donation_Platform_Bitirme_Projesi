package com.seffafbagis.api.service.evidence;

import com.seffafbagis.api.dto.request.evidence.CreateEvidenceDocumentRequest;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.evidence.EvidenceDocument;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.EvidenceDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvidenceDocumentService {

    private final EvidenceDocumentRepository evidenceDocumentRepository;

    @Transactional
    public EvidenceDocument addDocument(Evidence evidence, CreateEvidenceDocumentRequest request) {
        EvidenceDocument document = new EvidenceDocument();
        if (evidence == null) {
            throw new IllegalArgumentException("Evidence cannot be null");
        }
        document.setEvidence(evidence);
        document.setFileName(request.getFileName());
        document.setFileUrl(request.getFileUrl());
        document.setFileSize(request.getFileSize());
        document.setMimeType(request.getMimeType());
        document.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);

        return evidenceDocumentRepository.save(document);
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        if (!evidenceDocumentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Evidence document not found with id: " + documentId);
        }
        evidenceDocumentRepository.deleteById(documentId);
    }

    @Transactional
    public EvidenceDocument setPrimaryDocument(UUID evidenceId, UUID documentId) {
        EvidenceDocument document = evidenceDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence document not found with id: " + documentId));

        if (!document.getEvidence().getId().equals(evidenceId)) {
            throw new IllegalArgumentException("Document does not belong to the specified evidence");
        }

        // Reset other primary documents for this evidence
        List<EvidenceDocument> documents = evidenceDocumentRepository.findByEvidenceId(evidenceId);
        for (EvidenceDocument doc : documents) {
            doc.setIsPrimary(false);
        }
        evidenceDocumentRepository.saveAll(documents);

        // Set new primary
        document.setIsPrimary(true);
        return evidenceDocumentRepository.save(document);
    }

    public List<EvidenceDocument> getDocumentsByEvidenceId(UUID evidenceId) {
        return evidenceDocumentRepository.findByEvidenceId(evidenceId);
    }
}
