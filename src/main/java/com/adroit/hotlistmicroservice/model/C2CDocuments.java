package com.adroit.hotlistmicroservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "c2c_documents")
public class C2CDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long docId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Lob
    @Column(name = "file_type", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileType;

    @Column(name = "size")
    private Long size;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    // 🔗 Relation to placement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placement_id", referencedColumnName = "placementId") // ✅ Corrected
    private PlacementDetails placementDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", referencedColumnName = "emp_id")
    private C2CEmployerDetails employer;


}
