package org.xmpp;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christian Schudt
 */
public class TruncatedBinaryExponentialBackOffStrategyTest {

    @Test
    public void test() {
        TruncatedBinaryExponentialBackOffStrategy truncatedBinaryExponentialBackOffStrategy = new TruncatedBinaryExponentialBackOffStrategy(60, 4);
        int first = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(0);

        Assert.assertTrue(first >= 0 && first < 60);
        int second = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(1);
        Assert.assertTrue(second >= 0 && second < 180);
        int third = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(2);
        Assert.assertTrue(third >= 0 && third < 420);
        int fourth = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(3);
        Assert.assertTrue(fourth >= 0 && fourth < 900);
        int fifth = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(4);
        Assert.assertTrue(fifth >= 0 && fifth < 1860);
        int sixth = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(4);
        Assert.assertTrue(sixth >= 0 && sixth < 1860);
        int seventh = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(4);
        Assert.assertTrue(seventh >= 0 && seventh < 1860);
    }
}
