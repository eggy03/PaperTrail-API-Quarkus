package io.github.eggy03.papertrail.api.exceptions.mapper;

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
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception e) {

        log.error(AnsiColor.RED + "An internal server error has occurred" + AnsiColor.RESET, e);

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                LocalDateTime.now(),
                uriInfo.getPath()
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
