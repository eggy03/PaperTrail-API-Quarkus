package io.github.eggy03.controller;

import io.github.eggy03.dto.AuditLogRegistrationDTO;
import io.github.eggy03.service.AuditLogRegistrationService;
import jakarta.validation.Valid;
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

@Path("api/v1/log/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class AuditLogRegistrationController {

    private final AuditLogRegistrationService service;

    @POST
    public Response registerGuild (@Valid AuditLogRegistrationDTO dto) {
        return Response
                .status(Response.Status.CREATED)
                .entity(service.registerGuild(dto))
                .build();
    }

    @GET
    @Path("/{guildId}")
    public Response getGuild (@PathParam("guildId") @Positive Long guildId) {
        return Response
                .ok(service.viewRegisteredGuild(guildId))
                .build();
    }

    @PUT
    public Response updateGuild (@Valid AuditLogRegistrationDTO dto) {
        return Response
                .ok(service.updateRegisteredGuild(dto))
                .build();
    }

    @DELETE
    @Path("/{guildId}")
    public Response deleteGuild (@PathParam("guildId") @Positive Long guildId) {
        service.deleteRegisteredGuild(guildId);
        return Response.noContent().build();
    }

}
