package no.cantara.service.setup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static no.cantara.service.Main.CONTEXT_PATH;
import static no.cantara.service.loadtest.LoadTestResource.APPLICATION_PATH_FORM_READ;
import static no.cantara.service.loadtest.LoadTestResource.APPLICATION_PATH_FORM_WRITE;
import static no.cantara.service.setup.SetupLoadTestResource.CONFIG_PATH;

@Path(CONFIG_PATH)
@Produces(MediaType.TEXT_HTML)
public class SetupLoadTestResource {
    public static final String CONFIG_PATH = "/config";
    public static final String CONFIG_PATH_READ = "/config/read";
    public static final String CONFIG_PATH_WRITE = "/config/write";
    private static final Logger log = LoggerFactory.getLogger(SetupLoadTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();


    @GET
    public Response presentConfigUI() {
        log.trace("presentConfigUI");

        String jsonconfig = "{}";

        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("DefaultLoadTestConfig.json").getFile());
            LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
            jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileLoadtest);
            log.trace("Loaded defaultconfig: {}", jsonconfig);

        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
        }
        String response =
                "<html>" +
                        "  <body>\n" +
                        "  <h3>HTTPLoadTest - LoadTestRun Configuration</h3><br/><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + CONFIG_PATH_WRITE + "\" method=\"POST\" id=\"jsonConfig\"'>" +
                        "        LoadTestConfig:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"14\" cols=\"80\">" + jsonconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\">" +
                        "    </form>" +
                        "  <br/><br/>" +
                        "  <ul>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_READ + "\">Configure Read TestSpecification</a></li>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_WRITE + "\">Configure Write TestSpecification</a></li>" +
                        "  </ul><br/><br/>\n" +
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }

    @Path("/read")
    @GET
    public Response presentReadConfigUI() {
        log.trace("presentReadConfigUI");

        String jsonreadconfig = "{}";

        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("DefaultReadTestSpecification.json").getFile());
            List<TestSpecification> readTestSpec = new ArrayList<>();
            readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
            });
            jsonreadconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpec);
            log.trace("Loaded DefaultReadTestSpecification: {}", jsonreadconfig);

        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
        }

        String response =
                "<html>" +
                        "  <body>\n" +
                        "  <h3>HTTPLoadTest - Read TestSpecification Configuration</h3><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_READ + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        ReadTestSpecification:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"60\" cols=\"80\">" + jsonreadconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\">" +
                        "    </form>\n" +
                        "\n" +
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }

    @Path("/write")
    @GET
    public Response presentWriteConfigUI() {
        log.trace("presentWriteConfigUI");

        String jsonwriteconfig = "{}";

        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("DefaultWriteTestSpecification.json").getFile());
            List<TestSpecification> writeTestSpec = new ArrayList<>();
            writeTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
            });
            jsonwriteconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpec);
            log.trace("Loaded DefaultWriteTestSpecification: {}", jsonwriteconfig);

        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
        }

        String response =
                "<html>" +
                        "  <body>\n" +
                        "  <h3>HTTPLoadTest - Write TestSpecification Configuration</h3><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_WRITE + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        WriteTestSpecification:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"60\" cols=\"80\">" + jsonwriteconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\"><br/>" +
                        "    </form>\n" +
                        "\n" +
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }

}