package io.github.eggy03.papertrail.api.service.interfaces;

import io.github.eggy03.papertrail.api.dto.MessageDTO;

public interface MessageServiceInterface {

    void saveMessage(MessageDTO dto);

    MessageDTO getMessage(Long messageId);

    void updateMessage(Long messageId, MessageDTO updatedDto);

    void deleteMessage(Long messageId);
}
