package unit;

import io.github.eggy03.papertrail.api.dto.GuildDTO;
import io.github.eggy03.papertrail.api.entity.Guild;
import io.github.eggy03.papertrail.api.exceptions.GuildNotFoundException;
import io.github.eggy03.papertrail.api.mapper.GuildMapper;
import io.github.eggy03.papertrail.api.repository.GuildRepository;
import io.github.eggy03.papertrail.api.service.GuildService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuildServiceUnitTest {

    static final Long TEST_GUILD_ID = 1302148573926148096L;
    static final Long TEST_GUILD_EVENT_CHANNEL_ID = 1302148573926148097L;
    static final Long TEST_MEMBER_EVENT_CHANNEL_ID = 1302148573926148098L;
    static final Long TEST_MESSAGE_EVENT_CHANNEL_ID = 1302148573926148099L;
    // prep a valid Entity
    final Guild validEntity = new Guild(TEST_GUILD_ID, TEST_GUILD_EVENT_CHANNEL_ID, TEST_MEMBER_EVENT_CHANNEL_ID, TEST_MESSAGE_EVENT_CHANNEL_ID);
    // prep a valid DTO
    final GuildDTO validDTO = new GuildDTO(TEST_GUILD_ID, TEST_GUILD_EVENT_CHANNEL_ID, TEST_MEMBER_EVENT_CHANNEL_ID, TEST_MESSAGE_EVENT_CHANNEL_ID);
    @Mock
    GuildRepository repository;
    @Mock
    GuildMapper mapper;
    @InjectMocks
    GuildService service;

    @Test
    void saveGuild_success() {

        when(mapper.toEntity(validDTO)).thenReturn(validEntity);

        service.saveGuild(validDTO);

        verify(repository).persist(validEntity);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void viewGuild_success() {

        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.of(validEntity));
        when(mapper.toDTO(validEntity)).thenReturn(validDTO);

        GuildDTO result = service.viewGuild(TEST_GUILD_ID);
        assertThat(result).isEqualTo(validDTO);

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verify(mapper).toDTO(validEntity);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void viewGuild_notRegistered_notFound() {

        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.empty());

        assertThrows(GuildNotFoundException.class, () -> service.viewGuild(TEST_GUILD_ID));

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verify(mapper, never()).toDTO(any());
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void updateGuild_success() {

        Guild oldEntity = new Guild(TEST_GUILD_ID, 123L, 456L, 789L);
        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.of(oldEntity));

        service.updateGuild(TEST_GUILD_ID, validDTO);
        assertThat(oldEntity.getGuildEventChannelId()).isEqualTo(validDTO.getGuildEventChannelId()); // confirm that old entity was mutated with new dto data
        assertThat(oldEntity.getMemberEventChannelId()).isEqualTo(validDTO.getMemberEventChannelId());
        assertThat(oldEntity.getMessageEventChannelId()).isEqualTo(validDTO.getMessageEventChannelId());

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);

    }

    @Test
    void updateGuild_doesNotExist() {

        when(repository.findByIdOptional(TEST_GUILD_ID)).thenReturn(Optional.empty());

        assertThrows(GuildNotFoundException.class, () -> service.updateGuild(TEST_GUILD_ID, validDTO));

        verify(repository).findByIdOptional(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);
    }


    @Test
    void deleteGuild_success() {

        when(repository.deleteById(TEST_GUILD_ID)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteGuild(TEST_GUILD_ID));

        verify(repository).deleteById(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteGuild_doesNotExist_notFound() {

        when(repository.deleteById(TEST_GUILD_ID)).thenReturn(false);

        assertThrows(GuildNotFoundException.class, () -> service.deleteGuild(TEST_GUILD_ID));

        verify(repository).deleteById(TEST_GUILD_ID);
        verifyNoMoreInteractions(repository);
    }
}
