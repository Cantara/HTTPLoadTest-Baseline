package no.cantara.service.setup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.LoadTestConfig;
import no.cantara.service.TestSpecification;
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
import static no.cantara.service.loadtest.LoadTestResource.*;
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
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        LoadTestConfig:\n" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"14\" cols=\"80\">" + jsonconfig + "</textarea><br/>" +
                        "        <input type=\"submit\">\n" +
                        "    </form>\n" +
                        "\n" +
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
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_READ + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        ReadTestSpecification:\n" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"14\" cols=\"80\">" + jsonreadconfig + "</textarea><br/>" +
                        "        <input type=\"submit\">\n" +
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
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_WRITE + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        WriteTestSpecification:\n" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"14\" cols=\"80\">" + jsonwriteconfig + "</textarea><br/>" +
                        "        <input type=\"submit\">\n" +
                        "    </form>\n" +
                        "\n" +
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }

}