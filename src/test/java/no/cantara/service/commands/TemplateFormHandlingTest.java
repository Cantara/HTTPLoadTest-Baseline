package no.cantara.service.commands;

import org.testng.annotations.Test;

public class TemplateFormHandlingTest {

    @Test
    public void testTemplateTypeCheck() {
        String template = "grant_type=authorization_code&code=fa21e6308beb8715ee6aa9f03e71862764fbc8e1&redirect_uri=#REDIRECT_URI/authorize";
        String[] formParams = template.split("&");
        for (String formParam : formParams) {
            String key = formParam.substring(0, formParam.indexOf("="));
            String value = formParam.substring(formParam.indexOf("=") + 1, formParam.length());

        }
    }

    /*@Test
    public void testLocationToJson() {
        String locationExample = "Location: https://qa-no.melinmedical.com/mmsysapi/api/authorize?code=317c2fba518b354457733c0d19484c43e7e5bfb4&state=placeholder";
        String[] formParams = locationExample.split("\?");
        for (String formParam : formParams) {
            String key = formParam.substring(0, formParam.indexOf("="));
            String value = formParam.substring(formParam.indexOf("=") + 1, formParam.length());

        }

    }*/

}
