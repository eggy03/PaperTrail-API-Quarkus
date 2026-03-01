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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

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

    static final Long TEST_GUILD_ID = 1302148573926148096L;
    static final Long TEST_CHANNEL_ID = 1302148573926148097L;

    static final Long NEGATIVE_TEST_GUILD_ID = -1302148573926148096L;
    static final Long NEGATIVE_TEST_CHANNEL_ID = -1302148573926148097L;

    // prep a valid Entity
    final AuditLogRegistration validEntity = new AuditLogRegistration(TEST_GUILD_ID, TEST_CHANNEL_ID);
    // prep a valid DTO
    final AuditLogRegistrationDTO validDTO = new AuditLogRegistrationDTO(TEST_GUILD_ID, TEST_CHANNEL_ID);

    // prep a stream of invalid DTOs
    public static Stream<AuditLogRegistrationDTO> invalidDTOs() {

        AuditLogRegistrationDTO nullBodyDTO = new AuditLogRegistrationDTO(null, null);
        AuditLogRegistrationDTO nullGuildIdDTO = new AuditLogRegistrationDTO(null, TEST_CHANNEL_ID);
        AuditLogRegistrationDTO nullChannelIdDTO = new AuditLogRegistrationDTO(TEST_GUILD_ID, null);

        AuditLogRegistrationDTO negativeGuildIdDTO = new AuditLogRegistrationDTO(NEGATIVE_TEST_GUILD_ID, TEST_CHANNEL_ID);
        AuditLogRegistrationDTO negativeChannelIdDTO = new AuditLogRegistrationDTO(TEST_GUILD_ID, NEGATIVE_TEST_CHANNEL_ID);

        return Stream.of(nullBodyDTO, nullGuildIdDTO, nullChannelIdDTO, negativeGuildIdDTO, negativeChannelIdDTO);
    }

    @BeforeEach
    void cleanState() {
        QuarkusTransaction.requiringNew().run(repository::deleteAll);
        redisDataSource.flushall();
    }

    @Test
    void registerGuild_success() {

        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .body("guildId", is(TEST_GUILD_ID))
                .body("channelId", is(TEST_CHANNEL_ID));

        // assert that save was a success
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(TEST_GUILD_ID));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(AuditLogRegistration::getGuildId, AuditLogRegistration::getChannelId)
                .containsExactly(TEST_GUILD_ID, TEST_CHANNEL_ID);
    }

    @Test
    void registerGuild_alreadyExists_conflicts() {

        // register once, expect success
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .body("guildId", is(TEST_GUILD_ID))
                .body("channelId", is(TEST_CHANNEL_ID));

        // register again, expect 409 conflict
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(409);

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void registerGuild_validationFails_badRequest(AuditLogRegistrationDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(TEST_GUILD_ID));

        Optional<AuditLogRegistration> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(NEGATIVE_TEST_GUILD_ID));

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();
    }

    @Test
    void registerGuild_deserializationFails_badRequest() {

        given().contentType("application/json").body("\"text\"")
                .when().post(BASE_PATH)
                .then().statusCode(400);

    }

    @Test
    void getGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // view registered guild - expect success
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + TEST_GUILD_ID)
                .then().statusCode(200)
                .body("guildId", is(TEST_GUILD_ID))
                .body("channelId", is(TEST_CHANNEL_ID));

    }

    @Test
    void getGuild_notRegistered_notFound() {

        // view un-registered guild - expect not found
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + TEST_GUILD_ID)
                .then().statusCode(404);

    }

    @Test
    void getGuild_invalidParameters() {

        given().contentType("application/json")
                .when().get(BASE_PATH + "/notALong")
                .then().statusCode(404);

        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + NEGATIVE_TEST_GUILD_ID)
                .then().statusCode(400);

    }

    @Test
    void updateGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // update
        AuditLogRegistrationDTO dto = new AuditLogRegistrationDTO(TEST_GUILD_ID, 1302148579426154496L);

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(200)
                .body("guildId", is(TEST_GUILD_ID))
                .body("channelId", is(1302148579426154496L));

        // verify update
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(TEST_GUILD_ID));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(AuditLogRegistration::getGuildId, AuditLogRegistration::getChannelId)
                .containsExactly(TEST_GUILD_ID, 1302148579426154496L);

    }

    @Test
    void updateGuild_doesNotExist() {

        // update without registering
        given().contentType("application/json").body(validDTO)
                .when().put(BASE_PATH)
                .then().statusCode(404);

        // verify update didn't register a new guild
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(TEST_GUILD_ID));

        assertThat(entityOptional).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void updateGuild_validationFails_badRequest(AuditLogRegistrationDTO dto) {

        given().contentType("application/json").body(dto)
                .when().put(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was updated
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(TEST_GUILD_ID));

        Optional<AuditLogRegistration> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(NEGATIVE_TEST_GUILD_ID));

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
    void deleteGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + TEST_GUILD_ID)
                .then().statusCode(204);

        // verify deletion
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(TEST_GUILD_ID));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteGuild_doesNotExist_notFound() {

        // attempt delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + TEST_GUILD_ID)
                .then().statusCode(404);

        // verify guild actually does not exist
        Optional<AuditLogRegistration> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(TEST_GUILD_ID));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteGuild_invalidParameters() {
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/notALong")
                .then().statusCode(404);

        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + NEGATIVE_TEST_GUILD_ID)
                .then().statusCode(400);

    }

}
