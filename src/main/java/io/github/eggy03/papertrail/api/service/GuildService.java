package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.GuildDTO;
import io.github.eggy03.papertrail.api.entity.Guild;
import io.github.eggy03.papertrail.api.exceptions.AllChannelsNullException;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
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

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public final class GuildService implements GuildServiceInterface {

    private final GuildRepository repository;
    private final GuildMapper mapper;

    @Override
    @Transactional
    public void saveGuild(@NonNull GuildDTO dto) {

        log.debug("Saving guild configuration [guildId={}, guildEventChannelId={}, memberEventChannelId={}, messageEventChannelId={}]",
                dto.getGuildId(),
                dto.getGuildEventChannelId(),
                dto.getMemberEventChannelId(),
                dto.getMessageEventChannelId()
        );

        if (dto.getGuildEventChannelId() == null && dto.getMemberEventChannelId() == null && dto.getMessageEventChannelId() == null) {
            log.warn("Cannot save guild configuration because all event channels are null [guildId={}]", dto.getGuildId());
            throw new AllChannelsNullException("All three of the channels cannot be null together");
        }

        repository.persist(mapper.toEntity(dto));
    }

    @Override
    @Transactional
    @CacheResult(cacheName = "guild")
    public @NotNull GuildDTO viewGuild(@NonNull @CacheKey Long guildId) {

        log.debug("Fetching guild configuration for [guildId={}", guildId);

        Guild entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> {
                    log.debug("Guild configuration not found [guildId={}]", guildId);
                    return new GuildNotFoundException("Guild: %s is not saved".formatted(guildId));
                });

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "guild")
    public void updateGuild(@NonNull @CacheKey Long guildId, @NonNull GuildDTO updatedDto) {

        log.debug("Updating guild configuration [guildId={}, guildEventChannelId={}, memberEventChannelId={}, messageEventChannelId={}]",
                guildId,
                updatedDto.getGuildEventChannelId(),
                updatedDto.getMemberEventChannelId(),
                updatedDto.getMessageEventChannelId()
        );

        if (updatedDto.getGuildEventChannelId() == null && updatedDto.getMemberEventChannelId() == null && updatedDto.getMessageEventChannelId() == null) {
            log.warn("Cannot update guild configuration because all event channels are null [guildId={}]", guildId);
            throw new AllChannelsNullException("All three of the channels cannot be null together");
        }

        Guild entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> {
                    log.debug("Cannot update guild because configuration was not found [guildId={}]", guildId);
                    return new GuildNotFoundException("Guild: %s to be updated is not saved".formatted(guildId));
                });

        // dirty checking
        entity.setGuildEventChannelId(updatedDto.getGuildEventChannelId());
        entity.setMemberEventChannelId(updatedDto.getMemberEventChannelId());
        entity.setMessageEventChannelId(updatedDto.getMessageEventChannelId());
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "guild")
    public void deleteGuild(@NonNull @CacheKey Long guildId) {

        log.debug("Deleting guild configuration [guildId={}]", guildId);

        if (!repository.deleteById(guildId)) {
            log.debug("Cannot delete guild because configuration was not found [guildId={}]", guildId);
            throw new GuildNotFoundException("Guild: %s to be deleted is not saved".formatted(guildId));
        }
    }
}
