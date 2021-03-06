package com.echsylon.kraken.request;

import com.echsylon.atlantis.Atlantis;
import com.echsylon.blocks.callback.DefaultRequest;
import com.echsylon.kraken.dto.DepositMethod;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.echsylon.kraken.TestHelper.getKrakenInstance;
import static com.echsylon.kraken.TestHelper.startMockServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * These test cases will test the "deposit methods" feature of the Android
 * Kraken SDK.
 * <p>
 * The tests will take advantage of the fact that the Kraken implementation
 * returns a {@code DefaultRequest} object. Since the {@code DefaultRequest}
 * class extends {@code FutureTask} we can block the test thread until a result
 * is produced.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 25)
public class DepositMethodsTest {

    private Atlantis atlantis;

    @After
    public void after() {
        atlantis.stop();
        atlantis = null;
    }


    @Test
    public void requestingDepositMethod_shouldReturnArrayOfParsedObjects() throws Exception {
        atlantis = startMockServer("POST", "/0/private/DepositMethods",
                "{'error': [], 'result': [{" +
                        " 'method': 'Ether (Hex)'," +
                        " 'limit': false," +
                        " 'fee': '0.0000000000'," +
                        " 'gen-address': true}]}");

        String key = "key";
        String secret = "c2VjcmV0";

        DepositMethod[] result =
                ((DefaultRequest<DepositMethod[]>) getKrakenInstance(key, secret)
                        .getDepositMethods()
                        .enqueue())
                        .get(1, SECONDS);

        assertThat(result.length, is(1));
        assertThat(result[0].method, is("Ether (Hex)"));
        assertThat(result[0].limit, is("false"));
        assertThat(result[0].fee, is("0.0000000000"));
        assertThat(result[0].hasGeneratedAddress, is(true));
    }

}
