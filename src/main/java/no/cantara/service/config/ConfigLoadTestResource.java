package no.cantara.service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.health.HealthResource;
import no.cantara.service.loadtest.LoadTestExecutorService;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.TestSpecification;
import no.cantara.service.model.TestSpecificationLoader;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static no.cantara.service.Main.CONTEXT_PATH;
import static no.cantara.service.config.ConfigLoadTestResource.CONFIG_PATH;
import static no.cantara.service.loadtest.LoadTestResource.*;

@Path(CONFIG_PATH)
@Produces(MediaType.TEXT_HTML)
public class ConfigLoadTestResource {
    public static final String CONFIG_PATH = "/config";
    public static final String CONFIG_PATH_READ = "/config/read";
    public static final String CONFIG_PATH_WRITE = "/config/write";
    public static final String CONFIG_PATH_SELECT_TESTSPECIFICATIONSET = "/config/select";
    private static final Logger log = LoggerFactory.getLogger(ConfigLoadTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();


    @GET
    public Response presentConfigUI() {
        log.trace("presentConfigUI");
        String jsonconfig = "{}";
        try {
            InputStream file = Configuration.loadByName("DefaultLoadTestConfig.json");
            LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
            jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileLoadtest);
            log.trace("Loaded defaultConfig: {}", jsonconfig);
        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
        }
        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>  " +
                        "<body>\n" +
                        "  <h3>HTTPLoadTest - LoadTestRun Configuration</h3><br/><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM + "\" method=\"POST\" id=\"jsonConfig\"'>" +
                        "        LoadTestConfig:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"14\" cols=\"80\">" + jsonconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\">" +
                        "    </form>" +
                        "  <br/><br/>" +
                        "  <ul>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_SELECT_TESTSPECIFICATIONSET + "\">Select configured TestSpecification set</a></li>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_READ + "\">Configure Read TestSpecification</a></li>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_WRITE + "\">Configure Write TestSpecification</a></li>" +
                        "  </ul><br/><br/>" +
                        "  <a href=\"https://github.com/Cantara/HTTPLoadTest-Baseline\">Documentation and SourceCode</a><br/><br/>" +
                        "  HTTPLoadTest-Baseline "+ HealthResource.getVersion()+"<br/"+
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }  //

    @Path("/read")
    @GET
    public Response presentReadConfigUI() {
        log.trace("presentReadConfigUI");

        String jsonreadconfig = LoadTestExecutorService.getReadTestSpecificationListJson();
        if (jsonreadconfig == null || jsonreadconfig.length() < 20) {
            try {

                InputStream file = Configuration.loadByName("DefaultReadTestSpecification.json");
                List<TestSpecification>  readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
                });
                jsonreadconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpec);
                log.trace("Loaded DefaultReadTestSpecification: {}", jsonreadconfig);

            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }

        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>" +
                        "  <body>\n" +
                        "  <h3>HTTPLoadTest - Read TestSpecification Configuration</h3><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_READ + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        ReadTestSpecification:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"60\" cols=\"80\">" + Charset.forName("UTF-8").encode(jsonreadconfig) + "</textarea><br/><br/>" +
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
        String jsonwriteconfig = LoadTestExecutorService.getWriteTestSpecificationListJson();
        log.trace("presentWriteConfigUI jsonwriteconfig:{}", jsonwriteconfig);

        if (jsonwriteconfig == null || jsonwriteconfig.length() < 20) {
            try {
                InputStream wfile = Configuration.loadByName("DefaultWriteTestSpecification.json");
                List<TestSpecification> writeTestSpec =  mapper.readValue(wfile, new TypeReference<List<TestSpecification>>() {
                });
                jsonwriteconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpec);
                log.trace("Loaded DefaultWriteTestSpecification: {}", jsonwriteconfig);

            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }
        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>  " +
                        "<body>\n" +
                        "  <h3>HTTPLoadTest - Write TestSpecification Configuration</h3><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_WRITE + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        WriteTestSpecification:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"60\" cols=\"80\">" + Charset.forName("UTF-8").encode(jsonwriteconfig) + "</textarea><br/><br/>" +
                        "        <input type=\"submit\"><br/>" +
                        "    </form>\n" +
                        "\n" +
                        "  </body>" +
                        "</html>";
        log.trace("presentWriteConfigUI jsonwriteconfig:{}", Charset.forName("UTF-8").encode(jsonwriteconfig).array());
        return Response.ok(response).build();
    }


    @Path("/select")
    @GET
    public Response selectTestSpecificationSet() {
        log.trace("selectTestSpecificationSet");
        Map<String, String> configuredTestSpecifications = TestSpecificationLoader.getPersistedTestSpecificationFilenameMap();
        String optionString = "";
        for (int n = 0; n < configuredTestSpecifications.size() / 2; n++) {
            int displayvalue = 1 + n;
            String displayname = Configuration.getString("TestSpecification." + displayvalue + ".displayname") +
                    "   (" + displayvalue +
                    " [read:" + Configuration.getString("TestSpecification." + displayvalue + ".read.filename") +
                    ":write:" + Configuration.getString("TestSpecification." + displayvalue + ".write.filename") +
                    "])";
            optionString = optionString + "        <option value=\"" + displayvalue + "\">" + displayname + "</option>";
        }

        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>" +
                        "  <body>\n" +
                        "  <h3>HTTPLoadTest - Select pre-configured TestSpecification Configuration</h3><br/><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_SELECT + "\" method=\"POST\" '>" +
                        "        <select name=\"jsonConfig\">" +
                        "        " + optionString +
                        "        </select>" +
                        "        <br/><br/>" +
                        "        <input type=\"submit\">" +
                        "    </form>" +
                        "  <br/><br/>" +
                        "  <a href=\"https://github.com/Cantara/HTTPLoadTest-Baseline\">Documentation and SourceCode</a><br/>" +
                        "HTTPLoadTest-Baseline "+ HealthResource.getVersion()+"<br/"+
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }  //


}