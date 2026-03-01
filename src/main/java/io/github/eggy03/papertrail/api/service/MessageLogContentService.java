package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogContent;
import io.github.eggy03.papertrail.api.exceptions.MessageContentException;
import io.github.eggy03.papertrail.api.exceptions.MessageNotFoundException;
import io.github.eggy03.papertrail.api.mapper.MessageLogContentMapper;
import io.github.eggy03.papertrail.api.repository.MessageLogContentRepository;
import io.github.eggy03.papertrail.api.util.AnsiColor;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
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
public class MessageLogContentService {

    private final MessageLogContentRepository repository;
    private final MessageLogContentMapper mapper;

    @Transactional
    public @NotNull MessageLogContentDTO saveMessage(@NonNull MessageLogContentDTO dto) {

        try {
            repository.persistAndFlush(mapper.toEntity(dto));
            log.debug("{}Saved message with ID={}{}", AnsiColor.GREEN, dto.getMessageId(), AnsiColor.RESET);
            return dto;
        } catch (ConstraintViolationException e) {// from hibernate
            throw new MessageContentException(e);
        }
        // API Note: While ConstraintViolationException covers for a lot of constraints other than PK constraint
        // We have already covered them during dto validation phase in the controller
        // So realistically, only PK/UK constraint issues will be propagated from here
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @CacheResult(cacheName = "messageContent")
    public @NotNull MessageLogContentDTO getMessage(@NonNull @CacheKey Long messageId) {

        MessageLogContent entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message hasn't been saved yet"));

        return mapper.toDTO(entity);
    }

    @Transactional
    @CacheInvalidate(cacheName = "messageContent")
    public @NotNull MessageLogContentDTO updateMessage(@NonNull @CacheKey Long messageId, @NonNull MessageLogContentDTO updatedDto) {

        // this check is mostly redundant because the clients usually call view message before updating
        MessageLogContent entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message to be updated was never saved"));

        // quarkus will automatically detect changes to this entity and update the database
        entity.setMessageContent(updatedDto.getMessageContent());
        entity.setAuthorId(updatedDto.getAuthorId());

        log.debug("{}Updated message content having ID={}{}", AnsiColor.GREEN, messageId, AnsiColor.RESET);
        return updatedDto;
    }

    @Transactional
    @CacheInvalidate(cacheName = "messageContent")
    public void deleteMessage(@NonNull @CacheKey Long messageId) {

        repository
                .findByIdOptional(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message to be deleted was never saved"));

        if (repository.deleteById(messageId))
            log.debug("{} Deleted message having ID={}{}", AnsiColor.GREEN, messageId, AnsiColor.RESET);
        else
            log.warn("{}Failed to delete message having ID={}{}", AnsiColor.YELLOW, messageId, AnsiColor.RESET);
    }

    @Scheduled(every = "24h")
    @Transactional
    public void cleanupMessages() {
        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
        long deletedMessageCount = repository.deleteOlderThan(cutoff);
        log.info("{}Message Content Cleanup Service- Cleaned up {} messages older than {}{}", AnsiColor.GREEN, deletedMessageCount, cutoff, AnsiColor.RESET);
    }
}
