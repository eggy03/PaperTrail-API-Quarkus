package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.GuildDTO;
import io.github.eggy03.papertrail.api.entity.Guild;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
import io.github.eggy03.papertrail.api.exceptions.GuildRegistrationFailureException;
import io.github.eggy03.papertrail.api.mapper.GuildMapper;
import io.github.eggy03.papertrail.api.repository.GuildRepository;
import io.github.eggy03.papertrail.api.service.interfaces.GuildServiceInterface;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.smallrye.common.constraint.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public final class GuildService implements GuildServiceInterface {

    private final GuildRepository repository;
    private final GuildMapper mapper;

    @Override
    @Transactional
    public void saveGuild(@NonNull GuildDTO dto) {

        try {
            repository.persistAndFlush(mapper.toEntity(dto));
        } catch (ConstraintViolationException e) { // from hibernate
            throw new GuildRegistrationFailureException(e);
        }
    }

    @Override
    @Transactional
    @CacheResult(cacheName = "guild")
    public @NotNull GuildDTO viewGuild(@NonNull @CacheKey Long guildId) {

        Guild entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild: %s is not saved".formatted(guildId)));

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "guild")
    public void updateGuild(@NonNull @CacheKey Long guildId, @NonNull GuildDTO updatedDto) {

        Guild entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild: %s to be updated is not saved".formatted(guildId)));

        // dirty checking
        entity.setGuildEventChannelId(updatedDto.getGuildEventChannelId());
        entity.setMemberEventChannelId(updatedDto.getMemberEventChannelId());
        entity.setMessageEventChannelId(updatedDto.getMessageEventChannelId());
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "guild")
    public void deleteGuild(@NonNull @CacheKey Long guildId) {
        if (!repository.deleteById(guildId))
            throw new GuildNotFoundException("Guild: %s to be deleted is not saved".formatted(guildId));
    }
}
