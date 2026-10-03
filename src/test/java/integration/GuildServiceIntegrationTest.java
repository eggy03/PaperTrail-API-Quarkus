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

    private static final String BASE_PATH = "/api/v2/guild";

    // prep a sample Entity
    final Guild sampleEntity = new Guild(111111111111111L, 777777777777777L, 888888888888888L, 999999999999999L);
    // prep a sample DTO
    final GuildDTO sampleDTO = new GuildDTO(111111111111111L, 777777777777777L, 888888888888888L, 999999999999999L);

    @Inject
    GuildRepository repository;

    // RedisDataSource while not annotated for CDI, does get injected because Quarkus handles this synthetic bean
    // Or IntelliJ does not see the dependencies
    // see https://github.com/quarkiverse/quarkus-minio/issues/413 and https://github.com/quarkusio/quarkus/discussions/25120
    @Inject
    RedisDataSource redisDataSource;

    // prep a stream of valid DTOs
    public static Stream<GuildDTO> validDTOs() {

        final Long GUILD_ID = 111111111111111L;
        final Long GUILD_EVENT_CHANNEL_ID = 222222222222222L;
        final Long MEMBER_EVENT_CHANNEL_ID = 333333333333333L;
        final Long MESSAGE_EVENT_CHANNEL_ID = 444444444444444L;

        GuildDTO validDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);

        GuildDTO nullGuildEventChannelIdDTO = new GuildDTO(GUILD_ID, null, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO nullMemberEventChannelIdDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, null, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO nullMessageEventChannelIdDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, null);

        GuildDTO onlyGuildEventChannelIdDTO = new GuildDTO(GUILD_ID, null, null, GUILD_EVENT_CHANNEL_ID);
        GuildDTO onlyMemberEventChannelIdDTO = new GuildDTO(GUILD_ID, null, MEMBER_EVENT_CHANNEL_ID, null);
        GuildDTO onlyMessageEventChannelIdDTO = new GuildDTO(GUILD_ID, null, null, MESSAGE_EVENT_CHANNEL_ID);

        return Stream.of(validDTO,
                nullGuildEventChannelIdDTO, nullMemberEventChannelIdDTO, nullMessageEventChannelIdDTO,
                onlyGuildEventChannelIdDTO, onlyMemberEventChannelIdDTO, onlyMessageEventChannelIdDTO
        );
    }

    // prep a stream of negative DTOs
    public static Stream<GuildDTO> negativeDTOs() {

        final Long GUILD_ID = 111111111111111L;
        final Long GUILD_EVENT_CHANNEL_ID = 222222222222222L;
        final Long MEMBER_EVENT_CHANNEL_ID = 333333333333333L;
        final Long MESSAGE_EVENT_CHANNEL_ID = 444444444444444L;

        final Long NEGATIVE_GUILD_ID = -111111111111111L;
        final Long NEGATIVE_GUILD_EVENT_CHANNEL_ID = -222222222222222L;
        final Long NEGATIVE_MEMBER_EVENT_CHANNEL_ID = -333333333333333L;
        final Long NEGATIVE_MESSAGE_EVENT_CHANNEL_ID = -444444444444444L;

        GuildDTO negativeGuildIdDTO = new GuildDTO(NEGATIVE_GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO negativeGuildEventChannelIdDTO = new GuildDTO(GUILD_ID, NEGATIVE_GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO negativeMemberEventChannelIdDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, NEGATIVE_MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO negativeMessageEventChannelIdDTO = new GuildDTO(GUILD_ID, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, NEGATIVE_MESSAGE_EVENT_CHANNEL_ID);

        return Stream.of(
                negativeGuildIdDTO, negativeGuildEventChannelIdDTO,
                negativeMemberEventChannelIdDTO, negativeMessageEventChannelIdDTO
        );
    }

    // prep a stream of null DTOs
    public static Stream<GuildDTO> nullDTOs() {

        final Long GUILD_ID = 111111111111111L;
        final Long GUILD_EVENT_CHANNEL_ID = 222222222222222L;
        final Long MEMBER_EVENT_CHANNEL_ID = 333333333333333L;
        final Long MESSAGE_EVENT_CHANNEL_ID = 444444444444444L;

        GuildDTO nullGuildIdDTO = new GuildDTO(null, GUILD_EVENT_CHANNEL_ID, MEMBER_EVENT_CHANNEL_ID, MESSAGE_EVENT_CHANNEL_ID);
        GuildDTO allNullEventChannelIdDTO = new GuildDTO(GUILD_ID, null, null, null);

        return Stream.of(nullGuildIdDTO, allNullEventChannelIdDTO);
    }

    @BeforeEach
    void cleanState() {
        QuarkusTransaction.requiringNew().run(repository::deleteAll);
        redisDataSource.flushall();
    }

    @ParameterizedTest
    @MethodSource("validDTOs")
    void saveGuild_success(GuildDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // assert that save was a success
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(dto.getGuildId()));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Guild::getGuildId, Guild::getGuildEventChannelId, Guild::getMemberEventChannelId, Guild::getMessageEventChannelId)
                .containsExactly(dto.getGuildId(), dto.getGuildEventChannelId(), dto.getMemberEventChannelId(), dto.getMessageEventChannelId());
    }

    @ParameterizedTest
    @MethodSource("validDTOs")
    void saveGuild_alreadyExists_conflicts(GuildDTO dto) {

        // register once, expect success
        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(201);

        // register again, expect 409 conflict
        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(409);

    }

    @ParameterizedTest
    @MethodSource("negativeDTOs")
    void saveGuild_negativeDTOs_validationFails_badRequest(GuildDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(dto.getGuildId()));

        assertThat(entityOptional).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("nullDTOs")
    void saveGuild_nullDTOs_validationFails_badRequest(GuildDTO dto) {

        given().contentType("application/json").body(dto)
                .when().post(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        assertThat(repository.findAll().list()).isEmpty();
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
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(sampleEntity));

        // view registered guild - expect success
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + sampleEntity.getGuildId())
                .then().statusCode(200)
                .body("guildId", is(sampleEntity.getGuildId()))
                .body("guildEventChannelId", is(sampleEntity.getGuildEventChannelId()))
                .body("memberEventChannelId", is(sampleEntity.getMemberEventChannelId()))
                .body("messageEventChannelId", is(sampleEntity.getMessageEventChannelId()));

    }

    @Test
    void viewGuild_notRegistered_notFound() {

        long nonExistentGuildId = 999999999999999L;

        // view un-registered guild - expect not found
        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + nonExistentGuildId)
                .then().statusCode(404);

    }

    @Test
    void viewGuild_invalidParameters() {

        long negativeGuildId = -999999999999999L;

        given().contentType("application/json")
                .when().get(BASE_PATH + "/" + negativeGuildId)
                .then().statusCode(400);

    }

    @ParameterizedTest
    @MethodSource("validDTOs")
    void updateGuild_success(GuildDTO updatedDTO) {

        // register
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(sampleEntity));

        // update
        given().contentType("application/json").body(updatedDTO)
                .when().patch(BASE_PATH)
                .then().statusCode(204);

        // verify update
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(sampleEntity.getGuildId()));

        assertThat(entityOptional)
                .isPresent()
                .get()
                .extracting(Guild::getGuildId, Guild::getGuildEventChannelId, Guild::getMemberEventChannelId, Guild::getMessageEventChannelId)
                .containsExactly(sampleEntity.getGuildId(), updatedDTO.getGuildEventChannelId(), updatedDTO.getMemberEventChannelId(), updatedDTO.getMessageEventChannelId());

    }

    @Test
    void updateGuild_doesNotExist() {

        // update without registering
        given().contentType("application/json").body(sampleDTO)
                .when().patch(BASE_PATH)
                .then().statusCode(404);

        // verify update didn't register a new guild
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(sampleDTO.getGuildId()));

        assertThat(entityOptional).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("negativeDTOs")
    void updateGuild_negativeDTOs_validationFails_badRequest(GuildDTO dto) {

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was updated
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(dto.getGuildId()));

        assertThat(entityOptional).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("nullDTOs")
    void updateGuild_nullDTOs_validationFails_badRequest(GuildDTO dto) {

        given().contentType("application/json").body(dto)
                .when().patch(BASE_PATH)
                .then().statusCode(400);

        // assert that nothing was saved
        assertThat(repository.findAll().list()).isEmpty();
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
        QuarkusTransaction.requiringNew().run(() -> repository.persistAndFlush(sampleEntity));

        // delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + sampleEntity.getGuildId())
                .then().statusCode(204);

        // verify deletion
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(sampleEntity.getGuildId()));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteGuild_doesNotExist_notFound() {

        long nonExistentGuildId = 999999999999999L;

        // attempt delete
        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + nonExistentGuildId)
                .then().statusCode(404);

        // verify guild actually does not exist
        Optional<Guild> entityOptional = QuarkusTransaction
                .requiringNew()
                .call(() -> repository.findByIdOptional(nonExistentGuildId));

        assertThat(entityOptional).isEmpty();

    }

    @Test
    void deleteGuild_invalidParameters() {

        long negativeGuildId = -999999999999999L;

        given().contentType("application/json")
                .when().delete(BASE_PATH + "/" + negativeGuildId)
                .then().statusCode(400);

    }

}
