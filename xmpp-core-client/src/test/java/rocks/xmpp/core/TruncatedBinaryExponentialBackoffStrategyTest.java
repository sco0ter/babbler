package rocks.xmpp.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.session.TruncatedBinaryExponentialBackoffStrategy;

/**
 * @author Christian Schudt
 */
public class TruncatedBinaryExponentialBackoffStrategyTest {

    @Test
    public void test() {
        TruncatedBinaryExponentialBackoffStrategy truncatedBinaryExponentialBackoffStrategy = new TruncatedBinaryExponentialBackoffStrategy(60, 4);
        int first = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(0);

        Assert.assertTrue(first >= 0 && first < 60);
        int second = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(1);
        Assert.assertTrue(second >= 0 && second < 180);
        int third = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(2);
        Assert.assertTrue(third >= 0 && third < 420);
        int fourth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(3);
        Assert.assertTrue(fourth >= 0 && fourth < 900);
        int fifth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(4);
        Assert.assertTrue(fifth >= 0 && fifth < 1860);
        int sixth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(5);
        Assert.assertTrue(sixth >= 0 && sixth < 1860);
        int seventh = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(6);
        Assert.assertTrue(seventh >= 0 && seventh < 1860);
    }
}
