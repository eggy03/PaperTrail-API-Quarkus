package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.AuditLogRegistrationDTO;
import io.github.eggy03.papertrail.api.entity.AuditLogRegistration;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
import io.github.eggy03.papertrail.api.exceptions.GuildRegistrationFailureException;
import io.github.eggy03.papertrail.api.mapper.AuditLogRegistrationMapper;
import io.github.eggy03.papertrail.api.repository.AuditLogRegistrationRepository;
import io.github.eggy03.papertrail.api.service.interfaces.AuditLogRegistrationServiceInterface;
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
public final class AuditLogRegistrationService implements AuditLogRegistrationServiceInterface {

    private final AuditLogRegistrationRepository repository;
    private final AuditLogRegistrationMapper mapper;

    @Override
    @Transactional
    public @NotNull AuditLogRegistrationDTO registerGuild(@NonNull AuditLogRegistrationDTO dto) {

        try {
            repository.persistAndFlush(mapper.toEntity(dto));
            log.info("Audit Log Registration Succeeded for [Guild: {}, Channel: {}]", dto.getGuildId(), dto.getChannelId());
            return dto;
        } catch (ConstraintViolationException e) { // from hibernate
            log.info("Audit Log Registration Failed for [Guild: {}, Channel: {}] with [Reason: {}]", dto.getGuildId(), dto.getChannelId(), e.getMessage());
            throw new GuildRegistrationFailureException(e);
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    @CacheResult(cacheName = "auditLog")
    public @NotNull AuditLogRegistrationDTO viewRegisteredGuild(@NonNull @CacheKey Long guildId) {

        AuditLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered for audit logging"));

        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "auditLog")
    public @NotNull AuditLogRegistrationDTO updateRegisteredGuild(@NonNull @CacheKey Long guildId, @NonNull AuditLogRegistrationDTO updatedDto) {

        // dirty checking
        AuditLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered for audit logging"));

        log.info("Audit Log Registration Update Queued for [Guild: {}, Channel: {}], with new [Channel: {}]",
                entity.getGuildId(), entity.getChannelId(), updatedDto.getChannelId()
        );

        entity.setChannelId(updatedDto.getChannelId());
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheName = "auditLog")
    public void deleteRegisteredGuild(@NonNull @CacheKey Long guildId) {

        if (repository.deleteById(guildId))
            log.info("Audit Log Registration Removal Succeeded for [Guild: {}]", guildId);
        else {
            log.info("Audit Log Registration Removal Failed for [Guild: {}] with [Reason: Guild is not registered for audit logging]", guildId);
            throw new GuildNotFoundException("Guild is not registered for audit logging");
        }

    }
}
