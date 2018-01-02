package no.cantara.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class TestSpecification {
    private String command_url;
    private String command_contenttype = "application/json";
    private boolean command_http_post = false;
    private int command_timeout_milliseconds = 2000;
    private String command_template = "";
    private Map<String, String> command_replacement_map = new HashMap<>();


    /**
     * {
     * "command_url": "http://test.tull.no",
     * "command_contenttype": "application/json",
     * "command_template": "",
     * "command_replacement_map": ""
     * }
     */
    public TestSpecification(@JsonProperty("command_url") String command_url,
                             @JsonProperty("command_contenttype") String command_contenttype,
                             @JsonProperty("command_http_post") String command_http_post,
                             @JsonProperty("command_timeout_milliseconds") String command_timeout_milliseconds,
                             @JsonProperty("command_template") String command_template,
                             @JsonProperty("command_replacement_map") Map<String, String> command_replacement_map) {
        this.command_url = command_url;
        this.command_http_post = Boolean.parseBoolean(command_http_post);
        this.command_timeout_milliseconds = Integer.valueOf(command_timeout_milliseconds);
        this.command_contenttype = command_contenttype;
        this.command_template = command_template;
        this.command_replacement_map = command_replacement_map;

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
        return command_replacement_map;
    }

    public void setCommand_replacement_map(Map<String, String> command_replacement_map) {
        this.command_replacement_map = command_replacement_map;
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
}
