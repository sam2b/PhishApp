package tcss450.uw.edu.phishapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tcss450.uw.edu.phishapp.blog.BlogPost;
import tcss450.uw.edu.phishapp.model.Credentials;
import tcss450.uw.edu.phishapp.setlists.Setlist;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
                   , BlogFragment.OnListFragmentInteractionListener
                   , SetlistFragment.OnListFragmentInteractionListener
                   , WaitFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
    private String mEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // ** BELOW IS A LARGE REFACTOR FOR LAB5. ** //////////////////////////////////////////////////

        // When HomeActivity loads, have it display the third fragment from Lab 1 with the email address.
//        // Commented out for LAB5 refactor below.
//        SuccessFragment successFragment = new SuccessFragment();
//        Bundle args = getIntent().getExtras();
//        successFragment.setArguments(args);

        if(savedInstanceState == null) {
            if (findViewById(R.id.frame_home_container) != null) {
                Credentials credentials = (Credentials) getIntent()
                        .getSerializableExtra(getString(R.string.key_Email));
                String emailAddress = mEmail = credentials.getEmail();
                final Bundle args = new Bundle();
                args.putString(getString(R.string.key_Email), emailAddress);
                Fragment fragment;
                if (getIntent().getBooleanExtra(getString(R.string.key_intent_notifification_msg)
                        ,false)) {
                    fragment = new ChatFragment();
                } else {
                    fragment = new SuccessFragment();
                    fragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction().add(R.id.frame_home_container
                        , fragment).commit();
            }
        }
        // ELSE what if savedInstanceState is NOT null??  See former code on line 76.
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // MEAT AND POTATOES.
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                Bundle args = new Bundle();
                String email = getIntent().getStringExtra(getString(R.string.key_Email));
                args.putSerializable(getString(R.string.key_Email), email);
                SuccessFragment SuccessFragment = new SuccessFragment();
                SuccessFragment.setArguments(args);
                launchFragment(SuccessFragment);
                break;
            case R.id.nav_blogPosts:
                //launchFragment(new BlogFragment()); // old, Lab2.
                executeAsyncTaskBlog();
                break;
            case R.id.nav_setlists:
                executeAsyncTaskSetlist();
                break;
            case R.id.nav_globalchat:
                launchFragment(new ChatFragment());
                break;
            default:
                Log.wtf("HomeActivity onNavigationItemSelected()", "invalid id.");
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Wipe out the backstack.
    private void clearBackStack(final FragmentManager fm) {
        int quantity = fm.getBackStackEntryCount();
        for (int i = 0; i < quantity; i++) {
            fm.popBackStack();
        }
    }

    private void launchFragment(final android.support.v4.app.Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_home_container, fragment);
        transaction.commit();
    }

    // BLOGS
    private void executeAsyncTaskBlog() {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_phish))
                .appendPath(getString(R.string.ep_blog))
                .appendPath(getString(R.string.ep_get))
                .build();
        new GetAsyncTask.Builder(uri.toString())
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleBlogGetOnPostExecute)
                .build().execute();
    }

    // BLOGS
    private void handleBlogGetOnPostExecute(final String result) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("response")) {
                JSONObject response = root.getJSONObject("response");
                if (response.has("data")) {
                    JSONArray data = response.getJSONArray("data");
                    List<BlogPost> blogs = new ArrayList<>();
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonBlog = data.getJSONObject(i);
                        blogs.add(new BlogPost.Builder(jsonBlog.getString("pubdate"),
                                jsonBlog.getString("title"))
                                .addTeaser(jsonBlog.getString("teaser"))
                                .addUrl(jsonBlog.getString("url"))
                                .build());
                    }
                    BlogPost[] blogsAsArray = new BlogPost[blogs.size()];
                    blogsAsArray = blogs.toArray(blogsAsArray);
                    Bundle args = new Bundle();
                    args.putSerializable(BlogFragment.ARG_BLOG_LIST, blogsAsArray);
                    Fragment frag = new BlogFragment();
                    frag.setArguments(args);
                    onWaitFragmentInteractionHide();
                    loadFragment(frag);
                } else {

                    Log.e("ERROR!", "No data array");
                    //notify user
                    onWaitFragmentInteractionHide();
                }
            } else {
                Log.e("ERROR!", "No response");
                //notify user
                onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
        }
    }

    // SETLISTS
    private void executeAsyncTaskSetlist() {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_phish))
                .appendPath(getString(R.string.ep_setlists))
                .appendPath(getString(R.string.ep_recent))
                .build();
        new GetAsyncTask.Builder(uri.toString())
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleSetlistsGetOnPostExecute)
                .build().execute();
    }

    // SETLISTS
    private void handleSetlistsGetOnPostExecute(final String result) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("response")) {
                JSONObject response = root.getJSONObject("response");
                if (response.has("data")) {
                    JSONArray data = response.getJSONArray("data");
                    List<Setlist> setlists = new ArrayList<>();
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonSetlist = data.getJSONObject(i);
                        setlists.add(new Setlist.Builder(jsonSetlist.getString("long_date"),
                                jsonSetlist.getString("location"))
                                .addVenue(jsonSetlist.getString("venue"))
                                .addData(jsonSetlist.getString("setlistdata"))
                                .addNotes(jsonSetlist.getString("setlistnotes"))
                                .addUrl(jsonSetlist.getString("url"))
                                .build());
                    }
                    Setlist[] setlistsAsArray = new Setlist[setlists.size()];
                    setlistsAsArray = setlists.toArray(setlistsAsArray);
                    Bundle args = new Bundle();
                    args.putSerializable(SetlistFragment.ARG_SETLIST_LIST, setlistsAsArray);
                    Fragment frag = new SetlistFragment();
                    frag.setArguments(args);
                    onWaitFragmentInteractionHide();
                    loadFragment(frag);
                } else {
                    Log.e("ERROR!", "No data array");
                    //notify user
                    onWaitFragmentInteractionHide();
                }
            } else {
                Log.e("ERROR!", "No response");
                //notify user
                onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
        }
    }

    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_home_container, frag)
                .addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //remove the saved credentials from StoredPrefs
        prefs.edit().remove(getString(R.string.key_prefs_password)).apply();
        prefs.edit().remove(getString(R.string.key_prefs_email)).apply();
        //close the app
        finishAndRemoveTask();
        //or close this activity and bring back the Login
        //Intent i = new Intent(this, MainActivity.class);
        //startActivity(i);
        //End this Activity and remove it from the Activity back stack.
        //finish();
        new DeleteTokenAsyncTask().execute();
    }

    @Override
    public void onBlogListFragmentInteraction(final BlogPost blog) {
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.key_BlogPostTitle), blog.getTitle());
        args.putSerializable(getString(R.string.key_BlogPostDate), blog.getPubDate());
        args.putSerializable(getString(R.string.key_BlogPostContent), blog.getTeaser());
        args.putSerializable(getString(R.string.key_BlogPostUrl), blog.getUrl());
        BlogPostFragment blogPostFragment = new BlogPostFragment();
        blogPostFragment.setArguments(args);
        loadFragment(blogPostFragment);
    }

    @Override
    public void onSetlistListFragmentInteraction(Setlist setlist) {
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.key_SetlistLongDate), setlist.getLongDate());
        args.putSerializable(getString(R.string.key_SetlistLocation), setlist.getLocation());
        args.putSerializable(getString(R.string.key_SetlistVenue), setlist.getVenue());
        args.putSerializable(getString(R.string.key_SetlistData), setlist.getData());
        args.putSerializable(getString(R.string.key_SetlistNotes), setlist.getNotes());
        args.putSerializable(getString(R.string.key_SetlistUrl), setlist.getUrl());
        SetlistPostFragment setlistPostFragment = new SetlistPostFragment();
        setlistPostFragment.setArguments(args);
        loadFragment(setlistPostFragment);
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_home_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("WAIT")) // Hides only the instance that was shown by nonWaitFragmentInteractionShow above.
                .commit();
    }

    // Deleting the InstanceId (Firebase token) must be done asynchronously. Good thing
    // we have something that allows us to do that.
    class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onWaitFragmentInteractionShow();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //since we are already doing stuff in the background, go ahead
            //and remove the credentials from shared prefs here.
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            prefs.edit().remove(getString(R.string.key_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.key_prefs_email)).apply();
            try {
                //this call must be done asynchronously.
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                Log.e("FCM", "Delete error!");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //close the app
                        finishAndRemoveTask();
            //or close this activity and bring back the Login
            // Intent i = new Intent(this, MainActivity.class);
            // startActivity(i);
            // //Ends this Activity and removes it from the Activity back stack.
            // finish();
        }
    }

}
