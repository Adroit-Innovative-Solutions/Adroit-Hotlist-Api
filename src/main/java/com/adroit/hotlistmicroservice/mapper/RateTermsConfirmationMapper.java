package com.adroit.hotlistmicroservice.mapper;

import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.model.Consultant;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RateTermsConfirmationMapper {

    RateTermsConfirmation toEntity(RateTermsConfirmationDTO dto);

    RateTermsConfirmation entityFromRequest(RateTermsConfirmationRequest request);

    RateTermsConfirmationDTO toDtoFromEntity(RateTermsConfirmation entity);

    RTRAddedResponse toRtrAddedResponse(RateTermsConfirmation rtr);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRTRFromDto(RTRUpdateDTO dto, @MappingTarget RateTermsConfirmation rtr);

}
