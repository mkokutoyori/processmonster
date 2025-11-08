package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.admin.SystemParameterDTO;
import com.processmonster.bpm.entity.SystemParameter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for SystemParameter entity
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SystemParameterMapper {

    @Mapping(target = "value", expression = "java(maskValueIfEncrypted(systemParameter))")
    SystemParameterDTO toDTO(SystemParameter systemParameter);

    /**
     * Mask encrypted values in DTOs (don't expose actual encrypted strings)
     */
    default String maskValueIfEncrypted(SystemParameter systemParameter) {
        if (systemParameter.getEncrypted() && systemParameter.getValue() != null) {
            return "***ENCRYPTED***";
        }
        return systemParameter.getValue();
    }
}
