package com.example.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

/**
 * {@link Activity} for allowing the user of the app to log in.
 * @see LoginFragment
 */
public class PayingActivity extends Activity{

    Button payBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_paying);

        payBtn = (Button) findViewById(R.id.payBtn);
        payBtn.setEnabled(false);

        // This is for rating driver
        final Dialog dialog = new Dialog(PayingActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("Rate our driver!");
        dialog.setContentView(R.layout.rate_driver_input_box);
        TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
        txtMessage.setText("Your opinion is important to us. Help us getting better!");
        txtMessage.setTextColor(Color.parseColor("#ff2222"));


        final RatingBar rStars = (RatingBar) dialog.findViewById(R.id.ratingBarDriver);
        LayerDrawable strs = (LayerDrawable) rStars.getProgressDrawable();
        strs.getDrawable(2).setColorFilter(Color.parseColor("#ffd700"), PorterDuff.Mode.SRC_ATOP);


        Button confirmRateBtn =(Button) dialog.findViewById(R.id.confirmRateBtn);
        confirmRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ConexionRest conn = new ConexionRest(null);
                    String tripId = PathInfo.getInstance().getTripId();
                    String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
                    float dRate = rStars.getRating();
                    Log.d("MainActivity", "URL to rate driver: " + tripUrl);
                    conn.generatePost("{ \"action\": \"rate\", \"rating\": " + dRate + " }", tripUrl, null);
                } catch (Exception e) {
                    Log.e("MainActivity", "Confirming driver error: ", e);
                }
                dialog.dismiss();
            }
        });
        dialog.show();

        // Till here
        // Down here begins payment

        final ImageView cardsLogoImageView = (ImageView) findViewById(R.id.cardsLogoImageView);
        final ImageView cashLogoImageView = (ImageView) findViewById(R.id.cashLogoImageView);

        cashLogoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardsLogoImageView.setAlpha((float) 1.0);
                cashLogoImageView.setAlpha((float) 0.9);
                try {
                    ConexionRest conn = new ConexionRest(null);
                    String tripId = PathInfo.getInstance().getTripId();
                    String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
                    Jsonator jnator = new Jsonator();
                    PaymethodInfo pmi = new PaymethodInfo("cash");
                    String toSendJson = jnator.writePaymentAction(pmi);
                    Log.d("PayingActivity", "URL to pay with cash: " + tripUrl);
                    conn.generatePost(toSendJson, tripUrl, null);

                    UserInfo.getInstance().setUserStatus(UserStatus.P_IDLE);
                    PathInfo.getInstance().selfDestruct();
                    UserInfo.getInstance().setOtherUser(null);
                    ActivityChanger.getInstance().gotoActivity(PayingActivity.this, MainActivity.class);
                    Toast.makeText(getApplicationContext(), "Payment received!", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Log.e("PayingActivity", "Paying error: ", e);
                }
            }
        });

        cardsLogoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layouMagic = (LinearLayout) findViewById(R.id.layoutMagic);
                layouMagic.setVisibility(View.VISIBLE);
                payBtn.setEnabled(true);
                cardsLogoImageView.setAlpha((float) 0.9);
                cashLogoImageView.setAlpha((float) 1.0);
            }
        });

        final EditText cardNumber = (EditText) findViewById(R.id.cardNumber);
        cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ImageView cardLogoImageView = (ImageView) findViewById(R.id.cardLogoImageView);
                String cardType = getCardType(cardNumber.getText().toString());
                if(cardType.equals("visa")) {
                    cardLogoImageView.setImageResource(R.drawable.icon_visa);
                    cardLogoImageView.setVisibility(View.VISIBLE);
                }
                else if(cardType.equals("mastercard")) {
                    cardLogoImageView.setImageResource(R.drawable.icon_mastercard);
                    cardLogoImageView.setVisibility(View.VISIBLE);
                }
                else {
                    cardLogoImageView.setVisibility(View.INVISIBLE);
                }
            }
        });





        payBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                try {
                    ConexionRest conn = new ConexionRest(null);
                    String tripId = PathInfo.getInstance().getTripId();
                    String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
                    Jsonator jnator = new Jsonator();
                    PaymethodInfo pmi = new PaymethodInfo("card");
                    pmi.cardNumber = cardNumber.getText().toString();
                    pmi.cardCcvv = ((EditText) findViewById(R.id.cardCCVV)).getText().toString();
                    pmi.expYear = ((EditText) findViewById(R.id.cardYear)).getText().toString();
                    pmi.expMonth = ((EditText) findViewById(R.id.cardMonth)).getText().toString();
                    pmi.cardType = getCardType(cardNumber.getText().toString());
                    String toSendJson = jnator.writePaymentAction(pmi);
                    Log.d("MainActivity", "URL to pay with cash: " + tripUrl);
                    conn.generatePost(toSendJson, tripUrl, null);

                    PathInfo.getInstance().selfDestruct();
                    UserInfo.getInstance().setOtherUser(null);
                    UserInfo.getInstance().setUserStatus(UserStatus.P_IDLE);
                    ActivityChanger.getInstance().gotoActivity(PayingActivity.this, MainActivity.class);
                    Toast.makeText(getApplicationContext(), "Payment received!", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Log.e("MainActivity", "Confirming driver error: ", e);
                }
            }
        });



    }


    private String getCardType(String cardNumber){
        if(cardNumber.length() < 1) return "no_idea";
        if(cardNumber.charAt(0) == '5')
            return "mastercard";
        if(cardNumber.charAt(0) == '4')
            return "visa";
        return "no_idea";
    }

// -------------------------------------------------------------------------------------------------
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class LoginFromFile implements RestUpdate {

        private final String mUserId;
        private final String mPassword;

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

                ActivityChanger.getInstance().gotoMenuScreen(PayingActivity.this);
            } else {
                Log.e("LoginActivity", "Critical: couldn't log in with saved auths!");
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
