package no.cantara.service.commands.config;

import no.cantara.service.util.Configuration;

public class ConfigValue {

    public static String CONFIGSERVICE_URL ="";
    public static String CONFIGSERVICE_USERNAME ="";
    public static String CONFIGSERVICE_PASSWORD ="";
    public static String LOGIN_READ_USERNAME ="";
    public static String LOGIN_READ_PASSWORD ="";
    public static String LOGIN_ADMIN_USERNAME ="";
    public static String LOGIN_ADMIN_PASSWORD ="";


    public static int SERVICE_PORT=8087;
    public static String SERVICE_CONTEXT="/dashboard";

    static {
        CONFIGSERVICE_PASSWORD = Configuration.getString("configservice.password");
        CONFIGSERVICE_USERNAME = Configuration.getString("configservice.username");
        LOGIN_READ_USERNAME = Configuration.getString("login.user");
        LOGIN_READ_PASSWORD = Configuration.getString("login.password");
        LOGIN_ADMIN_USERNAME = Configuration.getString("login.admin.user");
        LOGIN_ADMIN_PASSWORD = Configuration.getString("login.admin.password");

        CONFIGSERVICE_URL = Configuration.getString("configservice.url");
        SERVICE_PORT = Configuration.getInt("service.port", 8087);
    }

}
