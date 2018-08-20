package no.cantara.service.loadtest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Fizzler {

    private static Random r = new Random();

    public static String getRandomDigits(int numberOfDigits) {
        String result = "";
        for (int n = 0; n < numberOfDigits; n++) {
            result = result + getRandomDigit();
        }
        return result;
    }

    public static String generatePin() {
        int i = r.nextInt(10000) % 10000;
        java.text.DecimalFormat f = new java.text.DecimalFormat("0000");
        return f.format(i);

    }

    public static char getRandomDigit() {
        String abc = "0123456789";
        return abc.charAt(r.nextInt(abc.length()));
    }

    public static char getRandomHEX() {
        String abc = "0123456789ABCDEF";
        return abc.charAt(r.nextInt(abc.length()));
    }
    public static char getRandomUppercaseCharacter() {
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return abc.charAt(r.nextInt(abc.length()));
    }

    public static char getRandomLowercaseCharacter() {
        String abc = "abcdefghijklmnopqrstuvwxyz";
        return abc.charAt(r.nextInt(abc.length()));
    }

    public static char getRandomCharacter() {
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        return abc.charAt(r.nextInt(abc.length()));
    }

    public static String getRandomCharacters(int noOfChars) {
        String result = "";
        for (int n = 0; n < noOfChars; n++) {
            result = result + getRandomCharacter();
        }
        return result;
    }

    public static String getRandomLowercaseCharacter(int noOfChars) {
        String result = "";
        for (int n = 0; n < noOfChars; n++) {
            result = result + getRandomLowercaseCharacter();
        }
        return result;
    }

    public static String getRandomUppercaseCharacter(int noOfChars) {
        String result = "";
        for (int n = 0; n < noOfChars; n++) {
            result = result + getRandomUppercaseCharacter();
        }
        return result;
    }

    public static String getRandomHEXCharacter(int noOfChars) {
        String result = "";
        for (int n = 0; n < noOfChars; n++) {
            result = result + getRandomHEX();
        }
        return result;
    }

    public static String getRandomSetValue(String options) {
        String[] values = options.split(",");
        int selected = r.nextInt(values.length);
        return values[selected].trim();

    }

    // Quick un-escaping of json set
    public static String getRandomSetValueAsString(String options) {
        String[] values = options.split(",");
        int selected = r.nextInt(values.length);
        String jsonvalue = values[selected].trim();
        String stringValue = jsonvalue.replaceAll("\\[", "").replaceAll("\"", "").replaceAll("]", "").trim();
        return stringValue;

    }

    // #fizzle(substring(0,32):#testString)
    public static String getSubString(String indices, String myString) {
        String[] startAndStopIndex = indices.split("[, ]");
        int startIndex=Integer.parseInt(startAndStopIndex[0]);
        int stopIndex=Integer.parseInt(startAndStopIndex[1]);
        String result = myString.substring(startIndex, stopIndex);
        return result;

    }

    private static final ZoneId z = ZoneId.of("Z");
    private static final DateTimeFormatter iso_timestamp_formattter
            = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final Map<String, DateTimeFormatter> formatterByPattern = new ConcurrentHashMap<>();

    public static String getTimestamp(String formatPattern) {
        if (formatPattern == null || formatPattern.isEmpty()) {
            return ZonedDateTime.now(z).format(iso_timestamp_formattter); // default
        }
        DateTimeFormatter formatter = formatterByPattern.computeIfAbsent(formatPattern, p -> DateTimeFormatter.ofPattern(p));
        return ZonedDateTime.now(z).format(formatter);
    }
}

