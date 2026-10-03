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

        log.debug("Saving message [messageId={}, authorId={}]", dto.getMessageId(), dto.getAuthorId());

        repository.persist(mapper.toEntity(dto));
    }

    @Override
    @Transactional
    public @NotNull MessageDTO getMessage(@NonNull Long messageId) {

        log.debug("Fetching message [messageId={}]", messageId);

        Message entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> {
                    log.debug("Message not found [messageId={}]", messageId);
                    return new MessageNotFoundException("Message hasn't been saved yet");
                });

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void updateMessage(
            @NonNull Long messageId,
            @NonNull MessageDTO updatedDto
    ) {

        log.debug("Updating message [messageId={}, authorId={}]", messageId, updatedDto.getAuthorId());

        Message entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> {
                    log.debug("Cannot update message because it was not found [messageId={}]", messageId);
                    return new MessageNotFoundException("Message: %s to be updated was never saved".formatted(messageId));
                });

        // dirty checking
        entity.setMessageContent(updatedDto.getMessageContent());
        entity.setAuthorId(updatedDto.getAuthorId());
    }

    @Override
    @Transactional
    public void deleteMessage(@NonNull Long messageId) {

        log.debug("Deleting message [messageId={}]", messageId);

        if (!repository.deleteById(messageId)) {
            log.debug("Cannot delete message because it was not found [messageId={}]", messageId);
            throw new MessageNotFoundException("Message: %s to be deleted was never saved".formatted(messageId));
        }
    }

    @Scheduled(every = "24h")
    @Transactional
    public void cleanupMessages() {

        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);

        log.debug("Starting message cleanup [cutoff={}]", cutoff);

        long deletedMessageCount = repository.deleteOlderThan(cutoff);

        log.info("Message cleanup completed [deletedMessages={}, cutoff={}]", deletedMessageCount, cutoff);
    }
}