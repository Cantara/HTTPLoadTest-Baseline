package no.cantara.service.commands.config;

import no.cantara.util.Configuration;

public class ConstantValue {

    public static final String HTML_CONTENT_TYPE = "text/html; charset=utf-8";
    public static final int COMMAND_TIMEOUT = 10000;
    public static final String JSON_DATA = "jsondata";


    public static final String ATOKEN = Configuration.getString("dummy.atoken");  //"AsT5OjbzRn430zqMLgV3Ia";
    public static final String UTOKEN = Configuration.getString("dummy.utoken");  //"usT5OjbzRn430zqMLgV3Ia";
}
