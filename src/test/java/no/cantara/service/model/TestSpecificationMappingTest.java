package no.cantara.service.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import no.cantara.service.loadtest.commands.Command;
import no.cantara.service.loadtest.commands.CommandFactory;
import no.cantara.service.loadtest.util.TemplateUtil;
import no.cantara.service.testsupport.TestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class TestSpecificationMappingTest {

    private static final Logger log = LoggerFactory.getLogger(TestSpecificationMappingTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Test
    public void testTestSpecificationMapping() throws Exception {
        TestSpecification testSpecification = new TestSpecification();
        String soaptemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:aut=\"http://dbonline.no/webservices/xsd/Autorisasjon\" xmlns:per=\"http://dbonline.no/webservices/xsd/PersonInfo\">\n" +
                "   <soapenv:Header>\n" +
                "      <aut:UserAuthorization>\n" +
                "         <UserID>#BrukerID</UserID>\n" +
                "         <Passord>#Passord</Passord>\n" +
                "         <EndUser>MyEndUserName</EndUser>\n" +
                "         <Versjon>v1-1-0</Versjon>\n" +
                "     </aut:UserAuthorization>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <per:GetPerson>\n" +
                "         <Internref>XYZXYZXYZXYZ</Internref>\n" +
                "         <NameAddress>1</NameAddress>\n" +
                "         <InterestCode>1</InterestCode>\n" +
                "         <Beta>Detaljer</Beta>\n" +
                "      </per:GetPerson>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";

        String jsonTemplate = "{\n" +
                "  \\\"sub\\\": \\\"#BrukerID\\\",\n" +
                "  \\\"name\\\": \\\"#Passord\\\",\n" +
                "  \\\"admin\\\": true\n" +
                "}";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        testSpecification.setCommand_replacement_map(replacements);

        Map<String, String> regexpSelectorMap = new HashMap<>();
        regexpSelectorMap.put("#testName", "$..test_name");
        regexpSelectorMap.put("#randomizeName", "$..test_randomize_sleeptime");
        testSpecification.setCommand_response_map(regexpSelectorMap);


        testSpecification.setCommand_url("http://test.tull.no");
        testSpecification.setCommand_contenttype("applications/xml");
        testSpecification.setCommand_template(soaptemplate);
        testSpecification.setCommand_template(jsonTemplate);

        List<TestSpecification> readTestSpec = new ArrayList<>();

        readTestSpec.add(testSpecification);
        String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpec);
        List<TestSpecification> testSpecification1 = mapper.readValue(loadTestJson, new TypeReference<List<TestSpecification>>() {
        });



    }

    @Test
    public void readTestSpecificationMappingFromFile() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("loadtest_setup/specifications/read/TestReadConfigReadTestSpecification.json").getFile());
        List<TestSpecification> readTestSpec = new ArrayList<>();
        readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
        });
        for (TestSpecification testSpecification : readTestSpec) {
            assertTrue(testSpecification.getCommand_url().length() > 0);
        }
        //      assertTrue(fileLoadtest.getTest_id().equalsIgnoreCase("TestID"));
    }

    @Test
    public void testInstanciateTestConfigFromJson() throws Exception {
        String test = "[\n" +
                "  {\n" +
                "    \"command_url\": \"https://odn1-thinkehr-cluster03.privatedns.zone/rest/v1/query\",\n" +
                "    \"command_contenttype\": \"application/json\",\n" +
                "    \"command_http_post\": true,\n" +
                "    \"command_http_authstring\": \"Basic YWRtaW46YWRtaW4=\",\n" +
                "    \"command_timeout_milliseconds\": 5000,\n" +
                "    \"command_template\": \"{ \\\"aql\\\": \\\"select count(a/uid/value) as count from EHR[ehr_id/value=:ehrId] contains COMPOSITION a where a/archetype_details/archetype_id/value='openEHR-EHR-COMPOSITION.encounter.v1' or a/archetype_details/archetype_id/value='openEHR-EHR-COMPOSITION.journal_event_psky.v0'\\\", \\\"aqlParameters\\\": { \\\"ehrId\\\": \\\"#ehr\\\" }\\t}\",\n" +
                "    \"command_replacement_map\": {\n" +
                "      \"#ehr\": \"f19473e5-fb8a-42cf-b5e8-50ff0deed766\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"command_url\": \"https://odn1-thinkehr-cluster03.privatedns.zone/rest/v1/query\",\n" +
                "    \"command_contenttype\": \"application/json\",\n" +
                "    \"command_http_post\": true,\n" +
                "    \"command_http_authstring\": \"Basic YWRtaW46YWRtaW4=\",\n" +
                "    \"command_timeout_milliseconds\": 5000,\n" +
                "    \"command_template\": \"{ \\\"aql\\\": \\\"select a/uid/value as id from EHR[ehr_id/value=:ehrId] contains COMPOSITION a where a/archetype_details/archetype_id/value='openEHR-EHR-COMPOSITION.encounter.v1' or a/archetype_details/archetype_id/value='openEHR-EHR-COMPOSITION.journal_event_psky.v0'\\\", \\\"aqlParameters\\\": { \\\"ehrId\\\": \\\"#ehr\\\" }\\t}\",\n" +
                "    \"command_replacement_map\": {\n" +
                "      \"#ehr\": \"f19473e5-fb8a-42cf-b5e8-50ff0deed766\"\n" +
                "    },\n" +
                "   \"command_response_map\" : {\n" +
                "      \"#compositionIds\" : \"$.resultSet[*].id\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"command_url\": \"https://odn1-thinkehr-cluster03.privatedns.zone}/rest/v1/composition/{{compositionId}}?format=STRUCTURED\",\n" +
                "    \"command_contenttype\": \"application/json\",\n" +
                "    \"command_http_post\": false,\n" +
                "    \"command_http_authstring\": \"Basic YWRtaW46YWRtaW4=\",\n" +
                "    \"command_timeout_milliseconds\": 5000,\n" +
                "    \"command_template\": \"\",\n" +
                "    \"command_replacement_map\": {\n" +
                "      \"#ehr\": \"f19473e5-fb8a-42cf-b5e8-50ff0deed766\"\n" +
                "    }\n" +
                "  }\n" +
                "]";
        List<TestSpecification> testSpecList = mapper.readValue(test, new TypeReference<List<TestSpecification>>() {
        });
        String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testSpecList);


        log.info(loadTestJson.toString());
        assertTrue(loadTestJson.contains("command_replacement_map"));

    }

    @Test
    public void testInstanciateTestConfigFromJsonWithFILEReference() throws Exception {
        String test = "" +
                "  {\n" +
                "    \"command_url\": \"https://server.privatedns.zone/rest/\",\n" +
                "    \"command_contenttype\": \"application/json\",\n" +
                "    \"command_http_post\": true,\n" +
                "    \"command_timeout_milliseconds\": 5000,\n" +
                "    \"command_template\": \"FILE:./pom.xml\",\n" +
                "    \"command_replacement_map\": {\n" +
                "    }\n" +
                "  }\n" +
                "";
        TestSpecification jsonLoadtest = mapper.readValue(test, TestSpecification.class);

        TestSpecification testSpecification = jsonLoadtest.clone();
        testSpecification.resolveVariables(null, null, null);


        String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testSpecification);


        log.info(testSpecification.toString());
        assertTrue(loadTestJson.contains("command_replacement_map"));
        assertTrue(loadTestJson.contains("HTTPLoadTest-baseline"));

    }

    @Test
    public void readTestSpecificationWithExternalTemplateMappingFromFile() throws Exception {
        TestServer testServer;
        testServer = new TestServer(getClass());
        testServer.start();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("loadtest_setup/specifications/read/TestReadConfigReadTestSpecification.json").getFile());
            List<TestSpecification> readTestSpec = new ArrayList<>();
            readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
            });
            readTestSpec.forEach(ts -> ts.setCommand_url(ts.getCommand_url().replace("http://localhost:8086/HTTPLoadTest-baseline", testServer.getUrl())));
            for (TestSpecification testSpecificationo : readTestSpec) {
                TestSpecification testSpecification = testSpecificationo.clone();

                assertTrue(testSpecification.getCommand_url().length() > 0);
                //testSpecification.setCommand_template("FILE:./pom.xml");
                testSpecification.resolveVariables(null, null, null);
                assertTrue(!testSpecification.getCommand_template().contains("FILE:"));
                testSpecification.resolveVariables(null, null, null);
                Command command = CommandFactory.createHystrixPostCommand(testSpecification, new AtomicInteger());
                String result = command.execute();
                log.warn(result);

            }
            //      assertTrue(fileLoadtest.getTest_id().equalsIgnoreCase("TestID"));
        } finally {
            testServer.stop();
        }
    }


    @Test
    public void testURICreate() {
        String url = "https://odn1-thinkehr-cluster03.privatedns.zone/rest/v1/composition/#fizzle(option:#compositionIds)?format=STRUCTURED";

        Map<String, String> replacements = new HashMap<>();

        replacements.put("#compositionIds", UUID.randomUUID().toString());

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(url);

        URI uri = URI.create(result);
        assertTrue(uri != null);
    }
}
