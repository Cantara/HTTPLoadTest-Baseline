package no.cantara.service.model;

import no.cantara.service.commands.config.ConstantValue;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class TestSpecification_http_authstring_Test {


    @Test
    public void testAuthStringVersionsUP1() {

        TestSpecification testSpecification = new TestSpecification();
        testSpecification.setCommand_http_authstring("admin:admin");
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
        testSpecification.resolveVariables(null, null, null);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
        Map<String, String> dummyMap = new HashMap<>();
        testSpecification.resolveVariables(dummyMap, dummyMap, dummyMap);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
    }

    @Test
    public void testAuthStringVersionsUP2() {

        TestSpecification testSpecification = new TestSpecification();
        testSpecification.setCommand_http_authstring("admin/admin");
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
        testSpecification.resolveVariables(null, null, null);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
        Map<String, String> dummyMap = new HashMap<>();
        testSpecification.resolveVariables(dummyMap, dummyMap, dummyMap);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
    }

    @Test
    public void testAuthStringVersionsPreCalculated() {

        TestSpecification testSpecification = new TestSpecification();
        testSpecification.setCommand_http_authstring("Basic YWRtaW46YWRtaW4=");
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
        testSpecification.resolveVariables(null, null, null);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
        Map<String, String> dummyMap = new HashMap<>();
        testSpecification.resolveVariables(dummyMap, dummyMap, dummyMap);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Basic"));
    }


    @Test
    public void testAuthStringVersionsOAuth() {

        TestSpecification testSpecification = new TestSpecification();
        testSpecification.setCommand_http_authstring("Bearer " + ConstantValue.ATOKEN);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
        testSpecification.resolveVariables(null, null, null);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
        Map<String, String> dummyMap = new HashMap<>();
        testSpecification.resolveVariables(dummyMap, dummyMap, dummyMap);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
    }


    @Test
    public void testAuthStringVersionsOAuth3() {

        TestSpecification testSpecification = new TestSpecification();
        testSpecification.setCommand_http_authstring("Bearer [" + ConstantValue.ATOKEN + "]");
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
        testSpecification.resolveVariables(null, null, null);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
        Map<String, String> dummyMap = new HashMap<>();
        testSpecification.resolveVariables(dummyMap, dummyMap, dummyMap);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
    }

   /* @Test
    public void testAuthStringVersionsEmpty() {

        TestSpecification testSpecification = new TestSpecification();
        testSpecification.setCommand_http_authstring("");
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
        testSpecification.resolveVariables(null, null, null);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
        Map<String, String> dummyMap = new HashMap<>();
        testSpecification.resolveVariables(dummyMap, dummyMap, dummyMap);
        assertTrue(testSpecification.getCommand_http_authstring().contains("Bearer"));
    }*/
}