package io.github.eggy03.api.controller;

import io.github.eggy03.api.entity.SampleEntity;
import io.github.eggy03.api.service.SampleService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("sample")
@Consumes(MediaType.APPLICATION_JSON)
public class SampleController {

    private final SampleService service;

    public SampleController(SampleService service) {
        this.service = service;
    }

    @POST
    @Blocking
    @RunOnVirtualThread
    public Response saveSample(@Valid SampleEntity sampleEntity) {
        service.saveSample(sampleEntity);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{id}")
    @Blocking
    @RunOnVirtualThread
    public Response deleteSample(@PathParam("id") @Positive Long id) {
        if (service.deleteSample(id))
            return Response.noContent().build();
        else return Response.status(Response.Status.NOT_FOUND).build();
    }

}
