package io.github.eggy03.papertrail.api.service.interfaces;

import io.github.eggy03.papertrail.api.dto.GuildDTO;

public interface GuildServiceInterface {

    GuildDTO registerGuild(GuildDTO dto);

    GuildDTO viewRegisteredGuild(Long guildId);

    GuildDTO updateRegisteredGuild(Long guildId, GuildDTO updatedDto);

    void deleteRegisteredGuild(Long guildId);
}
