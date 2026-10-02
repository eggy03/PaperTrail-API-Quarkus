package integration;

import io.github.eggy03.papertrail.api.dto.GuildDTO;
import io.github.eggy03.papertrail.api.entity.Guild;
import io.github.eggy03.papertrail.api.repository.GuildRepository;
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
class GuildServiceIntegrationTest {

    static final Long GUILD_ID = 1302148573926148096L;
    static final Long GUILD_EVENT_CHANNEL_ID = 1302148573926148097L;
    static final Long MEMBER_EVENT_CHANNEL_ID = 1302148573926148098L;
    static final Long MESSAGE_EVENT_CHANNEL_ID = 1302148573926148099L;
    static final Long NEGATIVE_GUILD_ID = -1302148573926148096L;
    static final Long NEGATIVE_GUILD_EVENT_CHANNEL_ID = -1302148573926148097L;
    static final Long NEGATIVE_MEMBER_EVENT_CHANNEL_ID = -1302148573926148098L;
    static final Long NEGATIVE_MESSAGE_EVENT_CHANNEL_ID = -1302148573926148099L;
    private static final String BASE_PATH = "/api/v2/guild";
    // prep a valid Entity
    final Guild validEntity = new Guild(GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
    // prep a valid DTO
    final GuildDTO validDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
    @Inject
    GuildRepository repository;
    // RedisDataSource while not annotated for CDI, does get injected because Quarkus handles this synthetic bean
    // Or IntelliJ does not see the dependencies
    // see https://github.com/quarkiverse/quarkus-minio/issues/413 and https://github.com/quarkusio/quarkus/discussions/25120
    @Inject
    RedisDataSource redisDataSource;

    // prep a stream of invalid DTOs
    public static Stream<GuildDTO> invalidDTOs() {

        GuildDTO nullGuildIdDTO = new GuildDTO(null, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);

        GuildDTO negativeGuildIdDTO = new GuildDTO(NEGATIVE_GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO negativeGuildEventChannelIdDTO = new GuildDTO(GUILD_ID, NEGATIVE_GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO negativeMemberEventChannelIdDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, NEGATIVE_MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO negativeMessageEventChannelIdDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, NEGATIVE_MESSAGE_EVENT_CHANNEL_ID);

        return Stream.of(nullGuildIdDTO, negativeGuildIdDTO, negativeGuildEventChannelIdDTO, negativeMemberEventChannelIdDTO, negativeMessageEventChannelIdDTO);
    }

    @BeforeEach
    void cleanState() {
        QuarkusTransaction.requiringNew().run(repository::deleteAll);
        redisDataSource.flushall();
    }

    @Test
    void saveGuild_success() {

        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // assert that save was a success
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(GUILD_ID));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Guild::getGuildId, Guild::getGuildEventChannelId, Guild::getMemberEventChannelId, Guild::getMessageEventChannelId)
                .containsExactly(GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
    }

    @Test
    void saveGuild_alreadyExists_conflicts() {

        // register once, expect success
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // register again, expect 409 conflict
        given().contentType("application/json").body(validDTO)
                .when().post(BASE_PATH)
                .then().statusCode(409);

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void saveGuild_validationFails_badRequest(GuildDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(GUILD_ID));

        Optional<Guild> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(NEGATIVE_GUILD_ID));

        assertThat(entityOptional).isEmpty();
        assertThat(entityOptionalTwo).isEmpty();
    }

    @Test
    void saveGuild_deserializationFails_badRequest() {

        given().contentType("application/json").body("\"text\"")
                .when().post(BASE_PATH)
                .then().statusCode(400);

    }

    @Test
    void viewGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // view registered guild - expect success
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + GUILD_ID)
                .then().statusCode(200)
                .body("guildId", is(GUILD_ID))
                .body("guildEventChannelId", is(GUILD_EVENT_CHANNEL_ID))
                .body("memberEventChannelId", is(MEMBER_EVENT_CHANNEL_ID))
                .body("messageEventChannelId", is(MESSAGE_EVENT_CHANNEL_ID));

    }

    @Test
    void viewGuild_notRegistered_notFound() {

        // view un-registered guild - expect not found
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + GUILD_ID)
                .then().statusCode(404);

    }

    @Test
    void viewGuild_invalidParameters() {

        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + NEGATIVE_GUILD_ID)
                .then().statusCode(400);

    }

    @Test
    void updateGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // update
        GuildDTO dto = new GuildDTO(GUILD_ID, 123L, 456L, 789L);

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(204);

        // verify update
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(GUILD_ID));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Guild::getGuildId, Guild::getGuildEventChannelId, Guild::getMemberEventChannelId, Guild::getMessageEventChannelId)
                .containsExactly(GUILD_ID, 123L, 456L, 789L);

    }

    @Test
    void updateGuild_doesNotExist() {

        // update without registering
        given().contentType("application/json").body(validDTO)
                .when().patch(BASE_PATH)
                .then().statusCode(404);

        // verify update didn't register a new guild
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(GUILD_ID));

        assertThat(entityOptional).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("invalidDTOs")
    void updateGuild_validationFails_badRequest(GuildDTO dto) {

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was updated
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(GUILD_ID));

        Optional<Guild> entityOptionalTwo = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(NEGATIVE_GUILD_ID));

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
    void deleteGuild_success() {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(validEntity));

        // delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + GUILD_ID)
                .then().statusCode(204);

        // verify deletion
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(GUILD_ID));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteGuild_doesNotExist_notFound() {

        // attempt delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + GUILD_ID)
                .then().statusCode(404);

        // verify guild actually does not exist
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(GUILD_ID));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteGuild_invalidParameters() {

        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + NEGATIVE_GUILD_ID)
                .then().statusCode(400);

    }

}
