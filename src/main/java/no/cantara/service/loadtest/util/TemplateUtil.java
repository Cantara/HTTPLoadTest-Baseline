package no.cantara.service.loadtest.util;

import no.cantara.service.loadtest.Fizzler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateUtil {

    private static final Logger log = LoggerFactory.getLogger(TemplateUtil.class);
    private static final List<String> fizzleKeyList = new ArrayList<String>() {{
        add("#fizzle(chars:");
        add("#fizzle(digits:");
        add("#fizzle(U_chars:");
        add("#fizzle(L_chars:");
        add("#fizzle(HEX:");
        add("#fizzle(option:");
    }};

    public static String updateTemplateWithValuesFromMap(String template, Map<String, String> templatereplacementMap) {
        if (template == null) {
            return "";
        }
        // We do variable replacements first
        if (templatereplacementMap != null) {
            for (String key : templatereplacementMap.keySet()) {
                if (template.contains(key)) {
                    template = template.replaceAll(key, templatereplacementMap.get(key));
                    log.trace("Replaced {} with {}", key, templatereplacementMap.get(key));
                }
            }
        }
        // and then fizzle
        for (String fizzlekey : fizzleKeyList) {
            if (template.contains(fizzlekey)) {
                template = fizzleTemplate(template, fizzlekey);
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
        if (fizzleKey.equalsIgnoreCase(fizzleKeyList.get(2))) {
            return frontTemplate + Fizzler.getRandomUppercaseCharacter(fizzSize) + backTemplate;
        }
        if (fizzleKey.equalsIgnoreCase(fizzleKeyList.get(3))) {
            return frontTemplate + Fizzler.getRandomLowercaseCharacter(fizzSize) + backTemplate;
        }
        if (fizzleKey.equalsIgnoreCase(fizzleKeyList.get(4))) {
            return frontTemplate + Fizzler.getRandomHEXCharacter(fizzSize) + backTemplate;
        }
        if (fizzleKey.equalsIgnoreCase(fizzleKeyList.get(5))) {
            return frontTemplate + Fizzler.getRandomSetValue(backtemp.substring(0, backtemp.indexOf(")") - 1)) + backTemplate;
        }
        return template;
    }
}
