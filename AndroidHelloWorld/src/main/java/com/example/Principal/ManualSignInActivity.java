package com.example.Principal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via userIdSignIn/password.
 */
public class ManualSignInActivity extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references (log in)
    private AutoCompleteTextView mUserIdView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    // (sign up)
    private Button signUpBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_sign_in);
        // Set up the login form.
        mUserIdView = (AutoCompleteTextView) findViewById(R.id.userIdSignIn);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.passwordSignIn);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.signInBtn);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);

        signUpBtn = (Button) findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UserSignupTask ust = new UserSignupTask();
                ust.signUser();
            }
        });
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid userIdSignIn, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserIdView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userId = mUserIdView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid userIdSignIn address.
        if (TextUtils.isEmpty(userId)) {
            mUserIdView.setError(getString(R.string.error_field_required));
            focusView = mUserIdView;
            cancel = true;
        } else if (!isUserIdValid(userId)) {
            mUserIdView.setError(getString(R.string.error_invalid_email));
            focusView = mUserIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
           // showProgress(true);
            mAuthTask = new UserLoginTask(userId, password);
            mAuthTask.logUser();
        }
    }

    /**
     * Private method for goint to MainActivity.
     */
    private void goMainScreen(){
        ActivityChanger.getInstance().gotoMenuScreen(this);
    }

    private boolean isUserIdValid(String userId) {
        boolean idOk = userId.length() > 1;
        // Next line checks that userId has at least one letter
        idOk &= (userId.matches(".*[a-z].*") || userId.matches(".*[A-Z].*"));
        return idOk;
    /*    boolean emailOk = email.contains("@");
        emailOk &= email.contains(".");

        int aPosition = email.indexOf("@");
        emailOk &= (aPosition >= 2);

        int dotPosition = email.indexOf(".");
        emailOk &= (dotPosition >= 3);

        emailOk &= ((dotPosition - aPosition)  >= 2);

        return emailOk;*/
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only userIdSignIn addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary userIdSignIn addresses first. Note that there won't be
                // a primary userIdSignIn address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(ManualSignInActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserIdView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

// --------------------------------------------------------------------------------------------------------

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask implements RestUpdate{

        private final String mUserId;
        private final String mPassword;

        UserLoginTask(String userId, String password) {
            mUserId = userId;
            mPassword = password;
        }

        /* Checks if the userIdSignIn:password pair has been already registered.*/
        private Boolean userIsRegistered(String servResponse){
            Jsonator jnator = new Jsonator();
            return jnator.userLoggedInIsOk(servResponse);
        }

        protected Boolean checkUserLog(String servResponse) {
            if(userIsRegistered(servResponse)){
                Jsonator jnator = new Jsonator();
                jnator.readUserLoggedInInfo(servResponse);
                UserInfo ui = UserInfo.getInstance();

                ui.initializeUserInfo(ui.getUserId(), ui.getEmail(), ui.getFirstName(),
                        ui.getLastName(), ui.getCountry(), ui.getBirthdate(), mPassword, "", ui.getAppServerToken());
                return true;
            }
            else
                return false;
        }

        protected void enterApplication(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                goMainScreen();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        protected void onCancelled() {
            mAuthTask = null;
           // showProgress(false);
        }

        public void logUser() {
            try {
                Jsonator jnator = new Jsonator();
                String toSendJson = jnator.writeUserLoginCredentials(mUserId, mPassword, " ");
                ConexionRest conn = new ConexionRest(this);
                String urlReq = conn.getBaseUrl() + "/users/login";
                Log.d("ManualSignInActivity", "JSON to send: "+ toSendJson);
                conn.generatePost(toSendJson, urlReq, null);
            }
            catch(Exception e){
                Log.e("ManualSignInActivity", "Manual log in error: ", e);
            }
        }

        @Override
        public void executeUpdate(String servResponse) {
            Log.d("ManualSignInActivity", "Response from server: "+ servResponse);
            Boolean resultado = this.checkUserLog(servResponse);
            enterApplication(resultado);
        }
    }

// --------------------------------------------------------------------------------------------------------

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserSignupTask implements RestUpdate{
        // TODO: Handle registration against app server
        UserSignupTask() {

        }

        /* Checks if the userIdSignIn:password pair was registered correctly*/
        private Boolean userIsRegistered(String servResponse){
            Jsonator jnator = new Jsonator();
            return jnator.userSignedUpIsOk(servResponse);
        }

        void getSignUpFields(){
            // TODO: Fill UserInfo with sign up info
        }

        protected void enterApplication(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                goMainScreen();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        protected void onCancelled() {
            mAuthTask = null;
            // showProgress(false);
        }

        public void signUser() {
            getSignUpFields();
            try {
                UserInfo ui = UserInfo.getInstance();
                ui.initializeUserInfo("cristian123", "calonicoo@gmail.com", "Cristian", "Calonico",
                        "Argentina", "06/06/1990", "docker1234", "", "");
                Jsonator jnator = new Jsonator();
                String toSendJson = jnator.writeUserSignUpInfo();
                Log.d("ManualSignInActivity", "JSON to send: "+toSendJson);
                ConexionRest conn = new ConexionRest(this);
                String signUrl = conn.getBaseUrl() + "/users";
                conn.generatePost(toSendJson, signUrl, null);
            }
            catch(Exception e){
                Log.e("ManualSignInActivity", "Manual log in error: ", e);
            }
        }

        @Override
        public void executeUpdate(String servResponse) {
            Log.d("ConexionRest", "Answer from server: " + servResponse);
        }
    }
}

