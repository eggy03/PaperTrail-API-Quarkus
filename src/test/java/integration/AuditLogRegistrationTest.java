package integration;

import io.github.eggy03.papertrail.api.dto.AuditLogRegistrationDTO;
import io.github.eggy03.papertrail.api.entity.AuditLogRegistration;
import io.github.eggy03.papertrail.api.repository.AuditLogRegistrationRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class AuditLogRegistrationTest {

    private static final String BASE_PATH = "/api/v1/log/audit";

    @Inject
    AuditLogRegistrationRepository repository;

    // RedisDataSource while not annotated for CDI, does get injected because Quarkus handles this synthetic bean
    // Or IntelliJ does not see the dependencies
    // see https://github.com/quarkiverse/quarkus-minio/issues/413 and https://github.com/quarkusio/quarkus/discussions/25120
    @Inject
    RedisDataSource redisDataSource;

    @BeforeEach
    void cleanState() {
        QuarkusTransaction.requiringNew().run(repository::deleteAll);
        redisDataSource.flushall();
    }

    @Test
    void registerGuild_success() {

        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();
        dto.setGuildId(123L);
        dto.setChannelId(456L);

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .body("guildId", is(123))
                .body("channelId", is(456));

        // assert that save was a success
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(AuditLogRegistration::getGuildId, AuditLogRegistration::getChannelId)
                .containsExactly(123L, 456L);
    }

    @Test
    void registerGuild_exists_conflicts() {

        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();
        dto.setGuildId(123L);
        dto.setChannelId(456L);

        // register once, expect success
        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .body("guildId", is(123))
                .body("channelId", is(456));

        // register again, expect 409 conflict
        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(409);

    }

    @Test
    void registerGuild_nullBody_badRequest() {

        // null guild and channel id
        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // positive guild id but null channel id
        dto.setGuildId(123L);

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // null guild id but positive channel id
        dto.setGuildId(null);
        dto.setChannelId(456L);

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isEmpty();
    }

    @Test
    void registerGuild_malformedBody_badRequest() {

        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();
        // negative guild id and channel id
        dto.setGuildId(-123L);
        dto.setChannelId(-456L);

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // positive guild id but negative channel id
        dto.setGuildId(123L);
        dto.setChannelId(-456L);

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // negative guild id but positive channel id
        dto.setGuildId(-123L);
        dto.setChannelId(456L);

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // invalid json
        given().contentType("application/json").body("\"text\"")
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        Optional<AuditLogRegistration> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(-123L));

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();

    }

    @Test
    void getGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(new AuditLogRegistration(123L, 456L)));

        // view registered guild - expect success
        given().contentType("application/json")
                .when().get(BASE_PATH + "/123")
                .then().statusCode(200)
                .body("guildId", is(123))
                .body("channelId", is(456));

    }

    @Test
    void getGuild_notRegistered_notFound() {

        // view un-registered guild - expect not found
        given().contentType("application/json")
                .when().get(BASE_PATH + "/123")
                .then().statusCode(404);

    }

    @Test
    void getGuild_notALong_notFound() {

        // view un-registered guild - expect not found
        given().contentType("application/json")
                .when().get(BASE_PATH + "/notALong")
                .then().statusCode(404);

    }

    @Test
    void getGuild_invalidParameter_badRequest() {

        // negative long
        given().contentType("application/json")
                .when().get(BASE_PATH + "/-123")
                .then().statusCode(400);

    }

    @Test
    void updateGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(new AuditLogRegistration(123L, 456L)));

        // update
        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();
        dto.setGuildId(123L);
        dto.setChannelId(457L);

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(200)
                .body("guildId", is(123))
                .body("channelId", is(457));

        // verify update
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(AuditLogRegistration::getGuildId, AuditLogRegistration::getChannelId)
                .containsExactly(123L, 457L);

    }

    @Test
    void updateGuild_doesNotExist() {

        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();
        dto.setGuildId(123L);
        dto.setChannelId(456L);

        // update without registering
        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(404);

        // verify update didn't register a new guild
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isEmpty();

    }

    @Test
    void updateGuild_nullBody_badRequest() {

        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();

        // null guild and channel id
        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // null guild id
        dto.setChannelId(456L);
        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // null channel id
        dto.setGuildId(123L);
        dto.setChannelId(null);
        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);


        // verify that updates were not applied
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional).isEmpty();
    }

    @Test
    void updateGuild_malformedBody_badRequest() {

        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO();
        // negative guild id and channel id
        dto.setGuildId(-123L);
        dto.setChannelId(-456L);

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // positive guild id but negative channel id
        dto.setGuildId(123L);
        dto.setChannelId(-456L);

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // negative guild id but positive channel id
        dto.setGuildId(-123L);
        dto.setChannelId(456L);

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // invalid json
        given().contentType("application/json").body("\"text\"")
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was updated
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        Optional<AuditLogRegistration> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(-123L));

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();
    }

    @Test
    void deleteGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(new AuditLogRegistration(123L, 456L)));

        // delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/123")
                .then().statusCode(204);

        // verify deletion
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isEmpty();

    }

    @Test
    void deleteGuild_doesNotExist_notFound() {

        // attempt delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/123")
                .then().statusCode(404);

        // verify guild actually does not exist
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(123L));

        assertThat(entityOptional)
                .isEmpty();

    }

    @Test
    void deleteGuild_notALong_notFound() {

        // view un-registered guild - expect not found
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/notALong")
                .then().statusCode(404);

    }

    @Test
    void deleteGuild_invalidParameter_badRequest() {

        // negative long
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/-123")
                .then().statusCode(400);

    }

}
