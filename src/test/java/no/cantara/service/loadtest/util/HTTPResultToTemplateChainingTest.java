package no.cantara.service.loadtest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.base.util.json.JsonPathHelper;
import no.cantara.service.model.LoadTestConfigTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
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

    @Test
    public void testJsonPathListInserts() throws Exception {
        String json = "{   \"resultSet\": [\n" +
                "        {\n" +
                "            \"id\": \"105b595a-e523-4c20-9d44-3365251c4364::default::2\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"b8d8757d-8e02-4ce5-9808-85b29b55ca79::default::5\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        List jsonList = JsonPathHelper.findJsonpathList(json, "$.resultSet[*].id");
        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonList);
        Map<String, String> replacements = new HashMap<>();

        replacements.put("##JsonList", jsonString);
        String result = TemplateUtil.updateTemplateWithValuesFromMap("The result #JsonList", replacements);
        assertTrue(!result.contains("##JsonList"));
        log.trace("Fizzled result: {}", result);
    }

    @Test
    public void testJsonPathListInserts2() throws Exception {
        String json = "{   \"resultSet\": [\n" +
                "        {\n" +
                "            \"id\": \"105b595a-e523-4c20-9d44-3365251c4364::default::2\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"b8d8757d-8e02-4ce5-9808-85b29b55ca79::default::5\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        List jsonList = JsonPathHelper.findJsonpathList(json, "$.resultSet[*].id");
        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonList);
        Map<String, String> replacements = new HashMap<>();

        replacements.put("#JsonList", jsonString);
        String result = TemplateUtil.updateTemplateWithValuesFromMap("The result #fizzle(option:#JsonList)", replacements);
        assertTrue(!result.contains("##JsonList"));
        log.trace("Fizzled result: {}", result);
    }

    @Test
    public void testFizzleMap() throws Exception {
        String jsonString = "[ \"AsT5OjbzRn430zqMLgV3Ia\" ]";
        Map<String, String> replacements = new HashMap<>();

        replacements.put("#JsonList", jsonString);
        String result = TemplateUtil.updateTemplateWithValuesFromMap("The result #fizzle(option:#JsonList)", replacements);
        assertTrue(!result.contains("##JsonList"));
        log.trace("Fizzled result: {}", result);
    }

    @Test
    public void testFizzleSubstring() throws Exception {
        String testString="d1a11d8b-06b5-4723-936f-17aa070ff16f::default::1";
        Map<String, String> replacements = new HashMap<>();

        replacements.put("#testString", testString);
        String result = TemplateUtil.updateTemplateWithValuesFromMap("#fizzle(substring(0,43):#testString)", replacements);

        assertTrue("d1a11d8b-06b5-4723-936f-17aa070ff16f".equalsIgnoreCase(result));
    }


    @Test
    public void testFizzleRealSubstring() throws Exception {
        String json = "{\"commitData\":[{\"href\":\"http://thinkehr-app.thinkehr-cluster04.svc.cluster.local:8081/rest/v1/composition/924dd513-e98e-4042-bcbb-0fd5255eca9f::default::1\",\"id\":\"924dd513-e98e-4042-bcbb-0fd5255eca9f::default::1\",\"action\":\"CREATE\"}]}";

        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#processId", "$..id");
        Map parseResults = HTTPResultUtil.parse(json, regexpSelectorMap);

        String result = TemplateUtil.updateTemplateWithValuesFromMap("#fizzle(substring(3,46):#processId)", parseResults);

        assertTrue("924dd513-e98e-4042-bcbb-0fd5255eca9f".equalsIgnoreCase(result));
    }
}
