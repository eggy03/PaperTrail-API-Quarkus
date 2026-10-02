package io.github.eggy03.papertrail.api.mapper;

import io.github.eggy03.papertrail.api.dto.MessageDTO;
import io.github.eggy03.papertrail.api.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface MessageMapper {

    Message toEntity(MessageDTO messageDTO);

    MessageDTO toDTO(Message message);
}
