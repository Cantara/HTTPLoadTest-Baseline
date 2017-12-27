package no.cantara.service.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.LoadTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

import static no.cantara.service.setup.SetupLoadTestResource.CONFIG_PATH;

@Path(CONFIG_PATH)
@Produces(MediaType.TEXT_HTML)
public class SetupLoadTestResource {
    public static final String CONFIG_PATH = "/config";
    private static final Logger log = LoggerFactory.getLogger(SetupLoadTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();


    @GET
    public Response presentConfigUI() {
        log.trace("presentConfigUI");

        String jsonconfig = "{}";

        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("loadtestconfig.json").getFile());
            LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
            String loadTestJson = mapper.writeValueAsString(fileLoadtest);

        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
        }

        String response = "<html><body>" + jsonconfig + "</body></html>";
        return Response.ok(response).build();
    }

}