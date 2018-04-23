package no.cantara.service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.health.HealthResource;
import no.cantara.service.loadtest.LoadTestExecutorService;
import no.cantara.service.loadtest.util.FileFinder;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.TestSpecification;
import no.cantara.service.model.TestSpecificationLoader;
import no.cantara.simulator.RestTestResource;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.cantara.service.Main.CONTEXT_PATH;
import static no.cantara.service.config.ConfigLoadTestResource.CONFIG_PATH;
import static no.cantara.service.loadtest.LoadTestExecutorService.DEFAULT_READ_TEST_SPECIFICATION;
import static no.cantara.service.loadtest.LoadTestExecutorService.DEFAULT_WRITE_TEST_SPECIFICATION;
import static no.cantara.service.loadtest.LoadTestResource.*;

@Path(CONFIG_PATH)
@Produces(MediaType.TEXT_HTML)
public class ConfigLoadTestResource {
    public static final String CONFIG_PATH = "/config";
    public static final String CONFIG_PATH_TRACE = "/config/trace";
    public static final String CONFIG_PATH_LOAD = "/config/load";
    public static final String CONFIG_PATH_LOAD_TRACE = "/config/load/trace";
    public static final String CONFIG_PATH_READ = "/config/read";
    public static final String CONFIG_PATH_WRITE = "/config/write";
    public static final String CONFIG_PATH_BENCHMARK = "/config/benchmark";
    public static final String CONFIG_PATH_SELECT_TESTSPECIFICATIONSET = "/config/select";
    private static final Logger log = LoggerFactory.getLogger(ConfigLoadTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String TEST_SPECIFICATION_ROOT_PATH = Configuration.getString("loadtest.testspecification.rootpath");  //./src";
    public static final String DEFAULT_LOAD_TEST_CONFIG = "loadtest_setup/configurations/DefaultLoadTestConfig.json";

    private static String backgroundImageURL = "https://amazingpict.com/wp-content/uploads/2014/03/Light-Abstract-HD-Wallpaper1.jpg";
    private static String backgroundImageURLLoadTest = "https://amazingpict.com/wp-content/uploads/2014/03/Light-Abstract-HD-Wallpaper1.jpg";
    private static String backgroundImageURLDEBUG = "https://amazingpict.com/wp-content/uploads/2014/03/Light-Abstract-Background-Pictures.jpg";

    @GET
    public Response presentConfigUI() {
        log.trace("presentConfigUI");
        backgroundImageURL = backgroundImageURLLoadTest;
        File debugFile = new File(System.getProperty("user.dir") + "/logs/debug_file.log");

        String jsonconfig = "{}";
        if (LoadTestExecutorService.getActiveLoadTestConfig() != null && !"zero".equalsIgnoreCase(LoadTestExecutorService.getActiveLoadTestConfig().getTest_id())) {
            try {
                jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getActiveLoadTestConfig());
            } catch (Exception e) {
                log.error("Unable to read configuration for LoadTest.", e);
            }
        } else {
            try {
                InputStream file = Configuration.loadByName(DEFAULT_LOAD_TEST_CONFIG);
                LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
                jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileLoadtest);
                log.trace("Loaded defaultConfig: {}", jsonconfig);
            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }
        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>  " +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - LoadTestRun Configuration - LOADTEST MODE</h3><br/>" +
                        "  &nbsp;&nbsp;<b><a href=\"" + CONTEXT_PATH + CONFIG_PATH_TRACE + "\">Switch to DEBUG mode</a></b><br/><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM + "\" method=\"POST\" id=\"jsonConfig\"'>" +
                        "        LoadTestConfig:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"18\" cols=\"80\">" + jsonconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\" value=\"Submit and start this LoadTest\">" +
                        "    </form>" +
                        "  <ul>" +
                        "  <li>1. <a href=\"" + CONTEXT_PATH + CONFIG_PATH_READ + "\">Configure Read TestSpecification</a></li>" +
                        "  <li>2. <a href=\"" + CONTEXT_PATH + CONFIG_PATH_WRITE + "\">Configure Write TestSpecification</a></li>" +
                        "  <li>3. <a href=\"" + CONTEXT_PATH + CONFIG_PATH_BENCHMARK + "\">Configure LoadTestBenchmark</a></li>" +
                        "  <li>4. <a href=\"" + CONTEXT_PATH + CONFIG_PATH_LOAD + "\">Select LoadTestConfig</a></li>" +
                        "  <br/>" +
                        "  <li><a href=\"" + CONTEXT_PATH + RestTestResource.REST_PATH + "/debug" + "\">Debug last LoadTest</a></li>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_SELECT_TESTSPECIFICATIONSET + "\">Select or upload configured TestSpecification set</a></li>" +
                        "  </ul><br/><br/>" +
                        "  <a href=\"https://github.com/Cantara/HTTPLoadTest-Baseline\">Documentation and SourceCode</a><br/><br/>" +
                        "  HTTPLoadTest-Baseline " + HealthResource.getVersion() + "<br/" +
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }  //

