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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login and sign up via userId/password.
 */
public class ManualSignInActivity extends Activity implements LoaderCallbacks<Cursor> {
    // Keep track of the login task to ensure we can cancel it if requested
    private UserLoginTask mAuthTask = null;
    // UI references (log in)
    private AutoCompleteTextView mUserIdView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    // (sign up)
    private Button signUpBtn;

    /**
     * Activity onCreate method.
     */
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

    /**
     * Initializes {@link android.app.LoaderManager}.
     */
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
     * Private method for going to MainActivity.
     */
    private void goMainScreen(){
        ActivityChanger.getInstance().gotoMenuScreen(this);
    }

    /**
     * Checks if the userId field of the logging in user is valid.
     * @param userId The user's userId
     */
    private boolean isUserIdValid(String userId) {
        UserInfoValidator uiv = new UserInfoValidator();
        return uiv.isUserIdValid(userId);
    }

    /**
     * Checks if the password field of the logging in user is valid.
     * @param password The user's password
     */
    private boolean isPasswordValid(String password) {
        UserInfoValidator uiv = new UserInfoValidator();
        return uiv.isPasswordValid(password);
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
    public class UserLoginTask implements RestUpdate {

        private final String mUserId;
        private final String mPassword;

        UserLoginTask(String userId, String password) {
            mUserId = userId;
            mPassword = password;
        }

        /**
         * Checks if the userIdSignIn:password pair has been already registered.
         * @param servResponse The app-server's response to the login request
         */
        private Boolean userIsRegistered(String servResponse) {
            Jsonator jnator = new Jsonator();
            return jnator.userLoggedInIsOk(servResponse);
        }

        /**
         * Checks if the user has successfully logged in, and initializes
         * {@link UserInfo} with their data.
         * @param servResponse The app-server's response to the login request
         */
        protected Boolean checkUserLog(String servResponse) {
            if (userIsRegistered(servResponse)) {
                Jsonator jnator = new Jsonator();
                jnator.readUserLoggedInInfo(servResponse);
                UserInfo ui = UserInfo.getInstance();

                ui.initializeUserInfo(ui.getUserId(), ui.getEmail(), ui.getFirstName(),
                        ui.getLastName(), ui.getCountry(), ui.getBirthdate(), mPassword, "", ui.getAppServerToken());
                return true;
            } else
                return false;
        }

        /**
         * Transition to {@link MainActivity}.
         * @param success True if login was successful, false otherwise
         * */
        protected void enterApplication(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                // Try to create a firebase account
                UserInfo ui = UserInfo.getInstance();
                try {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(ui.getEmail(), ui.getPassword());
                }
                catch (Exception e){
                    Log.e("ManualSignInActivity", "Error creating Firebase account: ", e);
                }
                goMainScreen();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        /**
         * Aborts if user cancels login.
         * */
        protected void onCancelled() {
            mAuthTask = null;
           // showProgress(false);
        }

        /**
         * Attempts to perform a log in with the userId and password
         * stored on this class. If the login request was successful,
         * the executeUpdate method will initialize {@link UserInfo}
         * and go to {@link MainActivity}.
         */
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

        /**
         * Checks if the user was successfully logged in, and enters
         * the application in that case. This function initializes
         * {@link UserInfo} with the app-server received data.
         * @param servResponse The app-server's response to the login request
         */
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
        private TextView fNameSignUp, lNameSignUp, emailSignUp, birthdateSignUp;
        private TextView countrySignUp, idSignUp, passwordSignUp;

        /**
         * Class constructor.
         * */
        UserSignupTask() {
            fNameSignUp = (TextView) findViewById(R.id.fNameSignUp);
            lNameSignUp = (TextView) findViewById(R.id.lNameSignUp);
            emailSignUp = (TextView) findViewById(R.id.emailSignUp);
            birthdateSignUp = (TextView) findViewById(R.id.birthdateSignUp);
            countrySignUp = (TextView) findViewById(R.id.countrySignUp);
            idSignUp = (TextView) findViewById(R.id.idSignUp);
            passwordSignUp = (TextView) findViewById(R.id.passwordSignUp);
        }

        /**
         * Checks if the userIdSignIn:password pair was registered correctly
         * @param servResponse The app-server's response to the sign up request
         */
        private Boolean userWasRegistered(String servResponse){
            Jsonator jnator = new Jsonator();
            return jnator.userSignedUpIsOk(servResponse);
        }

        /**
         * Gets sign up data of user and use it to initialize
         * {@link UserInfo}. This function runs a validation on
         * all fields using {@link UserInfoValidator}. Returns
         * false if this validation failed, or true otherwise.
         */
        boolean getSignUpFields(){
            // Store values at the time of the login attempt.
            String fName = fNameSignUp.getText().toString();
            String lName = lNameSignUp.getText().toString();
            String mail = emailSignUp.getText().toString();
            String bth = birthdateSignUp.getText().toString();
            String country = countrySignUp.getText().toString();
            String userId = idSignUp.getText().toString();
            String password = passwordSignUp.getText().toString();

            // Check all fields are ok
            UserInfoValidator uiv = new UserInfoValidator();
            boolean fieldsOk = true;
            fieldsOk &= uiv.checkFieldShowError(fNameSignUp, "name", "This user name is invalid");
            fieldsOk &= uiv.checkFieldShowError(lNameSignUp, "name", "This user surname is invalid");
            fieldsOk &= uiv.checkFieldShowError(emailSignUp, "email", "This user email is invalid");
            fieldsOk &= uiv.checkFieldShowError(countrySignUp, "name", "This country is invalid");
            fieldsOk &= uiv.checkFieldShowError(birthdateSignUp, "birthdate", "This date is invalid");
            fieldsOk &= uiv.checkFieldShowError(passwordSignUp, "password", getString(R.string.error_invalid_password));
            fieldsOk &= uiv.checkFieldShowError(idSignUp, "userid", getString(R.string.error_invalid_email));

            if(!fieldsOk) return false;

            // Initialize user info
            UserInfo ui = UserInfo.getInstance();
            ui.initializeUserInfo(userId, mail, fName, lName, country, bth, password, "", "");

            CheckBox driverChckBox = (CheckBox) findViewById(R.id.driverChckBox);
            if(driverChckBox.isChecked())
                ui.setAsDriver();
            return true;
        }

        /**
         * Transition to {@link MainActivity}.
         * @param success True if login was successful, false otherwise
         * */
        protected void enterApplication(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                goMainScreen();
            } else {
                passwordSignUp.setError(getString(R.string.error_incorrect_password));
                passwordSignUp.requestFocus();
            }
        }

        /**
         * Aborts if user cancels login.
         * */
        protected void onCancelled() {
            mAuthTask = null;
            // showProgress(false);
        }

        /**
         * Attempts a new user sign up, previously reading and
         * validating user data via {@link UserInfoValidator}.
         * This function initializes {@link UserInfo} if everything
         * was OK and sends the request to app-server. Transition
         * to {@link MainActivity} is made on executeUpdate method.
         * */
        public void signUser() {
            if(!getSignUpFields())
                return;
            try {
                UserInfo ui = UserInfo.getInstance();
                Jsonator jnator = new Jsonator();
                String toSendJson = jnator.writeUserSignUpInfo(false);
                Log.d("ManualSignInActivity", "JSON to send: "+toSendJson);
                ConexionRest conn = new ConexionRest(this);
                String signUrl = conn.getBaseUrl() + "/users";
                conn.generatePost(toSendJson, signUrl, null);
            }
            catch(Exception e){
                Log.e("ManualSignInActivity", "Manual log in error: ", e);
            }
        }

        /**
         * Checks if the user has successfully signed up, and log
         * them in via {@link UserLoginTask}.
         * @param servResponse The app-server's response to the sign up request
         * */
        @Override
        public void executeUpdate(String servResponse) {
            if(userWasRegistered(servResponse)) {
                UserInfo ui = UserInfo.getInstance();
                mAuthTask = new UserLoginTask(ui.getUserId(), ui.getPassword());
                mAuthTask.logUser();
            }
            else{
                idSignUp.setError("User ID already taken");
                idSignUp.requestFocus();
            }
        }
    }
}

