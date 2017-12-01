package com.example.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import static com.example.android.ManualSignInActivity.*;

/**
 * {@link Activity} for allowing the user of the app to log in.
 * @see LoginFragment
 */
public class LoginActivity extends Activity{

    Button signBtn;

    /**
     * This {@link Activity} overrided onCreate method.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String PREFS_FILE = "AuthFile";
        SharedPreferences pref = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        String userId = pref.getString("UserId", null);
        String pass = pref.getString("Password", null);

        if((userId == null) || (pass == null)) {
            Log.i("LoginActivity", "No saved account has been detected");
            startUI();
            }
        else {
            // Log in user via read info
            LoginFromFile lff = new LoginFromFile(userId, pass);
            lff.logUser();
        }



    }

    /**
     * Starts the UI of this {@link Activity}. Should only be called if no
     * user has previously logged in.
     */
    public void startUI(){
        setContentView(R.layout.activity_login);

        signBtn = (Button) findViewById(R.id.signBtn);
        signBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ActivityChanger.getInstance().gotoActivity(LoginActivity.this, ManualSignInActivity.class);
            }
        });
    }


// -------------------------------------------------------------------------------------------------
    /**
     * Represents the user login if that user has previously logged in on
     * this device.
     */
    public class LoginFromFile implements RestUpdate {

        private final String mUserId;
        private final String mPassword;

        /**
         * Constructor for this class, considering user auths.
         * @param userId The user id of the logging in user.
         * @param password The password of that user
         */
        LoginFromFile(String userId, String password) {
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
            if (success) {
                // Try to create a firebase account
                UserInfo ui = UserInfo.getInstance();
                try {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(ui.getEmail(), ui.getPassword());
                }
                catch (Exception e){
                    Log.e("LoginActivity", "Error creating Firebase account: ", e);
                }

                ActivityChanger.getInstance().gotoMenuScreen(LoginActivity.this);
            } else {
                Log.e("LoginActivity", "Critical: couldn't log in with saved auths!");
                startUI();
            }
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
                Log.d("LoginActivity", "JSON to send: "+ toSendJson);
                conn.generatePost(toSendJson, urlReq, null);
            }
            catch(Exception e){
                Log.e("LoginActivity", "Manual log in error: ", e);
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
            Log.d("LoginActivity", "Response from server: "+ servResponse);
            Boolean resultado = this.checkUserLog(servResponse);
            enterApplication(resultado);
        }
    }
}
