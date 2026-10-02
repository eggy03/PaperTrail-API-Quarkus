package io.github.eggy03.papertrail.api.service.interfaces;

import io.github.eggy03.papertrail.api.dto.MessageDTO;

public interface MessageServiceInterface {

    MessageDTO saveMessage(MessageDTO dto);

    MessageDTO getMessage(Long messageId);

    MessageDTO updateMessage(Long messageId, MessageDTO updatedDto);

    void deleteMessage(Long messageId);
}
