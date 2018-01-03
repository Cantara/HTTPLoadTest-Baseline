package no.cantara.service.loadtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateUtil {

    public static final List<String> fizzleKeyList = new ArrayList<String>() {{
        add("#fizzle(chars:");
        add("#fizzle(digits:");
    }};

    public static String updateTemplateWithvaluesFromMap(String template, Map<String, String> templatereplacementMap) {
        if (template == null) {
            return "";
        }
        for (String fizzlekey : fizzleKeyList) {
            if (template.contains(fizzlekey)) {
                template = fizzleTemplate(template, fizzlekey);
            }
        }
        if (templatereplacementMap == null) {
            return template;
        }
        for (String key : templatereplacementMap.keySet()) {
            if (template.contains(key)) {
                template = template.replaceAll(key, templatereplacementMap.get(key));
            }
        }
        return template;
    }

    private static String fizzleTemplate(String template, String fizzleKey) {

        String frontTemplate = template.substring(0, template.indexOf(fizzleKey));
        String backtemp = template.substring(template.indexOf(fizzleKey) + fizzleKey.length(), template.length());
        String backTemplate = backtemp.substring(backtemp.indexOf(")") + 1, backtemp.length());
        int fizzSize = template.length() - frontTemplate.length() - backTemplate.length() - fizzleKey.length() - 1;
        if (fizzleKey.equalsIgnoreCase(fizzleKeyList.get(0))) {
            return frontTemplate + Fizzler.getRandomCharacters(fizzSize) + backTemplate;
        }
        if (fizzleKey.equalsIgnoreCase(fizzleKeyList.get(1))) {
            return frontTemplate + Fizzler.getRandomDigits(fizzSize) + backTemplate;
        }
        return template;
    }
}
