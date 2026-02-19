package io.github.eggy03.service;

import io.github.eggy03.dto.MessageLogContentDTO;
import io.github.eggy03.entity.MessageLogContent;
import io.github.eggy03.exceptions.MessageAlreadyLoggedException;
import io.github.eggy03.exceptions.MessageNotFoundException;
import io.github.eggy03.mapper.MessageLogContentMapper;
import io.github.eggy03.repository.MessageLogContentRepository;
import io.github.eggy03.service.cache.MessageLogContentCacheService;
import io.github.eggy03.util.AnsiColor;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MessageLogContentService {

    private final MessageLogContentRepository repository;
    private final MessageLogContentMapper mapper;
    private final MessageLogContentCacheService cacheService;

    @Transactional
    public @NotNull MessageLogContentDTO saveMessage (@NonNull MessageLogContentDTO dto) {

        if(repository.findById(dto.getMessageId())!=null)
            throw new MessageAlreadyLoggedException("Message has already been saved");

        repository.persistAndFlush(mapper.toEntity(dto));
        log.debug("{}Saved message with ID={}{}", AnsiColor.GREEN, dto.getMessageId(), AnsiColor.RESET);
        return dto;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @CacheResult(cacheName = "messageContent")
    public @NotNull MessageLogContentDTO getMessage(@NonNull @CacheKey Long messageId) {

        MessageLogContent entity = repository
                .findByIdOptional(messageId)
                .orElseThrow(()-> new MessageNotFoundException("Message hasn't been saved yet"));

        return mapper.toDTO(entity);
    }

    @Transactional
    public @NotNull MessageLogContentDTO updateMessage (@NonNull MessageLogContentDTO updatedDto) {

        // dirty checking
        MessageLogContent entity = repository
                .findByIdOptional(updatedDto.getMessageId())
                .orElseThrow(() -> new MessageNotFoundException("Message to be updated was never saved"));

        entity.setMessageContent(updatedDto.getMessageContent());
        entity.setAuthorId(updatedDto.getAuthorId());

        cacheService.invalidateCache(updatedDto.getMessageId());

        log.debug("{}Updated message content having ID={}{}", AnsiColor.GREEN, updatedDto.getMessageId(), AnsiColor.RESET);
        return updatedDto;
    }

    @Transactional
    @CacheInvalidate(cacheName = "messageContent")
    public void deleteMessage (@NonNull @CacheKey Long messageId) {

        if(repository.findById(messageId)==null)
            throw new MessageNotFoundException("Message to be deleted was never saved");

        if(repository.deleteById(messageId))
            log.debug("{} Deleted message having ID={}{}", AnsiColor.GREEN, messageId, AnsiColor.RESET);
        else
            log.warn("{}Failed to delete message having ID={}{}", AnsiColor.YELLOW, messageId, AnsiColor.RESET);
    }
}
