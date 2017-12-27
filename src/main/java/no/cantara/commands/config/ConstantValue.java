package no.cantara.commands.config;

import no.cantara.service.util.Configuration;

public class ConstantValue {

    public static final int COMMAND_TIMEOUT = 10000;

    public static final String ATOKEN = Configuration.getString("dummy.atoken");  //"AsT5OjbzRn430zqMLgV3Ia";
    public static final String UTOKEN = Configuration.getString("dummy.utoken");  //"usT5OjbzRn430zqMLgV3Ia";


}
