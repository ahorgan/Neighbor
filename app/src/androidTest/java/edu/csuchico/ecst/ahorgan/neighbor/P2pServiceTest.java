package edu.csuchico.ecst.ahorgan.neighbor;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import edu.csuchico.ecst.ahorgan.neighbor.world.DiscoverService;
import edu.csuchico.ecst.ahorgan.neighbor.new_world.NewWorldService;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class P2pServiceTest {
    private static final String TAG = "P2pServiceTest";
    public static final String TEST_STRING = "This is a string";
    public static final long TEST_LONG = 12345678L;
    private NewWorldService mService;

    @Test
    public void testWithStartedService() throws TimeoutException {
        Log.d(TAG, "testWithBoundService called");
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(InstrumentationRegistry.getTargetContext(),
                        NewWorldService.class);
        Context context = InstrumentationRegistry.getContext();

        // Data can be passed to the service via the Intent.
        //serviceIntent.putExtra(P2pService.SEED_KEY, 42L);
        context.startService(serviceIntent);
    }
}