package com.adroit.hotlistmicroservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class C2CDocumentsDto {
    private Long docId;
    private String fileName;
    private Long size;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