    @Path("/trace")
    @GET
    public Response presentConfigUITrace() {
        log.trace("presentConfigUI-trace");
        backgroundImageURL = backgroundImageURLDEBUG;

        String jsonconfig = "{}";
        if (LoadTestExecutorService.getActiveLoadTestConfig() != null && !"zero".equalsIgnoreCase(LoadTestExecutorService.getActiveLoadTestConfig().getTest_id())) {
            try {
                LoadTestConfig loadTestConfig = LoadTestExecutorService.getActiveLoadTestConfig();
                loadTestConfig.setTest_no_of_threads(1);
                loadTestConfig.setTest_read_write_ratio(50);
                loadTestConfig.setTest_duration_in_seconds(3);
                jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loadTestConfig);
            } catch (Exception e) {
                log.error("Unable to read configuration for LoadTest.", e);
            }
        } else {
            try {
                InputStream file = Configuration.loadByName(DEFAULT_LOAD_TEST_CONFIG);
                LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
                fileLoadtest.setTest_no_of_threads(1);
                fileLoadtest.setTest_read_write_ratio(50);
                fileLoadtest.setTest_duration_in_seconds(3);
                jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileLoadtest);
                log.trace("Loaded defaultConfig: {}", jsonconfig);
            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }
        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>  " +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - LoadTestRun Configuration - DEBUG MODE</h3><br/>" +
                        "  &nbsp;&nbsp;<b><a href=\"" + CONTEXT_PATH + CONFIG_PATH + "\">Switch to LoadTest mode</a></b><br/><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_TRACE + "\" method=\"POST\" id=\"jsonConfig\"'>" +
                        "        LoadTestConfig:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"18\" cols=\"80\">" + jsonconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\" value=\"Submit and DEBUG this LoadTest\">" +
                        "    </form>" +
                        "&nbsp;If DEBUG display a short log, just re-run the test in DEBUG mode." +
                        "  <ul>" +
                        "  <li>1. <a href=\"" + CONTEXT_PATH + CONFIG_PATH_READ + "\">Configure Read TestSpecification</a></li>" +
                        "  <li>2. <a href=\"" + CONTEXT_PATH + CONFIG_PATH_WRITE + "\">Configure Write TestSpecification</a></li>" +
                        "  <li>3. <a href=\"" + CONTEXT_PATH + CONFIG_PATH_LOAD_TRACE + "\">Select LoadTestConfig </a></li>" +
                        "  <li><a href=\"" + CONTEXT_PATH + RestTestResource.REST_PATH + "/debug" + "\">Debug last LoadTest</a></li>" +
                        "  <br/>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_BENCHMARK + "\">Configure LoadTestBenchmark</a></li>" +
                        "  <li><a href=\"" + CONTEXT_PATH + CONFIG_PATH_SELECT_TESTSPECIFICATIONSET + "\">Select or upload configured TestSpecification set</a></li>" +
                        "  </ul><br/><br/>" +
                        "  <a href=\"https://github.com/Cantara/HTTPLoadTest-Baseline\">Documentation and SourceCode</a><br/><br/>" +
                        "  HTTPLoadTest-Baseline " + HealthResource.getVersion() + "<br/" +
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }  //


    @Path("/load")
    @GET
    public Response presentTestConfigConfigUI() {
        log.trace("presentReadConfigUI");
        String jsonconfig = "{}";

        if (jsonconfig == null || jsonconfig.length() < 20) {
            try {
                InputStream file = Configuration.loadByName(DEFAULT_LOAD_TEST_CONFIG);
                LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
                jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileLoadtest);
                log.trace("Loaded defaultConfig: {}", jsonconfig);


            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }
        String optionString = "";
        try {
            ArrayList<String> filenames = null;
            java.nio.file.Path startingDir = Paths.get(TEST_SPECIFICATION_ROOT_PATH);
            String pattern = Configuration.getString("loadtest.testspecification.filematcher");
            FileFinder finder = new FileFinder(pattern);
            Files.walkFileTree(startingDir, finder);
            filenames = finder.done();

            for (int n = 0; n < filenames.size(); n++) {
                optionString = optionString + "        <option value=\"" + "FILE:" + filenames.get(n) + "\">" + filenames.get(n) + "</option>";
            }
        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTestConfig.", e);
        }

        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>" +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - LoadTestConfig Configuration</h3><br/>";
        if (optionString != null && optionString.length() > 5) {
            response = response +
                    "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_LOAD + "\" method=\"POST\" '>" +
                    "        Select stored LoadTestConfig:<br/>" +
                    "        <select name=\"jsonConfig\">" +
                    "        " + optionString +
                    "        </select>" +
                    "        <br/><br/>" +
                    "        <input type=\"submit\" value=\"Select\">" +
                    "    </form>" +
                    "    <br/><br/>";
        }

        response = response +
                "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_LOAD + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                "        LoadTestConfig:<br/>" +
                "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"30\" cols=\"80\">" + jsonconfig + "</textarea><br/><br/>" +
                "        <input type=\"submit\">" +
                "    </form>\n" +
                "\n" +
                "  </body>" +
                "</html>";
        return Response.ok(response).build();
    }

    @Path("/load/trace")
    @GET
    public Response presentTestConfigConfigUITrace() {
        log.trace("presentReadConfigUI-trace");
        String jsonconfig = "{}";

        if (jsonconfig == null || jsonconfig.length() < 20) {
            try {
                InputStream file = Configuration.loadByName(DEFAULT_LOAD_TEST_CONFIG);
                LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
                jsonconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileLoadtest);
                log.trace("Loaded defaultConfig: {}", jsonconfig);


            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTestConfig.", e);
            }
        }
        String optionString = "";
        try {
            ArrayList<String> filenames = null;
            java.nio.file.Path startingDir = Paths.get(TEST_SPECIFICATION_ROOT_PATH);
            String pattern = Configuration.getString("loadtest.testspecification.filematcher");
            FileFinder finder = new FileFinder(pattern);
            Files.walkFileTree(startingDir, finder);
            filenames = finder.done();

            for (int n = 0; n < filenames.size(); n++) {
                optionString = optionString + "        <option value=\"" + "FILE:" + filenames.get(n) + "\">" + filenames.get(n) + "</option>";
            }
        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTestConfig.", e);
        }

        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>" +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - LoadTestConfig Configuration</h3><br/>";
        if (optionString != null && optionString.length() > 5) {
            response = response +
                    "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_LOAD_TRACE + "\" method=\"POST\" '>" +
                    "        Select stored LoadTestConfig:<br/>" +
                    "        <select name=\"jsonConfig\">" +
                    "        " + optionString +
                    "        </select>" +
                    "        <br/><br/>" +
                    "        <input type=\"submit\" value=\"Select\">" +
                    "    </form>" +
                    "    <br/><br/>";
        }

        response = response +
                "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_LOAD_TRACE + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                "        LoadTestConfig:<br/>" +
                "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"30\" cols=\"80\">" + jsonconfig + "</textarea><br/><br/>" +
                "        <input type=\"submit\">" +
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
        String jsonreadconfig = LoadTestExecutorService.getReadTestSpecificationListJson();
        if (jsonreadconfig == null || jsonreadconfig.length() < 20) {
            try {

                InputStream file = Configuration.loadByName(DEFAULT_READ_TEST_SPECIFICATION);
                List<TestSpecification> readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
                });
                jsonreadconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpec);
                log.trace("Loaded DefaultReadTestSpecification: {}", jsonreadconfig);


            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }
        String optionString = "";
        try {
            ArrayList<String> filenames = null;
            java.nio.file.Path startingDir = Paths.get(TEST_SPECIFICATION_ROOT_PATH);
            String pattern = Configuration.getString("loadtest.testspecification.read.filematcher");
            FileFinder finder = new FileFinder(pattern);
            Files.walkFileTree(startingDir, finder);
            filenames = finder.done();

            for (int n = 0; n < filenames.size(); n++) {
                optionString = optionString + "        <option value=\"" + "FILE:" + filenames.get(n) + "\">" + filenames.get(n) + "</option>";
            }
        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
        }

        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>" +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - Read TestSpecification Configuration</h3><br/>";
        if (optionString != null && optionString.length() > 5) {
            response = response +
                    "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_READ + "\" method=\"POST\" '>" +
                    "        Select stored ReadTestSpecifications:<br/>" +
                    "        <select name=\"jsonConfig\">" +
                    "        " + optionString +
                    "        </select>" +
                    "        <br/><br/>" +
                    "        <input type=\"submit\" value=\"Select\">" +
                    "    </form>" +
                    "    <br/><br/>";
        }

        response = response +
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
        String jsonwriteconfig = LoadTestExecutorService.getWriteTestSpecificationListJson();
        log.trace("presentWriteConfigUI jsonwriteconfig:{}", jsonwriteconfig);

        if (jsonwriteconfig == null || jsonwriteconfig.length() < 20) {
            try {
                InputStream wfile = Configuration.loadByName(DEFAULT_WRITE_TEST_SPECIFICATION);
                List<TestSpecification> writeTestSpec = mapper.readValue(wfile, new TypeReference<List<TestSpecification>>() {
                });
                jsonwriteconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpec);
                log.trace("Loaded DefaultWriteTestSpecification: {}", jsonwriteconfig);

            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }
        String optionString = "";
        try {
            ArrayList<String> filenames = null;
            java.nio.file.Path startingDir = Paths.get(TEST_SPECIFICATION_ROOT_PATH);
            String pattern = Configuration.getString("loadtest.testspecification.write.filematcher");
            FileFinder finder = new FileFinder(pattern);
            Files.walkFileTree(startingDir, finder);
            filenames = finder.done();

            for (int n = 0; n < filenames.size(); n++) {
                optionString = optionString + "        <option value=\"" + "FILE:" + filenames.get(n) + "\">" + filenames.get(n) + "</option>";
            }
        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
        }


        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>  " +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - Write TestSpecification Configuration</h3><br/>";
        if (optionString != null && optionString.length() > 5) {
            response = response +
                    "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_WRITE + "\" method=\"POST\" '>" +
                    "        Select stored WriteTestSpecifications:<br/>" +
                    "        <select name=\"jsonConfig\">" +
                    "        " + optionString +
                    "        </select>" +
                    "        <br/><br/>" +
                    "        <input type=\"submit\" value=\"Select\">" +
                    "    </form>" +
                    "    <br/><br/>";
        }
        response = response +

                "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_WRITE + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        WriteTestSpecification:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"60\" cols=\"80\">" + jsonwriteconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\"><br/>" +
                        "    </form>\n" +
                        "\n" +
                        "  </body>" +
                        "</html>";
        log.trace("presentWriteConfigUI jsonwriteconfig:{}", jsonwriteconfig);
        return Response.ok(response).build();
    }


    @Path("/benchmark")
    @GET
    public Response presentBenchmarkConfigUI() {
        log.trace("presentBenchmarkConfigUI");
        String jsonbenchmarkconfig = "";
        try {
            jsonbenchmarkconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestResultUtil.getLoadTestBenchmark());
        } catch (Exception e) {
            log.error("Unable to map configured benchmark to json. {}", e);
        }
        log.trace("presentBenchmarkConfigUI jsonbenchmarkconfig:{}", jsonbenchmarkconfig);

        if (jsonbenchmarkconfig == null || jsonbenchmarkconfig.length() < 20) {
            try {

                jsonbenchmarkconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestResultUtil.getLoadTestBenchmark());
                log.trace("Loaded DefaultLoadTestBenchmark: {}", jsonbenchmarkconfig);

            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
        }
        String response =
                "<html>" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>  " +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - LoadTestBenchmark Configuration</h3><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_BENCHMARK + "\" method=\"POST\" id=\"jsonConfig\"'>\n" +
                        "        LoadTestBenchmark:<br/>" +
                        "               <textarea name=\"jsonConfig\" form=\"jsonConfig\" rows=\"20\" cols=\"80\">" + jsonbenchmarkconfig + "</textarea><br/><br/>" +
                        "        <input type=\"submit\"><br/>" +
                        "    </form>\n" +
                        "\n" +
                        "  </body>" +
                        "</html>";
        log.trace("presentBenchmarkConfigUI jsonbenchmarkconfig:{}", jsonbenchmarkconfig);
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
                        "  <meta charset=\"UTF-8\">" +
                        "</head>" +
                        "<body background=\"" + backgroundImageURL + "\">\n" +
                        "  <h3>HTTPLoadTest - Select pre-configured TestSpecification Configuration</h3><br/><br/>" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_FORM_SELECT + "\" method=\"POST\" '>" +
                        "        <select name=\"jsonConfig\">" +
                        "        " + optionString +
                        "        </select>" +
                        "        <br/><br/>" +
                        "        <input type=\"submit\">" +
                        "    </form>" +
                        "    <br/<<br/>" +
                        "    Choose file to upload<br>\n" +
                        "    <form action=\"" + CONTEXT_PATH + APPLICATION_PATH_ZIP + "\" method=\"post\" enctype=\"multipart/form-data\">" +
                        "        <input name=\"file\" id=\"filename\" type=\"file\" /><br><br>\n" +
                        "        <button name=\"submit\" type=\"submit\">Upload</button>\n" +
                        "    </form>" +
                        "  <br/><br/>" +
                        "  <a href=\"https://github.com/Cantara/HTTPLoadTest-Baseline\">Documentation and SourceCode</a><br/>" +
                        "HTTPLoadTest-Baseline " + HealthResource.getVersion() + " - running since: " + HealthResource.getRunningSince() + "<br/" +
                        "  </body>" +
                        "</html>";
        return Response.ok(response).build();
    }  //


}