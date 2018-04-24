package no.cantara.service.loadtest.util;

import org.testng.annotations.Test;

import java.io.FileInputStream;

public class UnzipStreamTest {


    @Test
    public void testUnzipFilename() throws Exception {
        String filename = "./src/test/resources/loadtest_setup.zip";
        FileInputStream fileInputStream = new FileInputStream(filename);

        UnzipStream.unzip(fileInputStream);
    }
}