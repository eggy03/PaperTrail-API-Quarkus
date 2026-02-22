package io.github.eggy03.papertrail.api.configuration;

import io.github.eggy03.papertrail.api.util.AnsiColor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class HttpLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME = "start-time";

    private String colorMethod(String method) {
        return switch (method) {
            case "GET" -> AnsiColor.BLUE;
            case "POST" -> AnsiColor.GREEN;
            case "PUT" -> AnsiColor.YELLOW;
            case "DELETE" -> AnsiColor.RED;
            case null, default -> AnsiColor.RESET;
        };
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        containerRequestContext.setProperty(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {

        Object requestStartTime = containerRequestContext.getProperty(START_TIME);
        if (requestStartTime == null)
            return;

        long elapsedTime = System.currentTimeMillis() - (long) requestStartTime;

        String requestMethod = containerRequestContext.getMethod();
        String requestURI = containerRequestContext.getUriInfo().getRequestUri().getPath();
        int responseStatus = containerResponseContext.getStatus();

        log.info("{}{}{} {} -> {} ({} ms)",
                colorMethod(requestMethod),
                requestMethod,
                AnsiColor.RESET,
                requestURI,
                responseStatus,
                elapsedTime);
    }
}
