package com.seffafbagis.api.service.application;

import com.seffafbagis.api.dto.request.application.DocumentRequest;
import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.entity.application.ApplicationDocument;
import com.seffafbagis.api.repository.ApplicationDocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentServiceTest {

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @InjectMocks
    private ApplicationDocumentService documentService;

    @Test
    void addDocument_Success() {
        Application application = new Application();
        DocumentRequest request = new DocumentRequest();
        request.setDocumentType("id_card");
        request.setFileName("id.pdf");
        request.setFileUrl("http://example.com/id.pdf");

        when(documentRepository.save(any(ApplicationDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationDocument result = documentService.addDocument(application, request);

        assertNotNull(result);
        assertEquals("id.pdf", result.getFileName());
        assertEquals("http://example.com/id.pdf", result.getFileUrl());
        assertEquals(application, result.getApplication());
        assertFalse(result.getIsVerified());
    }

    @Test
    void verifyDocument_Success() {
        UUID docId = UUID.randomUUID();
        ApplicationDocument document = new ApplicationDocument();
        document.setId(docId);
        document.setIsVerified(false);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(ApplicationDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        documentService.verifyDocument(docId);

        assertTrue(document.getIsVerified());
        verify(documentRepository).save(document);
    }

    @Test
    void removeDocument_Success() {
        UUID docId = UUID.randomUUID();
        when(documentRepository.existsById(docId)).thenReturn(true);

        documentService.removeDocument(docId);

        verify(documentRepository).deleteById(docId);
    }

    @Test
    void removeDocument_Fail_WhenNotFound() {
        UUID docId = UUID.randomUUID();
        when(documentRepository.existsById(docId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> documentService.removeDocument(docId));
    }
}
