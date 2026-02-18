package io.github.eggy03.mapper;

import io.github.eggy03.dto.MessageLogRegistrationDTO;
import io.github.eggy03.entity.MessageLogRegistration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface MessageLogRegistrationMapper {

    MessageLogRegistration toEntity (MessageLogRegistrationDTO messageLogRegistrationDTO);

    MessageLogRegistrationDTO toDTO (MessageLogRegistration messageLogRegistration);
}
