package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

class TestSetupHelper {

    private static final Logger log = LoggerFactory.getLogger(TestSetupHelper.class);

    private final ObjectMapper mapper;

    private List<TestSpecification> readSpecification;
    private List<TestSpecification> writeSpecification;

    TestSetupHelper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    List<TestSpecification> getReadSpecification() {
        return readSpecification;
    }

    List<TestSpecification> getWriteSpecification() {
        return writeSpecification;
    }

    String getLoadTestConfigJson() {
        return "{\n" +
                "  \"test_id\": \"HTTPLoadTest-baseline-throughput-test\",\n" +
                "  \"test_name\": \"Throughput test of the HTTPLoadTest-baseline itself\",\n" +
                "  \"test_no_of_threads\": 1,\n" +
                "  \"test_read_write_ratio\": 50,\n" +
                "  \"test_sleep_in_ms\": 0,\n" +
                "  \"test_randomize_sleeptime\": false,\n" +
                "  \"test_duration_in_seconds\": 10,\n" +
                "  \"test_global_variables_map\": {\n" +
                "    \"#Passord\": \"TestPassord\",\n" +
                "    \"#BrukerID\": \"TestBruker\"\n" +
                "  }\n" +
                "}";
    }

    void generateSpecifications(int n, int testTargetServicePort) {
        LinkedHashSet<String> domains = new LinkedHashSet<>();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < n; i++) {
            String c1 = String.valueOf(alphabet.charAt((i / alphabet.length())));
            String c2 = String.valueOf(alphabet.charAt((i % alphabet.length())));
            domains.add(c1 + c2);
        }
        generateSpecifications(domains, testTargetServicePort);
    }

    void generateSpecifications(Collection<String> domains, int testTargetServicePort) {
        ArrayNode readSpecificationNode = new ArrayNode(JsonNodeFactory.instance);
        ArrayNode writeSpecificationNode = new ArrayNode(JsonNodeFactory.instance);

        generateWriteTemplateFiles(domains);
        addReadSpecification(readSpecificationNode, domains, testTargetServicePort);
        addWriteSpecification(writeSpecificationNode, domains, testTargetServicePort);
        try {
            String readSpecification = mapper.writeValueAsString(readSpecificationNode);
            this.readSpecification = mapper.readValue(readSpecification, new TypeReference<List<TestSpecification>>() {
            });
            String writeSpecification = mapper.writeValueAsString(writeSpecificationNode);
            this.writeSpecification = mapper.readValue(writeSpecification, new TypeReference<List<TestSpecification>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void addWriteSpecification(ArrayNode writeSpecification, Collection<String> domains, int testTargetServicePort) {
        ObjectNode managedDomainCommand = new ObjectNode(JsonNodeFactory.instance);
        managedDomainCommand.put("command_url",
                String.format("http://localhost:%d/data/#domain/#mrid?sync=true",
                        testTargetServicePort)
        );
        managedDomainCommand.put("command_contenttype", "application/json");
        managedDomainCommand.put("command_http_post", true);
        managedDomainCommand.put("command_timeout_milliseconds", 500);
        managedDomainCommand.put("command_template", "FILE:target/#domain.json");
        ObjectNode commandReplacementMap = new ObjectNode(JsonNodeFactory.instance);
        commandReplacementMap.put("#domain", "#fizzle(option:" + domains.stream().collect(Collectors.joining(", ")) + ")");
        commandReplacementMap.put("#mrid", "#fizzle(HEX:12)C8D2B7-0EB3-4A6D-91BB-A7451649F2F6");
        commandReplacementMap.put("#now", "#fizzle(timestamp)");
        managedDomainCommand.set("command_replacement_map", commandReplacementMap);
        ObjectNode commandResponseMap = new ObjectNode(JsonNodeFactory.instance);
        managedDomainCommand.set("command_response_map", commandResponseMap);
        writeSpecification.add(managedDomainCommand);
    }

    void generateWriteTemplateFiles(Collection<String> domains) {
        for (String domain : domains) {
            ObjectNode template = new ObjectNode(JsonNodeFactory.instance);
            template.put("domain", domain);
            template.put("id", "#mrid");
            template.put("version", "1.0");
            template.put("name", "My Name Is Myself");
            template.put("description", "Some description");
            template.put("createdBy", "me");
            template.put("created", "#now");
            template.put("modified", "#now");
            template.put("accessed", "#now");
            ArrayNode composite = new ArrayNode(JsonNodeFactory.instance);
            ObjectNode compositeValue1 = new ObjectNode(JsonNodeFactory.instance);
            compositeValue1.put("lang", "en");
            compositeValue1.put("code", "123");
            ObjectNode compositeValue2 = new ObjectNode(JsonNodeFactory.instance);
            compositeValue2.put("first", "1");
            compositeValue2.put("second", "2");
            composite.add(compositeValue1);
            composite.add(compositeValue2);
            template.set("composite", composite);
            try {
                mapper.writeValue(new File("target", domain + ".json"), template);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void addReadSpecification(ArrayNode readSpecification, Collection<String> domains, int testTargetServicePort) {
        ObjectNode managedDomainCommand = new ObjectNode(JsonNodeFactory.instance);
        managedDomainCommand.put("command_url",
                String.format("http://localhost:%d/ns/#domain/some/path/to/#mrid",
                        testTargetServicePort)
        );
        managedDomainCommand.put("command_contenttype", "application/json");
        managedDomainCommand.put("command_http_post", false);
        managedDomainCommand.put("command_timeout_milliseconds", 250);
        managedDomainCommand.put("command_template", getLoadTestConfigJson());
        ObjectNode commandReplacementMap = new ObjectNode(JsonNodeFactory.instance);
        commandReplacementMap.put("#domain", "#fizzle(option:" + domains.stream().collect(Collectors.joining(", ")) + ")");
        managedDomainCommand.set("command_replacement_map", commandReplacementMap);
        ObjectNode commandResponseMap = new ObjectNode(JsonNodeFactory.instance);
        managedDomainCommand.set("command_response_map", commandResponseMap);
        readSpecification.add(managedDomainCommand);
    }
}
