package no.cantara.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class TestSpecificationMappingTest {

    private static final Logger log = LoggerFactory.getLogger(TestSpecificationMappingTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();


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

        testSpecification.setCommand_url("http://test.tull.no");
        testSpecification.setCommand_contenttype("applications.xml");
        testSpecification.setCommand_template(soaptemplate);
        testSpecification.setCommand_template(jsonTemplate);
        testSpecification.setCommand_replacement_map(replacements);

        List<TestSpecification> readTestSpec = new ArrayList<>();

        readTestSpec.add(testSpecification);
        String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpec);
        List<TestSpecification> testSpecification1 = mapper.readValue(loadTestJson, new TypeReference<List<TestSpecification>>() {
        });

//        assertTrue(loadTestConfig.getTest_id().equalsIgnoreCase(mappedLoadtest.getTest_id()));


    }

    @Test
    public void readTestSpecificationMappingFromFile() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("readconfig.json").getFile());
        List<TestSpecification> readTestSpec = new ArrayList<>();
        readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
        });
        for (TestSpecification testSpecification : readTestSpec) {
            assertTrue(testSpecification.getCommand_url().length() > 0);
        }
        //      assertTrue(fileLoadtest.getTest_id().equalsIgnoreCase("TestID"));
    }
}
