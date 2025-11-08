package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.apikey.ApiKeyDTO;
import com.processmonster.bpm.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for ApiKey entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    @Mapping(target = "active", expression = "java(apiKey.isActive())")
    @Mapping(target = "expired", expression = "java(apiKey.isExpired())")
    ApiKeyDTO toDTO(ApiKey apiKey);
}
