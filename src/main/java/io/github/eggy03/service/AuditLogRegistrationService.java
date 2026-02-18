package io.github.eggy03.service;

import io.github.eggy03.dto.AuditLogRegistrationDTO;
import io.github.eggy03.entity.AuditLogRegistration;
import io.github.eggy03.exceptions.GuildAlreadyRegisteredException;
import io.github.eggy03.exceptions.GuildNotFoundException;
import io.github.eggy03.mapper.AuditLogRegistrationMapper;
import io.github.eggy03.repository.AuditLogRegistrationRepository;
import io.github.eggy03.util.AnsiColor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class AuditLogRegistrationService {

    private final AuditLogRegistrationRepository repository;
    private final AuditLogRegistrationMapper mapper;

    @Transactional
    @NotNull
    public AuditLogRegistrationDTO registerGuild (@NonNull AuditLogRegistrationDTO dto) {

        if(repository.findById(dto.getGuildId())!=null)
            throw new GuildAlreadyRegisteredException("Guild is already registered for audit logging");

        repository.persistAndFlush(mapper.toEntity(dto));
        log.debug("{}Saved audit log guild with ID={}{}", AnsiColor.GREEN, dto.getGuildId(), AnsiColor.RESET);
        return dto;
    }

    @NotNull
    public AuditLogRegistrationDTO viewRegisteredGuild (@NonNull Long guildId) {

        AuditLogRegistration entity = repository
                .findByIdOptional(guildId)
                .orElseThrow(()-> new GuildNotFoundException("Guild is not registered for audit logging"));

        return mapper.toDTO(entity);
    }

    @Transactional
    @NotNull
    public AuditLogRegistrationDTO updateRegisteredGuild (@NonNull AuditLogRegistrationDTO updatedDto) {

        // dirty checking
        AuditLogRegistration entity = repository
                .findByIdOptional(updatedDto.getGuildId())
                .orElseThrow(() -> new GuildNotFoundException("Guild is not registered"));

        entity.setChannelId(updatedDto.getChannelId());

        log.debug("{}Updated audit log guild with ID={}{}", AnsiColor.GREEN, updatedDto.getGuildId(), AnsiColor.RESET);
        return updatedDto;
    }

    @Transactional
    public void deleteRegisteredGuild (@NonNull Long guildId) {

        if(repository.findById(guildId)==null)
            throw new GuildNotFoundException("Guild is not registered for audit logging");

        if(repository.deleteById(guildId))
            log.debug("{} Deleted audit log guild with ID={}{}", AnsiColor.GREEN, guildId, AnsiColor.RESET);
        else
            log.warn("{}Failed to delete audit log guild with ID={}{}", AnsiColor.YELLOW, guildId, AnsiColor.RESET);
    }
}
