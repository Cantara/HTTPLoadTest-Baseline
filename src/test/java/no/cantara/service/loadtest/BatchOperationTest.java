package no.cantara.service.loadtest;

import no.cantara.service.testsupport.TestServerPlain;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class BatchOperationTest {

    private final static Logger log = LoggerFactory.getLogger(BatchOperationTest.class);
    private static String testURL = "http://localhost:8086/HTTPLoadTest-baseline/loadTest";


    private TestServerPlain testServer;

    @BeforeClass
    public void startServer() throws Exception {
        testServer = new TestServerPlain();
        testServer.start();
        Thread.sleep(2000);
    }

    @AfterClass
    public void stop() {
        testServer.stop();
    }

    @Test
    public void testZipUpload() {
        String s;
        Process p;
        testURL = testServer.getUrl();
        log.trace("Calling {}", testServer.getUrl());
        try {
            String command = "curl -u " +
                    Configuration.getString("login.admin.user") + ":" + Configuration.getString("login.admin.password") +
                    " -F \"file=@./src/test/resources/loadtest_setup.zip;filename=loadtest_setup.zip\" "
                    + testURL + "/loadTest/zip";
            log.info("Calling: \n  \"{}\"", command);
            p = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
                log.info("testZipUpload-line: " + s);
            p.waitFor();
            log.info("testZipUpload-exit: " + p.exitValue());
            p.destroy();
            assertTrue(p.exitValue() == 0);
        } catch (Exception e) {
            log.error("Exception in testing zip-upload from commandline (linux)", e);
            fail("Exception in testing zip-upload from commandline (linux)");
        }
    }
}