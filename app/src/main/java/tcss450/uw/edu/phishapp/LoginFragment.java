package tcss450.uw.edu.phishapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.phishapp.model.Credentials;
import tcss450.uw.edu.phishapp.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_PASSWORD = "PASSWORD";
    private Credentials mCredentials;
    private String mFirebaseToken;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Constructor
    public LoginFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        Button b = v.findViewById(R.id.button_login);
        b.setOnClickListener(this);
        b = v.findViewById(R.id.button_register);
        b.setOnClickListener(this);
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //retrieve the stored credentials from SharedPrefs
        if (prefs.contains(getString(R.string.key_prefs_email)) &&
                prefs.contains(getString(R.string.key_prefs_password))) {
            final String email = prefs.getString(getString(R.string.key_prefs_email), "");
            final String password = prefs.getString(getString(R.string.key_prefs_password), "");
            //Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.editText_email);
            emailEdit.setText(email);
            EditText passwordEdit = getActivity().findViewById(R.id.editText_password);
            passwordEdit.setText(password);
            // If the credentials exist in the SharedPrefs, then automatically log in.
            //doLogin(email, password);
            getFirebaseToken(email, password);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // MEAT AND POTATOES.
    @Override
    public void onClick(View v) {
        EditText etEmail = getActivity().findViewById(R.id.editText_email);
        EditText etPassword = getActivity().findViewById(R.id.editText_password);
        //Credentials credentials = new Credentials.Builder(getS(etEmail), getS(etPassword)).build(); // doLogin() does this.
        int errorCode = 0;
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.button_login:
                    errorCode = validateLocally(etEmail, etPassword);
                    if (errorCode == 0) {
                        //mListener.onLoginFragmentInteraction(R.id.fragment_display, credentials); // this is done in handleLoginOnPost() below.
                        //executeAsyncTask(mCredentials); // doLogin() does this.
                        //doLogin(getS(etEmail), getS(etPassword));
                        getFirebaseToken(getS(etEmail), getS(etPassword));
                    }
                    break;
                case R.id.button_register:
                    mListener.onLoginFragmentInteraction(R.id.fragment_registration, null);
                    break;
                default:
                    Log.wtf("LoginFragment onClick()", "Didn't expect to see me...");
                    break;
            }
        } else {
            Log.wtf("LoginFragment onClick()", "mListener is null.");
        }
    }

    private void doLogin(final String email, final String password) {
        mCredentials = new Credentials.Builder(email, password).build();
        executeAsyncTask(mCredentials);
    }

    private void doLogin(final Credentials c) {
        Credentials credentials = new Credentials.Builder(c.getEmail(), c.getPassword())
                .addFirstName(c.getFirstName())
                .addLastName(c.getLastName())
                .addUsername(c.getUsername())
                .build();
        executeAsyncTask(credentials);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
            extends WaitFragment.OnFragmentInteractionListener {
        void onLoginFragmentInteraction(int fragmentId, Credentials credentials);
    }

    private Uri buildWebServiceUri() {
        return new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .appendPath(getString(R.string.ep_with_token))
                .build();
    }

    private void executeAsyncTask(final Credentials credentials) {
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons.
        Uri uri = buildWebServiceUri();
        JSONObject json = credentials.asJSONObject();
        mCredentials = credentials; // Does this imply if an exception happens during creation of the json object that this assignment won't occur?  But the exception is not handled anyway!
        try {
            json.put("token", mFirebaseToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::handleLoginOnPre)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     *
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleLoginOnPre() {
        //mListener.onWaitFragmentInteractionShow(); // Removed in Lab5.
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     *
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            Log.d("JSON result", result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                //Inform the Activity so it can do its thing.
                //Login was successful. Switch to the loadSuccessFragment.
                saveCredentials(mCredentials);
                mListener.onLoginFragmentInteraction(0, mCredentials); // 0 is the default case in the switch block.
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                ((TextView) getView().findViewById(R.id.editText_email)) // R.id.edit_login_email
                        .setError("Login Unsuccessful");
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.editText_email)) // R.id.edit_login_email
                    .setError("Login Unsuccessful");
        }
    }

    protected int validateLocally(final TextView vEmail, final TextView vPassword) {
        return validEmail(vEmail) + validPassword(vPassword) + validNames(vEmail, vPassword);
    }

    private int validEmail(final TextView view) {
        int minimum = getValue(R.string.number_email_minimum);
        int result = 0;
        String theEmail = getS(view);
        if (!theEmail.contains("@")
                || theEmail.length() < minimum) {
            view.setError("Invalid");
            result--;
        }
        return result;
    }

    private int validPassword(final TextView view) {
        int result = 0, minimum = getValue(R.string.number_password_minimum);
        final String s = getS(view);
        if (s.length() < minimum) {
            view.setError("Must be at least " + minimum + " chars");
            result--;
        }
        return result;
    }

    private int validNames(final TextView... view) {
        int result = 0;
        for (int i = 0; i < view.length; i++) {
            if (getS(view[i]).length() == 0) {
                result--;
                view[i].setError("Cannot be blank");
            }
        }
        return result;
    }

    private String getS(View view) {
        EditText et = (EditText) view;
        return et.getText().toString();
    }

    private int getValue(final int theStringID) {
        return Integer.parseInt(getString(theStringID));
    }

    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.key_prefs_email),
                credentials.getEmail()).apply();
        prefs.edit().putString(getString(R.string.key_prefs_password),
                credentials.getPassword()).apply();
    }

    private void getFirebaseToken(final String email, final String password) {
        mListener.onWaitFragmentInteractionShow();

        //add this app on this device to listen for the topic all
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        //the call to getInstanceId happens asynchronously. task is an onCompleteListener
        //similar to a promise in JS.
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM: ", "getInstanceId failed", task.getException());
                        mListener.onWaitFragmentInteractionHide();
                        return;
                    }
                    // Get new Instance ID token
                    mFirebaseToken = task.getResult().getToken();
                    Log.d("FCM: ", mFirebaseToken);
                    //the helper method that initiates login service
                    doLogin(email, password);
                });
        //no code here. wait for the Task to complete.
    }
}
