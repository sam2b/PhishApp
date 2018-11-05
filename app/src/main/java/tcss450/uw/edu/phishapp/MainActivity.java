package tcss450.uw.edu.phishapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.Serializable;

import tcss450.uw.edu.phishapp.model.Credentials;


public class MainActivity extends AppCompatActivity implements
        LoginFragment.OnFragmentInteractionListener
        , RegisterFragment.OnFragmentInteractionListener
        , WaitFragment.OnFragmentInteractionListener {

    private boolean mLoadFromChatNotification = false;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("type")) {
                Log.d(TAG, "type of message: " + getIntent().getExtras().getString("type"));
                mLoadFromChatNotification = getIntent().getExtras().getString("type").equals("msg");
            } else {
                Log.d(TAG, "NO MESSAGE");
            }
        }

        if(savedInstanceState == null) {
            if(findViewById(R.id.frame_main_container) != null) {
                getSupportFragmentManager().beginTransaction().add(R.id.frame_main_container
                        , new LoginFragment()).commit();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onLoginFragmentInteraction(int fragmentId, Credentials credentials) {
        android.support.v4.app.Fragment fragment;

        switch (fragmentId) {
            case R.id.fragment_login:
                fragment = new LoginFragment();
                launchFragment(fragment);
                break;
            case R.id.fragment_registration:
                fragment = new RegisterFragment();
                launchFragment(fragment);
                break;
            default: // SUCCESSFUL LOGIN.
                login(credentials);
                break;
        }
    }

    @Override
    public void onRegisterFragmentInteraction(int fragmentId, Credentials credentials) {
        //android.support.v4.app.Fragment fragment;
        if (fragmentId == 0) {
            // SUCCESSFUL REGISTER.
            this.onLoginFragmentInteraction(0, credentials); // 0 is the default case in the switch block.
        }
        // else stay on the same fragment.
    }

    private void login(final Credentials credentials) {
        // The HomeActivity Drawer.
        clearBackStack(getSupportFragmentManager()); //TODO is this necessary?
        Intent intent = new Intent(this, HomeActivity.class);
        //intent.putExtra(getString(R.string.key_Username, credentials.getUsername()); // Before Lab5.
        intent.putExtra(getString(R.string.key_Email), (Serializable) credentials);    // Added in Lab5. Might make a bug.
        intent.putExtra(getString(R.string.key_intent_notifification_msg), mLoadFromChatNotification);
        startActivity(intent);
        //End this Activity and remove it from the Activity back stack.
        finish();
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
                .replace(R.id.frame_main_container, fragment);
        transaction.commit();
    }

    @Override
    public void onWaitFragmentInteractionShow() {

    }

    @Override
    public void onWaitFragmentInteractionHide() {

    }
}

