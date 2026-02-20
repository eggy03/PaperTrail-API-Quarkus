package io.github.eggy03.papertrail.api.exceptions.mapper;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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
public class InvalidFormatExceptionMapper implements ExceptionMapper<InvalidFormatException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(InvalidFormatException e) {

        log.debug(AnsiColor.MAGENTA + "{}" + AnsiColor.RESET, e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                LocalDateTime.now(),
                uriInfo.getPath()
        );

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
