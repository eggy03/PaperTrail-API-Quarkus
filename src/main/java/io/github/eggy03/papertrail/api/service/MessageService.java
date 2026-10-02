package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.MessageDTO;
import io.github.eggy03.papertrail.api.entity.Message;
import io.github.eggy03.papertrail.api.exceptions.MessageNotFoundException;
import io.github.eggy03.papertrail.api.mapper.MessageMapper;
import io.github.eggy03.papertrail.api.repository.MessageRepository;
import io.github.eggy03.papertrail.api.service.interfaces.MessageServiceInterface;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.constraint.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public final class MessageService implements MessageServiceInterface {

    private final MessageRepository repository;
    private final MessageMapper mapper;

    @Override
    @Transactional
    public void saveMessage(@NonNull MessageDTO dto) {
        repository.persist(mapper.toEntity(dto));
    }

    @Override
    @Transactional
    public @NotNull MessageDTO getMessage(@NonNull Long messageId) {

        Message entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message hasn't been saved yet"));

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void updateMessage(@NonNull Long messageId, @NonNull MessageDTO updatedDto) {

        Message entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message: %s to be updated was never saved".formatted(messageId)));

        // quarkus will automatically detect changes to this entity and update the database
        entity.setMessageContent(updatedDto.getMessageContent());
        entity.setAuthorId(updatedDto.getAuthorId());
    }

    @Override
    @Transactional
    public void deleteMessage(@NonNull Long messageId) {
        if (!repository.deleteById(messageId))
            throw new MessageNotFoundException("Message :%s to be deleted was never saved".formatted(messageId));
    }

    @Scheduled(every = "24h")
    @Transactional
    public void cleanupMessages() {
        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
        long deletedMessageCount = repository.deleteOlderThan(cutoff);
        log.debug("Message Content Cleanup Service- Cleaned up {} messages older than {}", deletedMessageCount, cutoff);
    }
}
