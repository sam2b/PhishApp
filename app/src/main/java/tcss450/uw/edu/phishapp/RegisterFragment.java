package tcss450.uw.edu.phishapp;

import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.phishapp.model.Credentials;
import tcss450.uw.edu.phishapp.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_PASSWORD = "PASSWORD";
    private Credentials mCredentials;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Constructor
    public RegisterFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        Button b = v.findViewById(R.id.button_register);
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
        if (mListener != null) {
            EditText etFirstName = getActivity().findViewById(R.id.editText_name_first);
            EditText etLastName = getActivity().findViewById(R.id.editText_name_last);
            EditText etUserName = getActivity().findViewById(R.id.editText_username);
            EditText etEmail = getActivity().findViewById(R.id.editText_email);
            EditText etPassword = getActivity().findViewById(R.id.editText_password);
            EditText etPassword2 = getActivity().findViewById(R.id.editText_password2);
            Credentials credentials =
                    new Credentials.Builder(getS(etEmail), getS(etPassword))
                            .addFirstName(getS(etFirstName))
                            .addLastName(getS(etLastName))
                            .addUsername(getS(etUserName))
                            .build();
            int errorCode = validateLocally(etFirstName, etLastName, etUserName, etEmail, etPassword, etPassword2);
            if (errorCode == 0) {
                //mListener.onRegisterFragmentInteraction(R.id.fragment_display, mCredentials); //etEmail.getText().toString() // this is done in handleRegisterOnPost() below.
                executeAsyncTask(credentials);
            }

        } else {
            Log.wtf("RegisterFragment onClick()", "mListener is null.");
        }
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
        void onRegisterFragmentInteraction(int fragmentId, Credentials credentials);
        void onLoginFragmentInteraction(int fragmentId, Credentials credentials);
    }

    private Uri buildWebServiceUrl() {
        return new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .build();
    }

    private void executeAsyncTask(final Credentials credentials) {
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons.
        Uri uri = buildWebServiceUrl();
        JSONObject json = credentials.asJSONObject();
        mCredentials = credentials; // Does this imply if an exception happens during creation of the json object that this assignment won't occur?  But the exception is not handled anyway!
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::handleRegisterOnPre)
                .onPostExecute(this::handleRegisterOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleRegisterOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleRegisterOnPost(String result) {
        try {
            Log.d("JSON result",result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                //Login was successful. Inform the Activity so it can do its thing.
                mListener.onRegisterFragmentInteraction(0, mCredentials); // 0 is the default case in the switch block.
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

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            Log.d("JSON result",result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                //Login was successful. Inform the Activity so it can do its thing.
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

    protected int validateLocally(final TextView vFirstName, final TextView vLastName
                                 ,final TextView vUserName, final TextView vEmail
                                 ,final TextView vPassword, final TextView vPassword2) {
        return validEmail(vEmail) + validPasswordBoth(vPassword,vPassword2)
                + validNames(vFirstName, vLastName, vUserName, vEmail, vPassword, vPassword2);

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

    private int validPasswordBoth(final TextView view1, final TextView view2) {
        int result = 0, minimum = getValue(R.string.number_password_minimum);
        final String s1 = getS(view1);
        final String s2 = getS(view2);
        if (s1.length() < minimum) {
            view1.setError("Must be at least " + minimum + " chars");
            view2.setError("Must be at least " + minimum + " chars");
            result--;
        } else if (s1.compareTo(s2) != 0) {
            view1.setError("Passwords do not match");
            view2.setError("Passwords do not match");
            result--;
        }
        return result;
    }

    private int validNames(final TextView... view) {
        int result = 0;
        for(int i=0; i<view.length; i++) {
            if (getS(view[i]).length() == 0) {
                result --;
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

}
