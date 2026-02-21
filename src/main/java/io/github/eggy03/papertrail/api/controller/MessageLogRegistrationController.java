package io.github.eggy03.papertrail.api.controller;

import io.github.eggy03.papertrail.api.dto.MessageLogRegistrationDTO;
import io.github.eggy03.papertrail.api.service.MessageLogRegistrationService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("api/v1/log/message")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
@RequiredArgsConstructor
public class MessageLogRegistrationController {

    private final MessageLogRegistrationService service;

    @POST
    public Response registerGuild(@Valid MessageLogRegistrationDTO dto) {
        return Response
                .status(Response.Status.CREATED)
                .entity(service.registerGuild(dto))
                .build();
    }

    @GET
    @Path("/{guildId}")
    public Response getGuild(@PathParam("guildId") @Positive @NotNull Long guildId) {
        return Response
                .ok(service.viewRegisteredGuild(guildId))
                .build();
    }

    @PUT
    public Response updateGuild(@Valid MessageLogRegistrationDTO dto) {
        return Response
                .ok(service.updateRegisteredGuild(dto))
                .build();
    }

    @DELETE
    @Path("/{guildId}")
    public Response deleteGuild(@PathParam("guildId") @Positive @NotNull Long guildId) {
        service.deleteRegisteredGuild(guildId);
        return Response.noContent().build();
    }

}
