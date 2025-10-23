package com.adroit.hotlistmicroservice.mapper;

import com.adroit.hotlistmicroservice.dto.RTRInterviewDto;
import com.adroit.hotlistmicroservice.dto.UpdateInterviewDto;
import com.adroit.hotlistmicroservice.model.RTRInterview;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RTRInterviewMapper {

    @Mapping(source = "emailId",target = "consultantEmailId")
    RTRInterview rtrToRTRInterview(RateTermsConfirmation rtr);

    RTRInterviewDto rtrEntityToRTRDto(RTRInterview entity);

    void updateInterviewFromDto(UpdateInterviewDto dto, @MappingTarget RTRInterview entity);


}
