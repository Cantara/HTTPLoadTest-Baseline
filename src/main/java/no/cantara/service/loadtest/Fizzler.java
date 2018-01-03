package no.cantara.service.loadtest;

import java.util.Random;

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

}

