package io.github.eggy03.papertrail.api.exceptions.mapper;

import io.github.eggy03.papertrail.api.exceptions.GuildRegistrationException;
import io.github.eggy03.papertrail.api.exceptions.entity.ErrorResponse;
import io.github.eggy03.papertrail.api.util.AnsiColor;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Provider
@Slf4j
public class GuildRegistrationExceptionMapper implements ExceptionMapper<GuildRegistrationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(GuildRegistrationException e) {

        log.debug(AnsiColor.MAGENTA + "{}" + AnsiColor.RESET, e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.CONFLICT.getStatusCode(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                LocalDateTime.now(),
                uriInfo.getPath()
        );

        return Response
                .status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}
