package io.github.eggy03.papertrail.api.mapper;

import io.github.eggy03.papertrail.api.dto.GuildDTO;
import io.github.eggy03.papertrail.api.entity.Guild;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface GuildMapper {

    Guild toEntity(GuildDTO guildDTO);

    GuildDTO toDTO(Guild guild);
}
