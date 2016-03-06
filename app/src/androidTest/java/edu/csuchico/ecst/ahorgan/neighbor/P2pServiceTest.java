package edu.csuchico.ecst.ahorgan.neighbor;

import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.util.Pair;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.concurrent.TimeoutException;

import edu.csuchico.ecst.ahorgan.neighbor.p2p.P2pService;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class P2pServiceTest {
    private static final String TAG = "P2pServiceTest";
    public static final String TEST_STRING = "This is a string";
    public static final long TEST_LONG = 12345678L;
    private P2pService mService;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testWithBoundService() throws TimeoutException {
        Log.d(TAG, "testWithBoundService called");
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(InstrumentationRegistry.getTargetContext(),
                        P2pService.class);

        // Data can be passed to the service via the Intent.
        //serviceIntent.putExtra(P2pService.SEED_KEY, 42L);

        // Bind the service and grab a reference to the binder.
        IBinder binder = mServiceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        P2pService mService =
                ((P2pService.LocalBinder) binder).getService();

        // Verify that the service is working correctly.
        //assertThat(service.getRandomInt(), is(any(Integer.class)));
    }
}