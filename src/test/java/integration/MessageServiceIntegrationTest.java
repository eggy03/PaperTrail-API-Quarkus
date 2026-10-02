package integration;

import io.github.eggy03.papertrail.api.dto.MessageDTO;
import io.github.eggy03.papertrail.api.entity.Message;
import io.github.eggy03.papertrail.api.repository.MessageRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class MessageServiceIntegrationTest {

    static final Long MESSAGE_ID = 1302148573926148096L;
    static final String MESSAGE_CONTENT = "message";
    static final Long AUTHOR_ID = 1302148573926148097L;
    static final Long NEGATIVE_MESSAGE_ID = -1302148573926148096L;
    static final Long NEGATIVE_AUTHOR_ID = -1302148573926148097L;
    private static final String BASE_PATH = "/api/v2/message";
    // prep a valid Entity
    final Message validEntity = new Message(MESSAGE_ID, MESSAGE_CONTENT, AUTHOR_ID, null, null);
    // prep a valid DTO
    final MessageDTO validDTO = new MessageDTO(MESSAGE_ID, MESSAGE_CONTENT, AUTHOR_ID);
    @Inject
    MessageRepository repository;
    // RedisDataSource while not annotated for CDI, does get injected because Quarkus handles this synthetic bean
    // Or IntelliJ does not see the dependencies
    // see https://github.com/quarkiverse/quarkus-minio/issues/413 and https://github.com/quarkusio/quarkus/discussions/25120
    @Inject
    RedisDataSource redisDataSource;

    // prep a stream of invalid DTOs
    public static Stream<MessageDTO> invalidDTOs() {

        MessageDTO nullBodyDTO = new MessageDTO(null, null, null);
        MessageDTO nullMessageIdDTO = new MessageDTO(null, MESSAGE_CONTENT, AUTHOR_ID);
        MessageDTO nullMessageContentDTO = new MessageDTO(MESSAGE_ID, null, AUTHOR_ID);
        MessageDTO nullAuthorIdDTO = new MessageDTO(MESSAGE_ID, MESSAGE_CONTENT, null);

        MessageDTO negativeMessageIdDTO = new MessageDTO(NEGATIVE_MESSAGE_ID, MESSAGE_CONTENT, AUTHOR_ID);
        MessageDTO negativeAuthorIdDTO = new MessageDTO(MESSAGE_ID, MESSAGE_CONTENT, NEGATIVE_AUTHOR_ID);

        return Stream.of(nullBodyDTO, nullMessageIdDTO, nullMessageContentDTO, nullAuthorIdDTO, negativeMessageIdDTO, negativeAuthorIdDTO);
    }

    @BeforeEach
    void cleanState() {
        QuarkusTransaction.requiringNew().run(repository::deleteAll);
        redisDataSource.flushall();
    }

    @Test
    void saveMessage_success() {

        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // assert that save was a success
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(MESSAGE_ID));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Message::getMessageId, Message::getMessageContent, Message::getAuthorId)
                .containsExactly(MESSAGE_ID, MESSAGE_CONTENT, AUTHOR_ID);
    }

    @Test
    void saveMessage_alreadyExists_conflicts() {

        // save once, expect success
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // save again, expect 409 conflict
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(409);

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void saveMessage_validationFails_badRequest(MessageDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(MESSAGE_ID));

        Optional<Message> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(NEGATIVE_MESSAGE_ID));

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();
    }

    @Test
    void saveMessage_deserializationFails_badRequest() {

        given().contentType("application/json").body("\"text\"")
                .when().post(BASE_PATH)
                .then().statusCode(400);

    }

    @Test
    void getMessage_success() {

        // save to db
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // view message - expect success
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + MESSAGE_ID)
                .then().statusCode(200)
                .body("messageId", is(MESSAGE_ID))
                .body("messageContent", is(MESSAGE_CONTENT))
                .body("authorId", is(AUTHOR_ID));

    }

    @Test
    void getMessage_notSaved_notFound() {

        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + MESSAGE_ID)
                .then().statusCode(404);

    }

    @Test
    void getMessage_invalidParameters() {

        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + NEGATIVE_MESSAGE_ID)
                .then().statusCode(400);

    }

    @Test
    void updateMessage_success() {

        // save message
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // create an updated DTO
        MessageDTO dto = new MessageDTO(MESSAGE_ID, "updatedMessage", AUTHOR_ID);

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(204);

        // verify update
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(MESSAGE_ID));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Message::getMessageId, Message::getMessageContent, Message::getAuthorId)
                .containsExactly(MESSAGE_ID, "updatedMessage", AUTHOR_ID);

    }

    @Test
    void updateMessage_doesNotExist_notFound() {

        // update without saving first
        given().contentType("application/json").body(validDTO)
                .when().patch(BASE_PATH)
                .then().statusCode(404);

        // verify update didn't register a new guild
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(MESSAGE_ID));

        assertThat(entityOptional).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void updateGuild_validationFails_badRequest(MessageDTO dto) {

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(400);

        // verify that updates were not applied
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(MESSAGE_ID));

        Optional<Message> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(NEGATIVE_MESSAGE_ID));

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();
    }

    @Test
    void updateGuild_deserializationFails_badRequest() {

        given().contentType("application/json").body("\"text\"")
                .when().patch(BASE_PATH)
                .then().statusCode(400);

    }

    @Test
    void deleteMessage_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + MESSAGE_ID)
                .then().statusCode(204);

        // verify deletion
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(MESSAGE_ID));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteMessage_doesNotExist_notFound() {

        // attempt delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + MESSAGE_ID)
                .then().statusCode(404);

        // verify guild actually does not exist
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(MESSAGE_ID));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteMessage_invalidParameters() {

        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + NEGATIVE_MESSAGE_ID)
                .then().statusCode(400);

    }

}
