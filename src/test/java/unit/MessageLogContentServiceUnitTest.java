package unit;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogContent;
import io.github.eggy03.papertrail.api.exceptions.MessageNotFoundException;
import io.github.eggy03.papertrail.api.exceptions.MessageSaveFailureException;
import io.github.eggy03.papertrail.api.mapper.MessageLogContentMapper;
import io.github.eggy03.papertrail.api.repository.MessageLogContentRepository;
import io.github.eggy03.papertrail.api.service.MessageLogContentService;
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
class MessageLogContentServiceUnitTest {

    static final Long TEST_MESSAGE_ID = 1302148573926148096L;
    static final String TEST_MESSAGE_CONTENT = "message";
    static final Long TEST_AUTHOR_ID = 1302148573926148097L;
    // prep a valid Entity
    final MessageLogContent validEntity = new MessageLogContent(TEST_MESSAGE_ID, TEST_MESSAGE_CONTENT, TEST_AUTHOR_ID, null);
    // prep a valid DTO
    final MessageLogContentDTO validDTO = new MessageLogContentDTO(TEST_MESSAGE_ID, TEST_MESSAGE_CONTENT, TEST_AUTHOR_ID);
    @Mock
    MessageLogContentRepository repository;
    @Mock
    MessageLogContentMapper mapper;
    @InjectMocks
    MessageLogContentService service;

    @Test
    void saveMessage_success() {

        when(mapper.toEntity(validDTO)).thenReturn(validEntity);

        service.saveMessage(validDTO);

        verify(repository).persistAndFlush(validEntity);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void saveMessage_alreadyExists_conflicts() {

        when(mapper.toEntity(validDTO)).thenReturn(validEntity);
        doThrow(ConstraintViolationException.class).when(repository).persistAndFlush(validEntity);

        assertThrows(MessageSaveFailureException.class, () -> service.saveMessage(validDTO));

        verify(mapper).toEntity(validDTO);
        verify(repository).persistAndFlush(validEntity);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void getMessage_success() {

        when(repository.findByIdOptional(TEST_MESSAGE_ID)).thenReturn(Optional.of(validEntity));
        when(mapper.toDTO(validEntity)).thenReturn(validDTO);

        MessageLogContentDTO result = service.getMessage(TEST_MESSAGE_ID);
        assertThat(result).isEqualTo(validDTO);

        verify(repository).findByIdOptional(TEST_MESSAGE_ID);
        verify(mapper).toDTO(validEntity);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void getMessage_notSaved_notFound() {

        when(repository.findByIdOptional(TEST_MESSAGE_ID)).thenReturn(Optional.empty());

        assertThrows(MessageNotFoundException.class, () -> service.getMessage(TEST_MESSAGE_ID));

        verify(repository).findByIdOptional(TEST_MESSAGE_ID);
        verify(mapper, never()).toDTO(any());
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void updateMessage_success() {

        MessageLogContent oldEntity = new MessageLogContent(TEST_MESSAGE_ID, "oldMessage", 123L, null);
        when(repository.findByIdOptional(TEST_MESSAGE_ID)).thenReturn(Optional.of(oldEntity));

        service.updateMessage(TEST_MESSAGE_ID, validDTO);
        assertThat(oldEntity.getMessageContent()).isEqualTo(validDTO.getMessageContent());
        assertThat(oldEntity.getAuthorId()).isEqualTo(validDTO.getAuthorId());// confirm that old entity was mutated with new dto data

        verify(repository).findByIdOptional(TEST_MESSAGE_ID);
        verifyNoMoreInteractions(repository);

    }

    @Test
    void updateMessage_doesNotExist() {

        when(repository.findByIdOptional(TEST_MESSAGE_ID)).thenReturn(Optional.empty());

        assertThrows(MessageNotFoundException.class, () -> service.updateMessage(TEST_MESSAGE_ID, validDTO));

        verify(repository).findByIdOptional(TEST_MESSAGE_ID);
        verifyNoMoreInteractions(repository);
    }


    @Test
    void deleteMessage_success() {

        when(repository.deleteById(TEST_MESSAGE_ID)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteMessage(TEST_MESSAGE_ID));

        verify(repository).deleteById(TEST_MESSAGE_ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteMessage_doesNotExist_notFound() {

        when(repository.deleteById(TEST_MESSAGE_ID)).thenReturn(false);

        assertThrows(MessageNotFoundException.class, () -> service.deleteMessage(TEST_MESSAGE_ID));

        verify(repository).deleteById(TEST_MESSAGE_ID);
        verifyNoMoreInteractions(repository);
    }
}
