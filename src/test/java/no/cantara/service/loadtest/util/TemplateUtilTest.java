package no.cantara.service.loadtest.util;

import no.cantara.util.Configuration;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

public class TemplateUtilTest {

    Map<String, String> replacements = new HashMap<>();

    @Test
    public void testTemplateUtilSimple() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #BrukerID before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced TestBruker before this").matcher(result).matches());
    }

    @Test
    public void testTemplateUtilSimpleMixed() {
        replacements.put("#BrukerID", "#fizzle(HEX:3234)");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #BrukerID before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced [A-F0-9]{4} before this").matcher(result).matches());
    }


    @Test
    public void testTemplateUtilWithCharacterFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(chars:text) before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced [A-Za-z]{4} before this").matcher(result).matches());
    }

    @Test
    public void testTemplateUtilWithDigitsFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(digits:3234) before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced [0-9]{4} before this").matcher(result).matches());
    }

    @Test
    public void testTemplateUtilWithHEXFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(HEX:3234) before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced [A-F0-9]{4} before this").matcher(result).matches());
    }

    @Test
    public void testTemplateUtilWithSetFizzling() {
        replacements.put("#BrukerID", "TestBruker");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #fizzle(option:yes, no, here, there) before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced (yes|no|here|there) before this").matcher(result).matches());
    }

    @Test
    public void testTemplateUtilWithSetFizzlingFromVariable() {
        replacements.put("#code", "[ \"b9210739319b13582b42fa89a16432c55345f847\" ]");
        String template = "Text to be replaced #fizzle(optionvalue:#code) before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced b9210739319b13582b42fa89a16432c55345f847 before this").matcher(result).matches());
    }

    @Test
    public void testVariableUtilWithSetFizzling() {
        replacements.put("#BrukerID", "#fizzle(option:yes, no, here, there)");
        replacements.put("#Passord", "TestPassord");
        String template = "Text to be replaced #BrukerID before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced (yes|no|here|there) before this").matcher(result).matches());
    }

    @Test
    public void testVariableUtilWithSetFizzling2() {
        replacements.put("#BrukerID", "Petter");
        replacements.put("#Passord", "#BrukerID");
        String template = "Text to be replaced #fizzle(option:#Passord) before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced Petter before this").matcher(result).matches());
    }

    @Test
    public void testTimestampFizzleFunctionWorks() {
        replacements.put("#now", "#Fizzle(timestamp:yyyy-MM-dd HH:mm:ss.SSSX)");
        String template = "Text to be replaced #(now) before this";

        String result = new TemplateUtil(replacements).updateTemplateWithValuesFromMap(template);
        assertTrue(Pattern.compile("Text to be replaced [0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z before this").matcher(result).matches());
    }

    @Test
    public void loadTemplateFile() {
        String jsonContent = Configuration.convertStreamToString(Configuration.loadByName("loadtest_setup/specifications/write/templates/write_resttestwrite_payload_1.json"));
        System.out.println(jsonContent);
    }

}