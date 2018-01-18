package no.cantara.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class ConfigurationTest {


    @Test
    public void testLoadFromFile() {
        String myPom = Configuration.loadFromDiskByName("./pom.xml");
        assertTrue(myPom.contains("HTTPLoadTest"));
    }
}