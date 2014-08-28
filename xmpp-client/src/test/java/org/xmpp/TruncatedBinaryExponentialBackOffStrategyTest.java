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
        System.out.println(first);
        Assert.assertTrue(first > 0 && first < 60);
        int second = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(1);
        System.out.println(second);
        Assert.assertTrue(second > 0 && second < 180);
        int third = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(2);
        System.out.println(third);
        Assert.assertTrue(third > 0 && third < 420);
        int fourth = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(3);
        System.out.println(fourth);
        Assert.assertTrue(fourth > 0 && fourth < 900);
        int fifth = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(4);
        System.out.println(fifth);
        Assert.assertTrue(fifth > 0 && fifth < 1860);
        int sixth = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(4);
        System.out.println(sixth);
        Assert.assertTrue(sixth > 0 && sixth < 1860);
        int seventh = truncatedBinaryExponentialBackOffStrategy.getNextReconnectionAttempt(4);
        System.out.println(seventh);
        Assert.assertTrue(seventh > 0 && seventh < 1860);
    }
}
