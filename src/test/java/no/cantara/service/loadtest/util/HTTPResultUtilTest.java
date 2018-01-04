package no.cantara.service.loadtest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.model.LoadTestConfigTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class HTTPResultUtilTest {

    private static final Logger log = LoggerFactory.getLogger(LoadTestConfigTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSelectingElementsFromHTTPResult() throws Exception {
        String exampleResult = "{\n" +
                "  \"test_id\": \"TestID\",\n" +
                "  \"test_name\": \"En liten test\",\n" +
                "  \"test_no_of_threads\": 10,\n" +
                "  \"test_read_write_ratio\": 90,\n" +
                "  \"test_sleep_in_ms\": 80,\n" +
                "  \"test_randomize_sleeptime\": true,\n" +
                "  \"test_duration_in_seconds\": 10\n" +
                "}";
        String testResult = "This is Chaitanya from Beginnersbook.com.";
        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#testName", "$..test_name");
        regexpSelectorMap.put("#randomizeName", "$..test_randomize_sleeptime");

        Map parseResults = HTTPResultUtil.parseWithJsonPath(testResult, regexpSelectorMap);

        log.trace("Resulting values {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parseResults));

    }

    @Test
    public void testRegExpElementsFromHTTPResult() throws Exception {
        String exampleResult = "FOO[DOG] = DOG\n" +
                "FOO[CAT] = CAT";
        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#FOO[BAR]", "\".*DOG.*\"");
        //regexpSelectorMap.put("#FOO[BAR]", "\\\\(.*?)\\\\]");

        Map parseResults = HTTPResultUtil.parseWithRegexp(exampleResult, regexpSelectorMap);

        log.trace("Resulting values {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parseResults));

    }
}