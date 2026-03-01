package io.github.eggy03.papertrail.api.service.locks;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import lombok.NonNull;

/**
 * This interface has a signature matching {@link io.github.eggy03.papertrail.api.service.MessageLogContentService}
 * Implementations of this interface will usually wrap the above described service methods in redisson locks
 * or without it.
 * <p>
 * Quarkus CDI will choose the implementation based on profiles
 */
public interface MessageLogContentOperation {

    MessageLogContentDTO saveMessage(@NonNull MessageLogContentDTO dto);

    MessageLogContentDTO getMessage(@NonNull Long messageId);

    MessageLogContentDTO updateMessage(@NonNull Long messageId, @NonNull MessageLogContentDTO dto);

    void deleteMessage(@NonNull Long messageId);
}
