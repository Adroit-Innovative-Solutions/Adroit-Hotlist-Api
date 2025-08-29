package com.adroit.hotlistmicroservice.mapper;


import com.adroit.hotlistmicroservice.dto.ConsultantAddedResponse;
import com.adroit.hotlistmicroservice.dto.ConsultantDto;
import com.adroit.hotlistmicroservice.dto.DeleteConsultantResponse;
import com.adroit.hotlistmicroservice.model.Consultant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConsultantMapper {

    @Mapping(source = "isAssignAll", target = "isAssignAll")
    @Mapping(source = "isDeleted",target = "isDeleted")
    ConsultantDto toDTO(Consultant consultant);

    @Mapping(source = "isAssignAll", target = "isAssignAll")
    @Mapping(source="isDeleted",target="isDeleted")
    Consultant toEntity(ConsultantDto dto);

    ConsultantAddedResponse toConsultantAddedResponse(Consultant consultant);

    DeleteConsultantResponse toDeleteConsultantResponse(Consultant consultant);
}
