package no.cantara.service.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static no.cantara.service.setup.SetupLoadTestResource.CONFIG_PATH;

@Path(CONFIG_PATH)
@Produces(MediaType.TEXT_HTML)
public class SetupLoadTestResource {
    public static final String CONFIG_PATH = "/config";
    private static final Logger log = LoggerFactory.getLogger(SetupLoadTestResource.class);

    @GET
    public Response presentConfigUI() {
        log.trace("presentConfigUI");
        String response = "<html><body>Number of threads: 56</body></html>";
        return Response.ok(response).build();
    }

}