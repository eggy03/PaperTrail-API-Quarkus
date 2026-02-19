package io.github.eggy03.papertrail.api.mapper;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogContent;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface MessageLogContentMapper {

    MessageLogContent toEntity (MessageLogContentDTO messageLogContentDTO);

    MessageLogContentDTO toDTO (MessageLogContent messageLogContent);
}
