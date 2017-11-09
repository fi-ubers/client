package com.example.Principal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;


/**
 * An Activity that allows a user to watch and edit their profile
 * (i.e. their data).
 */
public class ProfileActivity extends Activity {
    EditText  userName, userSurname, userMail, userBirthdate, userCountry;
    TextView userUsID;
	Button submitBtn, cancelBtn, carsBtn;

	/**
	 * Activity onCreate method.
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		userName = (EditText) findViewById(R.id.profileName);
		userSurname = (EditText) findViewById(R.id.profileSurname);
		userMail = (EditText) findViewById(R.id.profileMail);
		userCountry = (EditText) findViewById(R.id.profileCountry);
		userUsID = (TextView) findViewById(R.id.userUsID);
		userBirthdate = (EditText) findViewById(R.id.profileBirthdate);

		UserInfo ui = UserInfo.getInstance();

		// Sanity check
		if(!ui.wasInitialized())
			Log.w("ProfileActivity", "UserInfo not initialized!");

		userName.setText(ui.getFirstName());
		userSurname.setText(ui.getLastName());
		userMail.setText(ui.getEmail());
		userBirthdate.setText(ui.getBirthdate());
		userCountry.setText(ui.getCountry());
		userUsID.setText("  " + ui.getUserId());

		carsBtn = (Button) findViewById(R.id.profileCarsBtn);
		if(ui.isDriver()){
			Log.d("ProfileActivity", "User is a driver!");
			carsBtn.setClickable(true);
			carsBtn.setVisibility(View.VISIBLE);
			// TODO: Link carsBtn with a CarsActivity or something like that
		}

		if(AccessToken.getCurrentAccessToken() == null) {
			// IF USER HAS NO FB ACCOUNT, USE DEFAULT!!
			ProfilePictureView profilePictureView;
			profilePictureView = (ProfilePictureView) findViewById(R.id.usProfilePicture);
			profilePictureView.setProfileId("107457569994960");
		}
		else{
			// If here, user logged in with FB
			Profile curProfile = Profile.getCurrentProfile();
			ProfilePictureView profilePictureView;
			profilePictureView = (ProfilePictureView) findViewById(R.id.usProfilePicture);
			profilePictureView.setProfileId(curProfile.getId());
		}


		submitBtn = (Button) findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				String newName = userName.getText().toString();
				String newSurname = userSurname.getText().toString();
				String newMail = userMail.getText().toString();
				String newBth = userBirthdate.getText().toString();
				String newCountry = userCountry.getText().toString();
				String newId = userUsID.getText().toString().replaceAll(" ","");;
				UserInfo ui = UserInfo.getInstance();
				if(ui.infoWillChange(newMail, newName, newSurname, newId, newBth, newCountry)){
					// Check if all inserted fields are valid
					UserInfoValidator uiv = new UserInfoValidator();
					boolean fieldsOk = true;
					fieldsOk &= uiv.checkFieldShowError(userName, "name", "This user name is invalid");
					fieldsOk &= uiv.checkFieldShowError(userSurname, "name", "This user surname is invalid");
					fieldsOk &= uiv.checkFieldShowError(userMail, "email", "This user email is invalid");
					fieldsOk &= uiv.checkFieldShowError(userCountry, "name", "This country is invalid");
					fieldsOk &= uiv.checkFieldShowError(userBirthdate, "birthdate", "This date is invalid");

					if(!fieldsOk) return;
					// Fields are ok, so change info
					String prevFbTkn = ui.getFbToken();
					String prevAppSTkn = ui.getAppServerToken();
					String prevPssw = ui.getPassword();
					int prevIntId = ui.getIntegerId();
					ui.seppuku();
					ui.initializeUserInfo(newId, newMail, newName, newSurname,
							newCountry, newBth, prevPssw, prevFbTkn, prevAppSTkn);
					ui.setIntegerId(prevIntId);
					// PUT changes to app server
					try {
						Jsonator jnator = new Jsonator();
						String toSendJson = jnator.writeUserSignUpInfo(true);
						Log.d("ProfileInActivity", "JSON to send: "+toSendJson);
						ConexionRest conn = new ConexionRest(null);
						String signUrl = conn.getBaseUrl() + "/users/" + Integer.toString(ui.getIntegerId());
						Log.d("ProfileInActivity", "URL to put: " + signUrl);
						conn.generatePut(toSendJson, signUrl, null);
					}
					catch(Exception e){
						Log.e("ProfileInActivity", "Sunmitting PUT error: ", e);
					}

				}

				ActivityChanger.getInstance().gotoActivity(ProfileActivity.this, MainActivity.class);

			}
		});

		cancelBtn = (Button) findViewById(R.id.profileCancelBtn);
		cancelBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				ActivityChanger.getInstance().gotoActivity(ProfileActivity.this, MainActivity.class);
			}
		});
	}
}