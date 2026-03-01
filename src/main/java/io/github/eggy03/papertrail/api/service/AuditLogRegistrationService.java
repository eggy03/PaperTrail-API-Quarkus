package io.github.eggy03.papertrail.api.service;

import io.github.eggy03.papertrail.api.dto.AuditLogRegistrationDTO;
import io.github.eggy03.papertrail.api.entity.AuditLogRegistration;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
import io.github.eggy03.papertrail.api.exceptions.GuildRegistrationException;
import io.github.eggy03.papertrail.api.mapper.AuditLogRegistrationMapper;
import io.github.eggy03.papertrail.api.repository.AuditLogRegistrationRepository;
import io.github.eggy03.papertrail.api.util.AnsiColor;
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
public class AuditLogRegistrationService {

    private final AuditLogRegistrationRepository repository;
    private final AuditLogRegistrationMapper mapper;

    @Transactional
    public @NotNull AuditLogRegistrationDTO registerGuild(@NonNull AuditLogRegistrationDTO dto) {

        try {
            repository.persistAndFlush(mapper.toEntity(dto));
            log.debug("{}Saved audit log guild with ID={}{}", AnsiColor.GREEN, dto.getGuildId(), AnsiColor.RESET);
            return dto;
        } catch (ConstraintViolationException e) { // from hibernate
            throw new GuildRegistrationException(e);
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @CacheResult(cacheName = "auditLog")
    public @NotNull AuditLogRegistrationDTO viewRegisteredGuild(@NonNull @CacheKey Long guildId) {

        AuditLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered for audit logging"));

        return mapper.toDTO(entity);
    }

    @Transactional
    @CacheInvalidate(cacheName = "auditLog")
    public @NotNull AuditLogRegistrationDTO updateRegisteredGuild(@NonNull @CacheKey Long guildId, @NonNull AuditLogRegistrationDTO updatedDto) {

        // dirty checking
        AuditLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered"));

        entity.setChannelId(updatedDto.getChannelId());

        log.debug("{}Updated audit log guild with ID={}{}", AnsiColor.GREEN, guildId, AnsiColor.RESET);
        return updatedDto;
    }

    @Transactional
    @CacheInvalidate(cacheName = "auditLog")
    public void deleteRegisteredGuild(@NonNull @CacheKey Long guildId) {

        repository
                .findByIdOptional(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered for audit logging"));

        if (repository.deleteById(guildId))
            log.debug("{}Deleted audit log guild with ID={}{}", AnsiColor.GREEN, guildId, AnsiColor.RESET);
        else
            log.warn("{}Failed to delete audit log guild with ID={}{}", AnsiColor.YELLOW, guildId, AnsiColor.RESET);
    }
}
