package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.MessageLogRegistrationDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogRegistration;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
import io.github.eggy03.papertrail.api.exceptions.GuildRegistrationFailureException;
import io.github.eggy03.papertrail.api.mapper.MessageLogRegistrationMapper;
import io.github.eggy03.papertrail.api.repository.MessageLogRegistrationRepository;
import io.github.eggy03.papertrail.api.service.interfaces.MessageLogRegistrationServiceInterface;
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
public final class MessageLogRegistrationService implements MessageLogRegistrationServiceInterface {

    private final MessageLogRegistrationRepository repository;
    private final MessageLogRegistrationMapper mapper;

    @Override
    @Transactional
    public @NotNull MessageLogRegistrationDTO registerGuild(@NonNull MessageLogRegistrationDTO dto) {

        try {
            repository.persistAndFlush(mapper.toEntity(dto));
            log.debug("Message Log Registration Succeeded for [Guild: {}, Channel: {}]", dto.getGuildId(), dto.getChannelId());
            return dto;
        } catch (ConstraintViolationException e) { // from hibernate
            log.debug("Message Log Registration Failed for [Guild: {}, Channel: {}] with [Reason: {}]", dto.getGuildId(), dto.getChannelId(), e.getMessage());
            throw new GuildRegistrationFailureException(e);
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    @CacheResult(cacheName = "messageLog")
    public @NotNull MessageLogRegistrationDTO viewRegisteredGuild(@NonNull @CacheKey Long guildId) {

        MessageLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered for message logging"));

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "messageLog")
    public @NotNull MessageLogRegistrationDTO updateRegisteredGuild(@NonNull @CacheKey Long guildId, @NonNull MessageLogRegistrationDTO updatedDto) {

        // dirty checking
        MessageLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered"));

        log.debug("Message Log Registration Update Queued for [Guild: {}, Channel: {}], with new [Channel: {}]",
                entity.getGuildId(), entity.getChannelId(), updatedDto.getChannelId()
        );
        
        entity.setChannelId(updatedDto.getChannelId());
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "messageLog")
    public void deleteRegisteredGuild(@NonNull @CacheKey Long guildId) {

        if (repository.deleteById(guildId))
            log.debug("Message Log Registration Removal Succeeded for [Guild: {}]", guildId);
        else {
            log.debug("Message Log Registration Removal Failed for [Guild: {}] with [Reason: Guild is not registered for message logging]", guildId);
            throw new GuildNotFoundException("Guild is not registered for message logging");
        }
    }
}
