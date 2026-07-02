package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogContent;
import io.github.eggy03.papertrail.api.exceptions.MessageNotFoundException;
import io.github.eggy03.papertrail.api.exceptions.MessageSaveFailureException;
import io.github.eggy03.papertrail.api.mapper.MessageLogContentMapper;
import io.github.eggy03.papertrail.api.repository.MessageLogContentRepository;
import io.github.eggy03.papertrail.api.service.interfaces.MessageLogContentServiceInterface;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.constraint.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public final class MessageLogContentService implements MessageLogContentServiceInterface {

    private final MessageLogContentRepository repository;
    private final MessageLogContentMapper mapper;

    @Override
    @Transactional
    public @NotNull MessageLogContentDTO saveMessage(@NonNull MessageLogContentDTO dto) {

        try {
            repository.persistAndFlush(mapper.toEntity(dto));
            log.debug("Message Save Succeeded for [MessageID: {}, AuthorID: {}], having [Content: {}]", dto.getMessageId(), dto.getAuthorId(), dto.getMessageContent());
            return dto;
        } catch (ConstraintViolationException e) {// from hibernate
            log.debug("Message Save Failed for [MessageID: {}, AuthorID: {}], having [Content: {}] with [REASON: {}]", dto.getMessageId(), dto.getAuthorId(), dto.getMessageContent(), e.getMessage());
            throw new MessageSaveFailureException(e);
        }
        // API Note: While ConstraintViolationException covers for a lot of constraints other than PK constraint
        // We have already covered them during dto validation phase in the controller
        // So realistically, only PK/UK constraint issues will be propagated from here
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public @NotNull MessageLogContentDTO getMessage(@NonNull Long messageId) {

        MessageLogContent entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message hasn't been saved yet"));

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public @NotNull MessageLogContentDTO updateMessage(@NonNull Long messageId, @NonNull MessageLogContentDTO updatedDto) {

        // this check is mostly redundant because the clients usually call view message before updating
        MessageLogContent entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message to be updated was never saved"));

        log.debug("Message update queued for [MessageID: {}][Old Message Content: {}, Old AuthorID: {}][New Message Content: {}, New AuthorID: {}]",
                messageId, entity.getMessageContent(), entity.getAuthorId(), updatedDto.getMessageContent(), updatedDto.getAuthorId()
        );

        // quarkus will automatically detect changes to this entity and update the database
        entity.setMessageContent(updatedDto.getMessageContent());
        entity.setAuthorId(updatedDto.getAuthorId());

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void deleteMessage(@NonNull Long messageId) {

        if (repository.deleteById(messageId))
            log.debug("Message Deletion Succeeded for [MessageID: {}]", messageId);
        else {
            log.debug("Message Deletion Failed for [MessageID: {}] with [Reason: Message was not saved before]", messageId);
            throw new MessageNotFoundException("Message to be deleted was never saved");
        }
    }

    @Scheduled(every = "24h")
    @Transactional
    public void cleanupMessages() {
        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
        long deletedMessageCount = repository.deleteOlderThan(cutoff);
        log.debug("Message Content Cleanup Service- Cleaned up {} messages older than {}", deletedMessageCount, cutoff);
    }
}
