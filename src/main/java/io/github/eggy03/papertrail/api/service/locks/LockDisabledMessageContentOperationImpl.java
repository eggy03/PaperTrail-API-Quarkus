package io.github.eggy03.papertrail.api.service.locks;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.service.MessageLogContentService;
import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.common.constraint.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@IfBuildProperty(name = "message.locks.enabled", stringValue = "false", enableIfMissing = true)
@RequiredArgsConstructor
public class LockDisabledMessageContentOperationImpl implements MessageLogContentOperation {

    private final MessageLogContentService delegate;

    @Override
    @NotNull
    public MessageLogContentDTO saveMessage(@NonNull MessageLogContentDTO dto) {
        return delegate.saveMessage(dto);
    }

    @Override
    @NotNull
    public MessageLogContentDTO getMessage(@NonNull Long messageId) {
        return delegate.getMessage(messageId);
    }

    @Override
    @NotNull
    public MessageLogContentDTO updateMessage(@NonNull Long messageId, @NonNull MessageLogContentDTO dto) {
        return delegate.updateMessage(messageId, dto);
    }

    @Override
    public void deleteMessage(@NonNull Long messageId) {
        delegate.deleteMessage(messageId);
    }
}
