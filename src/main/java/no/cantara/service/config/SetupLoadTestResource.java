package no.cantara.service.config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static no.cantara.service.Main.CONTEXT_PATH;
import static no.cantara.service.config.ConfigLoadTestResource.CONFIG_PATH;
import static no.cantara.service.config.SetupLoadTestResource.SETUP_PATH;

@Path(SETUP_PATH)
@Produces(MediaType.TEXT_HTML)
public class SetupLoadTestResource {

    public static final String SETUP_PATH = "/setup";

    @GET
    public Response presentConfigUIRedirect() {
        return Response.status(Response.Status.FOUND).header("Location", (CONTEXT_PATH + CONFIG_PATH)).build();
    }
}
