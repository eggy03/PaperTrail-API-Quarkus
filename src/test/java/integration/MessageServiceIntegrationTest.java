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

    private static final String BASE_PATH = "/api/v2/message";
    // prep a valid Entity
    final Message sampleEntity = new Message(111111111111111L, "message-one", 999999999999999L, null, null);
    // prep a valid DTO
    final MessageDTO sampleDTO = new MessageDTO(111111111111111L, "message-one", 999999999999999L);

    @Inject
    MessageRepository repository;

    // RedisDataSource while not annotated for CDI, does get injected because Quarkus handles this synthetic bean
    // Or IntelliJ does not see the dependencies
    // see https://github.com/quarkiverse/quarkus-minio/issues/413 and https://github.com/quarkusio/quarkus/discussions/25120
    @Inject
    RedisDataSource redisDataSource;

    // prep a stream of negative DTOs
    public static Stream<MessageDTO> negativeDTOs() {

        final Long MESSAGE_ID = 111111111111111L;
        final String MESSAGE_CONTENT = "message-two";
        final Long AUTHOR_ID = 222222222222222L;

        final Long NEGATIVE_MESSAGE_ID = -111111111111111L;
        final Long NEGATIVE_AUTHOR_ID = -222222222222222L;

        MessageDTO negativeMessageIdDTO = new MessageDTO(NEGATIVE_MESSAGE_ID, MESSAGE_CONTENT, AUTHOR_ID);
        MessageDTO negativeAuthorIdDTO = new MessageDTO(MESSAGE_ID, MESSAGE_CONTENT, NEGATIVE_AUTHOR_ID);

        return Stream.of(negativeMessageIdDTO, negativeAuthorIdDTO);
    }

    // prep a stream of null DTOs
    public static Stream<MessageDTO> nullDTOs() {

        final Long MESSAGE_ID = 111111111111111L;
        final String MESSAGE_CONTENT = "message-two";
        final Long AUTHOR_ID = 222222222222222L;

        MessageDTO nullBodyDTO = new MessageDTO(null, null, null);
        MessageDTO nullMessageIdDTO = new MessageDTO(null, MESSAGE_CONTENT, AUTHOR_ID);
        MessageDTO nullMessageContentDTO = new MessageDTO(MESSAGE_ID, null, AUTHOR_ID);
        MessageDTO nullAuthorIdDTO = new MessageDTO(MESSAGE_ID, MESSAGE_CONTENT, null);

        return Stream.of(nullBodyDTO, nullMessageIdDTO, nullMessageContentDTO, nullAuthorIdDTO);
    }

    @BeforeEach
    void cleanState() {
        QuarkusTransaction.requiringNew().run(repository::deleteAll);
        redisDataSource.flushall();
    }

    @Test
    void saveMessage_success() {

        given().contentType("application/json").body(sampleDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // assert that save was a success
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(sampleDTO.getMessageId()));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Message::getMessageId, Message::getMessageContent, Message::getAuthorId)
                .containsExactly(sampleDTO.getMessageId(), sampleDTO.getMessageContent(), sampleDTO.getAuthorId());
    }

    @Test
    void saveMessage_alreadyExists_conflicts() {

        // save once, expect success
        given().contentType("application/json").body(sampleDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // save again, expect 409 conflict
        given().contentType("application/json").body(sampleDTO)
                .when().post(BASE_PATH)
                .then().statusCode(409);

    }

    @ParameterizedTest
    @MethodSource("negativeDTOs")
    void saveMessage_negativeDTOs_validationFails_badRequest(MessageDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(dto.getMessageId()));

        Optional<Message> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(-dto.getMessageId())); // test for negatives

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("nullDTOs")
    void saveMessage_nullDTOs_validationFails_badRequest(MessageDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        assertThat(repository.findAll().list()).isEmpty();
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
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(sampleEntity));

        // view message - expect success
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + sampleEntity.getMessageId())
                .then().statusCode(200)
                .body("messageId", is(sampleEntity.getMessageId()))
                .body("messageContent", is(sampleEntity.getMessageContent()))
                .body("authorId", is(sampleEntity.getAuthorId()));

    }

    @Test
    void getMessage_notSaved_notFound() {

        long nonExistentMessageId = 999999999999999L;

        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + nonExistentMessageId)
                .then().statusCode(404);

    }

    @Test
    void getMessage_invalidParameters() {

        long negativeMessageId = -999999999999999L;

        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + negativeMessageId)
                .then().statusCode(400);

    }

    @Test
    void updateMessage_success() {

        // save message
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(sampleEntity));

        // create an updated DTO
        MessageDTO dto = new MessageDTO(sampleEntity.getMessageId(), "updatedMessage", sampleEntity.getAuthorId() + 1);

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(204);

        // verify update
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(sampleEntity.getMessageId()));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Message::getMessageId, Message::getMessageContent, Message::getAuthorId)
                .containsExactly(sampleEntity.getMessageId(), "updatedMessage", sampleEntity.getAuthorId() + 1);

    }

    @Test
    void updateMessage_doesNotExist_notFound() {

        // update without saving first
        given().contentType("application/json").body(sampleDTO)
                .when().patch(BASE_PATH)
                .then().statusCode(404);

        // verify update didn't save a new message
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(sampleDTO.getMessageId()));

        assertThat(entityOptional).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("negativeDTOs")
    void updateMessage_negativeDTOs_validationFails_badRequest(MessageDTO dto) {

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(400);

        // verify that updates were not applied
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(dto.getMessageId()));

        assertThat(entityOptional).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("nullDTOs")
    void updateMessage_nullDTOs_validationFails_badRequest(MessageDTO dto) {

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(400);

        // verify that updates were not applied
        assertThat(repository.findAll().list()).isEmpty();
    }

    @Test
    void updateMessage_deserializationFails_badRequest() {

        given().contentType("application/json").body("\"text\"")
                .when().patch(BASE_PATH)
                .then().statusCode(400);

    }

    @Test
    void deleteMessage_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(sampleEntity));

        // delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + sampleEntity.getMessageId())
                .then().statusCode(204);

        // verify deletion
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(sampleEntity.getMessageId()));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteMessage_doesNotExist_notFound() {

        long nonExistentMessageId = 999999999999999L;

        // attempt delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + nonExistentMessageId)
                .then().statusCode(404);

        // verify message actually does not exist
        Optional<Message> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(nonExistentMessageId));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteMessage_invalidParameters() {

        long negativeMessageId = -999999999999999L;

        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + negativeMessageId)
                .then().statusCode(400);

    }

}
