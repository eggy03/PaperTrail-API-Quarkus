package io.github.eggy03.papertrail.api.controller;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.service.MessageLogContentService;
import io.github.eggy03.papertrail.api.service.locks.MessageLogContentLockingService;
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

@Path("/api/v1/content/message")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
@RequiredArgsConstructor
public class MessageLogContentController {

    private final MessageLogContentLockingService lockingService; // accessing the MessageLogContentService behind Redisson locks
    private final MessageLogContentService service; // direct access

    @POST
    public Response saveMessage (@Valid MessageLogContentDTO dto) {
        return Response
                .status(Response.Status.CREATED)
                .entity(lockingService.saveMessage(dto))
                .build();
    }

    @GET
    @Path("/{messageId}")
    public Response getMessage(@PathParam("messageId") @Positive @NotNull Long messageId) {
        return Response
                .ok(service.getMessage(messageId))
                .build();
    }

    @PUT
    public Response updateMessage (@Valid MessageLogContentDTO dto) {
        return Response
                .ok(lockingService.updateMessage(dto))
                .build();
    }

    @DELETE
    @Path("/{messageId}")
    public Response deleteMessage(@PathParam("messageId") @Positive @NotNull Long messageId) {
        lockingService.deleteMessage(messageId);
        return Response.noContent().build();
    }

}
