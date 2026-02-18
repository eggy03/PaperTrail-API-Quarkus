package io.github.eggy03.mapper;

import io.github.eggy03.dto.AuditLogRegistrationDTO;
import io.github.eggy03.entity.AuditLogRegistration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface AuditLogRegistrationMapper {

    AuditLogRegistration toEntity (AuditLogRegistrationDTO auditLogRegistrationDTO);

    AuditLogRegistrationDTO toDTO (AuditLogRegistration auditLogRegistration);
}
