package no.cantara.service.loadtest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.model.LoadTestConfigTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class HTTPResultToTemplateChainingTest {

    private static final Logger log = LoggerFactory.getLogger(LoadTestConfigTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSelectingElementsFromHTTPResultAndUseThemInOption() throws Exception {
        String exampleResult = "{\n" +
                "  \"test_id\": \"TestID\",\n" +
                "  \"test_name\": \"En, liten, test\",\n" +
                "  \"test_no_of_threads\": 10,\n" +
                "  \"test_read_write_ratio\": 90,\n" +
                "  \"test_sleep_in_ms\": 80,\n" +
                "  \"test_randomize_sleeptime\": true,\n" +
                "  \"test_duration_in_seconds\": 10\n" +
                "}";
        Map<String, String> regexpSelectorMap = new HashMap<String, String>();
        regexpSelectorMap.put("#testName", "$..test_name");
        regexpSelectorMap.put("#randomizeName", "$..test_randomize_sleeptime");


        Map<String, String> parseResults = HTTPResultUtil.parseWithJsonPath(exampleResult, regexpSelectorMap);

        String nameElements = parseResults.get("#testName");

        log.trace("Resulting nameElements {}", nameElements);

        Map<String, String> replacements = new HashMap<>();

        replacements.put("#BrukerID", nameElements);
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(option:#BrukerID) before this";

        String result = TemplateUtil.updateTemplateWithValuesFromMap(template, replacements);
        assertTrue(!result.contains("#BrukerID"));
        log.trace("Fizzled result: {}", result);

    }
}
