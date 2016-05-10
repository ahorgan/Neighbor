package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static String TAG = "MainActivity";
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private CreateProfileOnClick createProfileOnClick = new CreateProfileOnClick();
    private CreateEventOnClick createEventOnClick = new CreateEventOnClick();
    private ProfilesOnClick profilesOnClick = new ProfilesOnClick();
    private ViewProfileOnClick viewProfileOnClick = new ViewProfileOnClick();
    private EventsOnClick eventsOnClick = new EventsOnClick();
    private ConnectOnClick connectOnClick = new ConnectOnClick();
    private DisconnectOnClick disconnectOnClick = new DisconnectOnClick();
    private CreateProfileFragment createProfileFragment = new CreateProfileFragment();
    private ProfileFragment viewProfilesFragment = new ProfileFragment();
    private ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (findViewById(R.id.fragment_container) != null) {
            Log.d(TAG, "Create createprofilefragment");
            if(savedInstanceState != null)
                return;
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            createProfileFragment.setArguments(getIntent().getExtras());
            viewProfilesFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, createProfileFragment)
                    .commit();

            fab.setOnClickListener(createProfileOnClick);
        }
        else {
            Log.d(TAG, "Cannot find R.id.fragment_create_profile");
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onProfileSelected(Map<String, Object> item) {
        if(fab == null)
            fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(viewProfileOnClick);
        Log.d(TAG, "Profile Selected:");
        HashMap hashItem = new HashMap();
        hashItem.putAll(item);
        for(Object entry : hashItem.entrySet()) {
            Log.d(TAG, ((HashMap.Entry)entry).getKey() + " : " + ((HashMap.Entry)entry).getValue());
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("ITEM_MAP", hashItem);
        ViewProfileFragment newView = new ViewProfileFragment();
        newView.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, newView)
                .commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        // set new on click listener depending on which item clicked
        if (id == R.id.edit_profile) {
            Log.d(TAG, "edit profile selected");
            fab.setOnClickListener(createProfileOnClick);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, createProfileFragment)
                    .commit();
        }
        else if(id == R.id.view_profiles) {
            Log.d(TAG, "view profiles selected");
            fab.setOnClickListener(profilesOnClick);
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    viewProfilesFragment).addToBackStack(null).commit();
        }
        else if (id == R.id.create_event) {
            Log.d(TAG, "create event selected");
            fab.setOnClickListener(createEventOnClick);
        } else if (id == R.id.neighbors) {
            Log.d(TAG, "neighbors selected");
            fab.setOnClickListener(profilesOnClick);
        } else if (id == R.id.events) {
            Log.d(TAG, "events selected");
            fab.setOnClickListener(eventsOnClick);
        } else if (id == R.id.connect) {
            Log.d(TAG, "connect selected");
            fab.setOnClickListener(connectOnClick);
        } else if (id == R.id.disconnect) {
            Log.d(TAG, "disconnect selected");
            fab.setOnClickListener(disconnectOnClick);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class CreateProfileOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(fab == null)
                fab = (FloatingActionButton)findViewById(R.id.fab);
            Log.d(TAG, "create profile button clicked");
            //int id = fragmentManager.getBackStackEntryAt(current_fragment_id).getId();
            createProfileFragment.updateProfile();
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    viewProfilesFragment).commit();
            fab.setOnClickListener(profilesOnClick);
        }
    }

    class CreateEventOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "create event button clicked");
        }
    }

    class ProfilesOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "profiles button clicked");
            if(fab == null)
                fab = (FloatingActionButton) findViewById(R.id.fab);
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    createProfileFragment).commit();
            fab.setOnClickListener(createProfileOnClick);
        }
    }

    class ViewProfileOnClick implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(fab == null)
                fab = (FloatingActionButton) findViewById(R.id.fab);
            Bundle bundle = new Bundle();
            bundle.putSerializable("ITEM_MAP", (HashMap)viewProfileFragment.getItem());
            createProfileFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    createProfileFragment).commit();
            fab.setOnClickListener(createProfileOnClick);
        }
    }

    class EventsOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "events button clicked");
        }
    }

    class ConnectOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "connect button clicked");
        }
    }

    class DisconnectOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "disconnect button clicked");
        }
    }
}
