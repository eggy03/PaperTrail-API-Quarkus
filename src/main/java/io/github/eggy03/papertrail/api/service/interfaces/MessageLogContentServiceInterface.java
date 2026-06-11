package io.github.eggy03.papertrail.api.service.interfaces;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;

public interface MessageLogContentServiceInterface {

    MessageLogContentDTO saveMessage(MessageLogContentDTO dto);

    MessageLogContentDTO getMessage(Long messageId);

    MessageLogContentDTO updateMessage(Long messageId, MessageLogContentDTO updatedDto);

    void deleteMessage(Long messageId);
}
