package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.MessageLogRegistrationDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogRegistration;
import io.github.eggy03.papertrail.api.exceptions.GuildAlreadyRegisteredException;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
import io.github.eggy03.papertrail.api.mapper.MessageLogRegistrationMapper;
import io.github.eggy03.papertrail.api.repository.MessageLogRegistrationRepository;
import io.github.eggy03.papertrail.api.service.cache.MessageLogRegistrationCacheService;
import io.github.eggy03.papertrail.api.util.AnsiColor;
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
public class MessageLogRegistrationService {

    private final MessageLogRegistrationRepository repository;
    private final MessageLogRegistrationMapper mapper;
    private final MessageLogRegistrationCacheService cacheService;

    @Transactional
    public @NotNull MessageLogRegistrationDTO registerGuild(@NonNull MessageLogRegistrationDTO dto) {

        if (repository.findById(dto.getGuildId()) != null)
            throw new GuildAlreadyRegisteredException("Guild is already registered for message logging");

        repository.persistAndFlush(mapper.toEntity(dto));
        log.debug("{}Saved message log guild with ID={}{}", AnsiColor.GREEN, dto.getGuildId(), AnsiColor.RESET);
        return dto;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @CacheResult(cacheName = "messageLog")
    public @NotNull MessageLogRegistrationDTO viewRegisteredGuild(@NonNull @CacheKey Long guildId) {

        MessageLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered for message logging"));

        return mapper.toDTO(entity);
    }

    @Transactional
    public @NotNull MessageLogRegistrationDTO updateRegisteredGuild(@NonNull MessageLogRegistrationDTO updatedDto) {

        // dirty checking
        MessageLogRegistration entity = repository
                .findByIdOptional(updatedDto.getGuildId())
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered"));

        entity.setChannelId(updatedDto.getChannelId());
        cacheService.invalidateCache(updatedDto.getGuildId());

        log.debug("{}Updated message log guild with ID={}{}", AnsiColor.GREEN, updatedDto.getGuildId(), AnsiColor.RESET);
        return updatedDto;
    }

    @Transactional
    @CacheInvalidate(cacheName = "messageLog")
    public void deleteRegisteredGuild(@NonNull @CacheKey Long guildId) {

        if (repository.findById(guildId) == null)
            throw new GuildNotFoundException("Guild is not registered for message logging");

        if (repository.deleteById(guildId))
            log.debug("{} Deleted message log guild with ID={}{}", AnsiColor.GREEN, guildId, AnsiColor.RESET);
        else
            log.warn("{}Failed to delete message log guild with ID={}{}", AnsiColor.YELLOW, guildId, AnsiColor.RESET);
    }
}
