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
    public void testSelectingElementsFromHJsonTTPResult() throws Exception {
        String exampleResult = "{\n" +
                "  \"test_id\": \"TestID\",\n" +
                "  \"test_name\": \"En liten test\",\n" +
                "  \"test_no_of_threads\": 10,\n" +
                "  \"test_read_write_ratio\": 90,\n" +
                "  \"test_sleep_in_ms\": 80,\n" +
                "  \"test_randomize_sleeptime\": true,\n" +
                "  \"test_duration_in_seconds\": 10\n" +
                "}";
        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#testName", "$..test_name");
        regexpSelectorMap.put("#randomizeName", "$..test_randomize_sleeptime");

        Map parseResults = HTTPResultUtil.parseWithJsonPath(exampleResult, regexpSelectorMap);

        log.trace("Resulting values {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parseResults));

    }

    @Test
    public void testSelectingElementsFromJsonHTTPResult2() throws Exception {
        String exampleResult = "{\n" +
                "  \"test_id\": \"TestID\",\n" +
                "  \"test_name\": \"En liten test\",\n" +
                "  \"test_no_of_threads\": 10,\n" +
                "  \"test_read_write_ratio\": 90,\n" +
                "  \"test_sleep_in_ms\": 80,\n" +
                "  \"test_randomize_sleeptime\": true,\n" +
                "  \"test_duration_in_seconds\": 10\n" +
                "}";
        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#testName", "$..test_name");
        regexpSelectorMap.put("#randomizeName", "$..test_randomize_sleeptime");

        Map parseResults = HTTPResultUtil.parse(exampleResult, regexpSelectorMap);

        log.trace("Resulting values {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parseResults));

    }

    @Test
    public void testSelectingElementsFromJsonHTTPResult3() throws Exception {
        String exampleResult = "{ \"access_token\":\"AsT5OjbzRn430zqMLgV3Ia\" }";
        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#access_token", "$..access_token");
        Map parseResults = HTTPResultUtil.parse(exampleResult, regexpSelectorMap);

        log.trace("Resulting values {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parseResults));
        //assertTrue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parseResults))
        //assertTrue(parseResults.containsValue("AsT5OjbzRn430zqMLgV3Ia"));
    }

    @Test
    public void testSelectingElementsFromXMLHTTPResult() throws Exception {
        String exampleResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<no>" +
                "   <cantara>" +
                "       <base>test</base>" +
                "   </cantara>" +
                "</no>";
        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#base", "//base");

        Map parseResults = HTTPResultUtil.parse(exampleResult, regexpSelectorMap);

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