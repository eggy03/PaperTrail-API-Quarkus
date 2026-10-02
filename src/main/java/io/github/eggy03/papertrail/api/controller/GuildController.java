package io.github.eggy03.papertrail.api.controller;

import io.github.eggy03.papertrail.api.dto.GuildDTO;
import io.github.eggy03.papertrail.api.service.GuildService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("api/v2/guild")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class GuildController {

    private final GuildService service;

    @POST
    @Blocking
    @RunOnVirtualThread
    public Response registerGuild(@Valid GuildDTO dto) {
        service.saveGuild(dto);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{guildId}")
    @Blocking
    @RunOnVirtualThread
    public Response getGuild(@PathParam("guildId") @Positive @NotNull Long guildId) {
        return Response
                .ok(service.viewGuild(guildId))
                .build();
    }

    @PATCH
    @Blocking
    @RunOnVirtualThread
    public Response updateGuild(@Valid GuildDTO dto) {
        service.updateGuild(dto.getGuildId(), dto);
        return Response
                .noContent()
                .build();
    }

    @DELETE
    @Path("/{guildId}")
    @Blocking
    @RunOnVirtualThread
    public Response deleteGuild(@PathParam("guildId") @Positive @NotNull Long guildId) {
        service.deleteGuild(guildId);
        return Response.noContent().build();
    }

}
