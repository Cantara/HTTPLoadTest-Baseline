package no.cantara.service.health;

import no.cantara.service.loadtest.LoadTestExecutorService;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

/**
 * Simple health endpoint for checking the server is running
 *
 * @author <a href="mailto:asbjornwillersrud@gmail.com">Asbjørn Willersrud</a> 30/03/2016.
 */
@Path(HealthResource.HEALTH_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    public static final String HEALTH_PATH = "/health";
    private static final Logger log = LoggerFactory.getLogger(HealthResource.class);

    @GET
    public Response healthCheck() {
        log.trace("healthCheck");
        String resultsJson = LoadTestResultUtil.printStats(LoadTestExecutorService.getResultListSnapshot(), false);  // Do not force stats when tests are running
        String response = String.format("{ \"HTTPLoadTest-health\": \"OK\", \n\"version\": \"%s\", \n\"now\":\"%s\"," +
                        " \n\"running since\": \"%s\", \n\n\"resultfiles\": \n\"%s\", \n\n\"passed_benchmark\": \"%s\",\n\n\"statistics\": \n\"%s\", \n\n\"readTestSpecification\": \n\"%s\", \n\n\"writeTestSpecification\": \n\"%s\"}",
                getVersion(),
                Instant.now(),
                getRunningSince(),
                LoadTestResultUtil.listStoredResults(),
                LoadTestResultUtil.hasPassedBenchmark(LoadTestResultUtil.hasPassedBenchmark(LoadTestExecutorService.getResultListSnapshot(), false)),
                resultsJson,
                LoadTestExecutorService.getReadTestSpecificationListJson(),
                LoadTestExecutorService.getWriteTestSpecificationListJson());
        return Response.ok(response).build();
    }

    public static String getRunningSince() {
        long uptimeInMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        return Instant.now().minus(uptimeInMillis, ChronoUnit.MILLIS).toString();
    }

    public static String getVersion() {
        Properties mavenProperties = new Properties();
        String resourcePath = "/META-INF/maven/no.cantara.service/HTTPLoadTest-baseline/pom.properties";
        URL mavenVersionResource = Configuration.class.getResource(resourcePath);
        if (mavenVersionResource != null) {
            try {
                mavenProperties.load(mavenVersionResource.openStream());
                return mavenProperties.getProperty("version", "missing version info in " + resourcePath);
            } catch (IOException e) {
                log.warn("Problem reading version resource from classpath: ", e);
            }
        }
        return "(DEV VERSION)";
    }
}