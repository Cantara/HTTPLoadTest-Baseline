package no.cantara.service.loadtest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class TemplateUtilTest {

    Map<String, String> replacements = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(TemplateUtilTest.class);

    @Test
    public void testTemplateUtilSimple() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #BrukerID before this";

        String result = TemplateUtil.updateTemplateWithvaluesFromMap(template, replacements);
        log.trace("Fizzled result: {}", result);
        assertTrue(result.contains(template.substring(0, template.indexOf("#BrukerID"))));


    }

    @Test
    public void testTemplateUtilSimpleMixed() {
        replacements.put("#BrukerID", "#fizzle(HEX:3234)");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #BrukerID before this";

        String result = TemplateUtil.updateTemplateWithvaluesFromMap(template, replacements);
        log.trace("Fizzled result: {}", result);
        assertTrue(result.contains(template.substring(0, template.indexOf("#BrukerID"))));


    }


    @Test
    public void testTemplateUtilWithCharacterFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(chars:text) before this";

        String result = TemplateUtil.updateTemplateWithvaluesFromMap(template, replacements);
        log.trace("Fizzled result: {}", result);
        assertTrue(result.contains(template.substring(0, template.indexOf("#fizzle"))));


    }

    @Test
    public void testTemplateUtilWithDigitsFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(digits:3234) before this";

        String result = TemplateUtil.updateTemplateWithvaluesFromMap(template, replacements);
        log.trace("Fizzled result: {}", result);
        assertTrue(result.contains(template.substring(0, template.indexOf("#fizzle"))));


    }

    @Test
    public void testTemplateUtilWithHEXFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(HEX:3234) before this";

        String result = TemplateUtil.updateTemplateWithvaluesFromMap(template, replacements);
        log.trace("Fizzled result: {}", result);
        assertTrue(result.contains(template.substring(0, template.indexOf("#fizzle"))));


    }

    @Test
    public void testTemplateUtilWithSetFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(option:yes, no, here, there) before this";

        String result = TemplateUtil.updateTemplateWithvaluesFromMap(template, replacements);
        log.trace("Fizzled result: {}", result);
        assertTrue(result.contains(template.substring(0, template.indexOf("#fizzle"))));


    }
}