package io.github.eggy03.papertrail.api.controller;

import io.github.eggy03.papertrail.api.dto.MessageDTO;
import io.github.eggy03.papertrail.api.service.MessageService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.persistence.OptimisticLockException;
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
import org.eclipse.microprofile.faulttolerance.Retry;

@Path("/api/v2/message")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class MessageController {

    private final MessageService service;

    @POST
    @Blocking
    @RunOnVirtualThread
    public Response saveMessage(@Valid MessageDTO dto) {

        service.saveMessage(dto);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{messageId}")
    @Blocking
    @RunOnVirtualThread
    public Response getMessage(@PathParam("messageId") @Positive @NotNull Long messageId) {
        return Response
                .ok(service.getMessage(messageId))
                .build();
    }

    @PATCH
    @Blocking
    @RunOnVirtualThread
    @Retry(retryOn = OptimisticLockException.class)
    public Response updateMessage(@Valid MessageDTO dto) {
        service.updateMessage(dto.getMessageId(), dto);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{messageId}")
    @Blocking
    @RunOnVirtualThread
    public Response deleteMessage(@PathParam("messageId") @Positive @NotNull Long messageId) {
        service.deleteMessage(messageId);
        return Response.noContent().build();
    }

}
