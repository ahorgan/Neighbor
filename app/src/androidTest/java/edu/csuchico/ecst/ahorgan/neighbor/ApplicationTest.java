package edu.csuchico.ecst.ahorgan.neighbor;

import android.app.Application;
import android.content.Intent;
import android.test.ApplicationTestCase;

import edu.csuchico.ecst.ahorgan.neighbor.Memeosphere.MemeosphereService;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        getContext().startService(new Intent(getContext(), MemeosphereService.class));
    }
}