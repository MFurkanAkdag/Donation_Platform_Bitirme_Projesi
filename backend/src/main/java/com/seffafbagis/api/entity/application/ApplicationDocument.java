package com.seffafbagis.api.entity.application;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "application_documents")
@Getter
@Setter
public class ApplicationDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt = OffsetDateTime.now();
}
