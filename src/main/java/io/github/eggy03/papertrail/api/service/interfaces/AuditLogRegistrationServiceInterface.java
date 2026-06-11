package io.github.eggy03.papertrail.api.service.interfaces;

import io.github.eggy03.papertrail.api.dto.AuditLogRegistrationDTO;

public interface AuditLogRegistrationServiceInterface {

    AuditLogRegistrationDTO registerGuild(AuditLogRegistrationDTO dto);

    AuditLogRegistrationDTO viewRegisteredGuild(Long guildId);

    AuditLogRegistrationDTO updateRegisteredGuild(Long guildId, AuditLogRegistrationDTO updatedDto);

    void deleteRegisteredGuild(Long guildId);
}
