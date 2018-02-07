package no.cantara.service.loadtest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileFinderTest {

    private static final Logger log = LoggerFactory.getLogger(HTTPResultUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String SEARCH_FILE_PATH = "./src/";

    @Test
    public void testUsage() throws Exception {


        Path startingDir = Paths.get(SEARCH_FILE_PATH);
        String pattern = "**Write*on.json*";

        FileFinder finder = new FileFinder(pattern);
        Files.walkFileTree(startingDir, finder);
        ArrayList<String> filenames = finder.done();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(filenames));

        System.out.println("files found:" + filenames.size());

    }

}
