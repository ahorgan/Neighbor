package edu.csuchico.ecst.ahorgan.neighbor;

import android.content.Context;
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

    @Test
    public void testWithStartedService() throws TimeoutException {
        Log.d(TAG, "testWithBoundService called");
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(InstrumentationRegistry.getTargetContext(),
                        P2pService.class);
        Context context = InstrumentationRegistry.getContext();

        // Data can be passed to the service via the Intent.
        //serviceIntent.putExtra(P2pService.SEED_KEY, 42L);
        context.startService(serviceIntent);
    }
}