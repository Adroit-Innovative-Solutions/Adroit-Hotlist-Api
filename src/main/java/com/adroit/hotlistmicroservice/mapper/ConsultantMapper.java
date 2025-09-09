package com.adroit.hotlistmicroservice.mapper;


import com.adroit.hotlistmicroservice.dto.ConsultantAddedResponse;
import com.adroit.hotlistmicroservice.dto.ConsultantDto;
import com.adroit.hotlistmicroservice.dto.DeleteConsultantResponse;
import com.adroit.hotlistmicroservice.model.Consultant;
import org.mapstruct.*;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConsultantMapper {

    @Mapping(source = "isAssignAll", target = "isAssignAll")
    @Mapping(source = "isDeleted",target = "isDeleted")
    @Mapping(source = "movedToHotlist",target = "movedToHotlist")
    ConsultantDto toDTO(Consultant consultant);

    @Mapping(source = "isAssignAll", target = "isAssignAll")
    @Mapping(source="isDeleted",target="isDeleted")
    @Mapping(source = "movedToHotlist",target="movedToHotlist")
    Consultant toEntity(ConsultantDto dto);
    @Mapping(source = "movedToHotlist",target = "movedToHotlist")
    ConsultantAddedResponse toConsultantAddedResponse(Consultant consultant);

    DeleteConsultantResponse toDeleteConsultantResponse(Consultant consultant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateConsultantFromDto(ConsultantDto dto, @MappingTarget Consultant consultant);
}
