package io.github.eggy03.papertrail.api.service.interfaces;

import io.github.eggy03.papertrail.api.dto.GuildDTO;

public interface GuildServiceInterface {

    void saveGuild(GuildDTO dto);

    GuildDTO viewGuild(Long guildId);

    void updateGuild(Long guildId, GuildDTO updatedDto);

    void deleteGuild(Long guildId);
}
