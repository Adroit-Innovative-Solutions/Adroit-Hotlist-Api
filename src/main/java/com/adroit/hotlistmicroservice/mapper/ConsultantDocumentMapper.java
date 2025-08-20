package com.adroit.hotlistmicroservice.mapper;

import com.adroit.hotlistmicroservice.dto.DocumentDetailsDTO;
import com.adroit.hotlistmicroservice.model.ConsultantDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConsultantDocumentMapper {

    DocumentDetailsDTO toDocumentDTO(ConsultantDocument consultantDocument);
}
