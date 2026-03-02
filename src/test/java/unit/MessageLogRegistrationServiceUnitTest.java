package unit;

import io.github.eggy03.papertrail.api.dto.MessageLogRegistrationDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogRegistration;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
import io.github.eggy03.papertrail.api.exceptions.GuildRegistrationFailureException;
import io.github.eggy03.papertrail.api.mapper.MessageLogRegistrationMapper;
import io.github.eggy03.papertrail.api.repository.MessageLogRegistrationRepository;
import io.github.eggy03.papertrail.api.service.MessageLogRegistrationService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageLogRegistrationServiceUnitTest {

    static final Long TEST_GUILD_ID = 1302148573926148096L;
    static final Long TEST_CHANNEL_ID = 1302148573926148097L;
    // prep a valid Entity
    final MessageLogRegistration validEntity = new MessageLogRegistration(TEST_GUILD_ID, TEST_CHANNEL_ID);
    // prep a valid DTO
    final MessageLogRegistrationDTO validDTO = new MessageLogRegistrationDTO(TEST_GUILD_ID, TEST_CHANNEL_ID);
    @Mock
    MessageLogRegistrationRepository repository;
    @Mock
    MessageLogRegistrationMapper mapper;
    @InjectMocks
    MessageLogRegistrationService service;

    @Test
    void registerGuild_success() {

        when(mapper.toEntity(validDTO)).thenReturn(validEntity);

        service.registerGuild(validDTO);

        verify(repository).persistAndFlush(validEntity);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void registerGuild_alreadyExists_conflicts() {

        when(mapper.toEntity(validDTO)).thenReturn(validEntity);
        doThrow(ConstraintViolationException.class).when(repository).persistAndFlush(validEntity);

        assertThrows(GuildRegistrationFailureException.class, () -> service.registerGuild(validDTO));

        verify(mapper).toEntity(validDTO);
        verify(repository).persistAndFlush(validEntity);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void getGuild_success() {

        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.of(validEntity));
        when(mapper.toDTO(validEntity)).thenReturn(validDTO);

        MessageLogRegistrationDTO result = service.viewRegisteredGuild(TEST_GUILD_ID);
        assertThat(result).isEqualTo(validDTO);

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verify(mapper).toDTO(validEntity);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void getGuild_notRegistered_notFound() {

        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.empty());

        assertThrows(GuildNotFoundException.class, () -> service.viewRegisteredGuild(TEST_GUILD_ID));

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verify(mapper, never()).toDTO(any());
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void updateGuild_success() {

        MessageLogRegistration oldEntity = new MessageLogRegistration(TEST_GUILD_ID, 123L);
        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.of(oldEntity));

        service.updateRegisteredGuild(TEST_GUILD_ID, validDTO);
        assertThat(oldEntity.getChannelId()).isEqualTo(validDTO.getChannelId()); // confirm that old entity was mutated with new dto data

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);

    }

    @Test
    void updateGuild_doesNotExist() {

        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.empty());

        assertThrows(GuildNotFoundException.class, () -> service.updateRegisteredGuild(TEST_GUILD_ID, validDTO));

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);
    }


    @Test
    void deleteGuild_success() {

        when(repository.deleteById(TEST_GUILD_ID)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteRegisteredGuild(TEST_GUILD_ID));

        verify(repository).deleteById(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteGuild_doesNotExist_notFound() {

        when(repository.deleteById(TEST_GUILD_ID)).thenReturn(false);

        assertThrows(GuildNotFoundException.class, () -> service.deleteRegisteredGuild(TEST_GUILD_ID));

        verify(repository).deleteById(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);
    }
}
