package integration;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.entity.MessageLogContent;
import io.github.eggy03.papertrail.api.repository.MessageLogContentRepository;
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
class MessageLogContentTest {

    private static final String BASE_PATH = "/api/v1/content/message";

    @Inject
    MessageLogContentRepository repository;

    // RedisDataSource while not annotated for CDI, does get injected because Quarkus handles this synthetic bean
    // Or IntelliJ does not see the dependencies
    // see https://github.com/quarkiverse/quarkus-minio/issues/413 and https://github.com/quarkusio/quarkus/discussions/25120
    @Inject
    RedisDataSource redisDataSource;
    // prep a valid Entity
    MessageLogContent validEntity = new MessageLogContent(123L, "message", 456L, null);
    // prep a valid DTO
    MessageLogContentDTO validDTO = new MessageLogContentDTO(123L, "message", 456L);

    // prep a stream of invalid DTOs
    public static Stream<MessageLogContentDTO> invalidDTOs() {

        MessageLogContentDTO nullBodyDTO = new MessageLogContentDTO(null, null, null);
        MessageLogContentDTO nullMessageIdDTO = new MessageLogContentDTO(null, "message", 456L);
        MessageLogContentDTO nullMessageContentDTO = new MessageLogContentDTO(123L, null, 456L);
        MessageLogContentDTO nullAuthorIdDTO = new MessageLogContentDTO(123L, "message", null);

        MessageLogContentDTO negativeMessageIdDTO = new MessageLogContentDTO(-123L, "message", 456L);
        MessageLogContentDTO negativeAuthorIdDTO = new MessageLogContentDTO(123L, "message", -456L);

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
                .then().statusCode(201)
                .body("messageId", is(123))
                .body("messageContent", is("message"))
                .body("authorId", is(456));

        // assert that save was a success
        Optional<MessageLogContent> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(MessageLogContent::getMessageId, MessageLogContent::getMessageContent, MessageLogContent::getAuthorId)
                .containsExactly(123L, "message", 456L);
    }

    @Test
    void saveMessage_exists_conflicts() {

        // save once, expect success
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .body("messageId", is(123))
                .body("messageContent", is("message"))
                .body("authorId", is(456));

        // save again, expect 409 conflict
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(409);

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void saveMessage_validationFails_badRequest(MessageLogContentDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<MessageLogContent> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional).isEmpty();
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
                .when().get(BASE_PATH + "/123")
                .then().statusCode(200)
                .body("messageId", is(123))
                .body("messageContent", is("message"))
                .body("authorId", is(456));

    }

    @Test
    void getMessage_notSaved_notFound() {

        given().contentType("application/json")
                .when().get(BASE_PATH + "/123")
                .then().statusCode(404);

    }

    @Test
    void getMessage_invalidParameters() {

        given().contentType("application/json")
                .when().get(BASE_PATH + "/notALong")
                .then().statusCode(404);

        given().contentType("application/json")
                .when().get(BASE_PATH + "/-123")
                .then().statusCode(400);

    }

    @Test
    void updateMessage_success() {

        // save message
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // create an updated DTO
        MessageLogContentDTO dto = new MessageLogContentDTO(123L, "updatedMessage", 457L);

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(200)
                .body("messageId", is(123))
                .body("messageContent", is("updatedMessage"))
                .body("authorId", is(457));

        // verify update
        Optional<MessageLogContent> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(MessageLogContent::getMessageId, MessageLogContent::getMessageContent, MessageLogContent::getAuthorId)
                .containsExactly(123L, "updatedMessage", 457L);

    }

    @Test
    void updateMessage_doesNotExist_notFound() {

        // update without saving first
        given().contentType("application/json").body(validDTO)
                .when().put(BASE_PATH)
                .then().statusCode(404);

        // verify update didn't register a new guild
        Optional<MessageLogContent> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void updateGuild_validationFails_badRequest(MessageLogContentDTO dto) {

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // verify that updates were not applied
        Optional<MessageLogContent> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        Optional<MessageLogContent> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(-123L));

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();
    }

    @Test
    void updateGuild_deserializationFails_badRequest() {

        given().contentType("application/json").body("\"text\"")
                .when().put(BASE_PATH)
                .then().statusCode(400);

    }

    @Test
    void deleteMessage_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/123")
                .then().statusCode(204);

        // verify deletion
        Optional<MessageLogContent> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteMessage_doesNotExist_notFound() {

        // attempt delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/123")
                .then().statusCode(404);

        // verify guild actually does not exist
        Optional<MessageLogContent> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteMessage_invalidParameters() {

        given().contentType("application/json")
                .when().delete(BASE_PATH + "/notALong")
                .then().statusCode(404);

        given().contentType("application/json")
                .when().delete(BASE_PATH + "/-123")
                .then().statusCode(400);

    }

}
