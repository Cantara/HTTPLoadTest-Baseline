package no.cantara.service.loadtest.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoadTestResultUtilTest {

    @Test
    public void thatToRoundedStringWorks() {
        Assert.assertEquals(LoadTestResultUtil.toRoundedString(2.3506219573), "2.351");
        Assert.assertEquals(LoadTestResultUtil.toRoundedString(2.3504873639), "2.350");
    }
}
