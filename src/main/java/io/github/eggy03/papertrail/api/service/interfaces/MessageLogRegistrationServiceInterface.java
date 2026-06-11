package io.github.eggy03.papertrail.api.service.interfaces;

import io.github.eggy03.papertrail.api.dto.MessageLogRegistrationDTO;

public interface MessageLogRegistrationServiceInterface {

    MessageLogRegistrationDTO registerGuild(MessageLogRegistrationDTO dto);

    MessageLogRegistrationDTO viewRegisteredGuild(Long guildId);

    MessageLogRegistrationDTO updateRegisteredGuild(Long guildId, MessageLogRegistrationDTO updatedDto);

    void deleteRegisteredGuild(Long guildId);
}
