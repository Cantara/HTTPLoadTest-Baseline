package no.cantara.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSpecification implements Serializable {
    private String command_url;
    private String command_contenttype = "application/json";
    private String command_http_authstring;
    private boolean command_http_post = false;
    private int command_timeout_milliseconds = 2000;
    private String command_template = "";
    private Map<String, String> command_replacement_map = TestSpecificationLoader.getGlobal_command_replacement_map();
    private Map<String, String> command_response_map = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(TestSpecification.class);


    public TestSpecification(@JsonProperty("command_url") String command_url,
                             @JsonProperty("command_contenttype") String command_contenttype,
                             @JsonProperty("command_http_authstring") String command_http_authstring,
                             @JsonProperty("command_http_post") String command_http_post,
                             @JsonProperty("command_timeout_milliseconds") String command_timeout_milliseconds,
                             @JsonProperty("command_template") String command_template,
                             @JsonProperty("command_replacement_map") Map<String, String> command_replacement_map,
                             @JsonProperty("command_response_map") Map<String, String> command_response_map) {
        this.command_url = command_url;
        this.command_http_post = Boolean.parseBoolean(command_http_post);
        this.command_http_authstring = command_http_authstring;
        this.command_timeout_milliseconds = Integer.valueOf(command_timeout_milliseconds);
        this.command_contenttype = command_contenttype;
        this.command_template = command_template;
        this.command_replacement_map = TestSpecificationLoader.getGlobal_command_replacement_map();
        addMapToCommand_replacement_map(command_replacement_map);
        this.command_response_map = command_response_map;

    }

    public TestSpecification() {
    }


    public String getCommand_url() {
        return command_url;
    }

    public void setCommand_url(String command_url) {
        this.command_url = command_url;
    }

    public String getCommand_contenttype() {
        return command_contenttype;
    }

    public void setCommand_contenttype(String command_contenttype) {
        this.command_contenttype = command_contenttype;
    }

    public String getCommand_template() {
        return command_template;
    }

    public void setCommand_template(String command_template) {
        this.command_template = command_template;
    }

    public Map<String, String> getCommand_replacement_map() {
        if (command_replacement_map == null) {
            command_replacement_map = TestSpecificationLoader.getGlobal_command_replacement_map();
        }

        return command_replacement_map;
    }

    public void setCommand_replacement_map(Map<String, String> command_replacement_map) {
        this.command_replacement_map = TestSpecificationLoader.getGlobal_command_replacement_map();
        addMapToCommand_replacement_map(command_replacement_map);
    }

    public void addMapToCommand_replacement_map(Map<String, String> map_to_add) {

        //map_to_add.forEach(this.command_replacement_map::putIfAbsent);
        this.command_replacement_map.putAll(map_to_add);
        //this.command_replacement_map.putAll(Maps.difference(command_replacement_map, map_to_add).entriesOnlyOnLeft());
    }


    public boolean isCommand_http_post() {
        return command_http_post;
    }

    public void setCommand_http_post(boolean command_http_post) {
        this.command_http_post = command_http_post;
    }

    public int getCommand_timeout_milliseconds() {
        return command_timeout_milliseconds;
    }

    public void setCommand_timeout_milliseconds(int command_timeout_milliseconds) {
        this.command_timeout_milliseconds = command_timeout_milliseconds;
    }

    public String getCommand_http_authstring() {
        if (command_http_authstring != null && command_http_authstring.split("/").length == 2) {
            String[] upfields = command_http_authstring.split("/");
            String name = upfields[0];
            String password = upfields[1];

            String authString = name + ":" + password;
            System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
//            String authStringEnc = new String(authEncBytes);
            String authStringEnc = "Basic " + new String(authEncBytes);
            return authStringEnc;

        }
        if (command_http_authstring != null && command_http_authstring.split(":").length == 2) {
            String[] upfields = command_http_authstring.split(":");
            String name = upfields[0];
            String password = upfields[1];

            String authString = name + ":" + password;
            System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
//            String authStringEnc = new String(authEncBytes);
            String authStringEnc = "Basic " + new String(authEncBytes);
            return authStringEnc;

        }
        return command_http_authstring;
    }

    public void setCommand_http_authstring(String command_http_authstring) {
        this.command_http_authstring = command_http_authstring;
    }

    public Map<String, String> getCommand_response_map() {
        if (command_response_map == null) {
            command_response_map = new HashMap<>();
        }
        return command_response_map;
    }

    public void setCommand_response_map(Map<String, String> command_response_map) {
        this.command_response_map = command_response_map;
    }

    public void loadTemplateReference() {
        if (getCommand_template().startsWith("FILE:")) {
            try {
                String contents = new String(Files.readAllBytes(Paths.get(getCommand_template().substring(5, getCommand_template().length()))));
                setCommand_template(contents);
            } catch (Exception e) {
                log.error("Unable to load external referenced TestSpecification remplate", e);
            }

        }
    }

    @Override
    public String toString() {
        return "TestSpecification{" +
                "command_url='" + command_url + '\'' +
                ", command_http_post=" + command_http_post +
                '}';
    }
}
