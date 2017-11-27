package com.example.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

/**
 * The main menu {@link Activity} of the app. The starting point for the very
 * users operations.
 */
public class MainActivity extends Activity {
	Button restApiBtn, anActBtn;
	Button editProfBtn, chatBtn;
	private MyBroadcastReceiver mbr;

	/**
	 * Activity onCreate method.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Fiuber MainActivity", "Main activity started!");

		if (AccessToken.getCurrentAccessToken() == null) {
			// If here, either user has logged in manually or
			// haven't logged in at all. If user has logged in,
			// UserInfo must have been initialized. If it's not,
			// then user has never signed in.
			if (!UserInfo.getInstance().wasInitialized()) {
				Log.e("Fiuber MainActivity", "Token missed!");
				ActivityChanger.getInstance().gotoLogInScreen(this);
			}
			// If here, user already signed in manually.
			Log.i("Fiuber MainActivity", "User has logged in manually");
			// IF USER HAS NO FB ACCOUNT, USE DEFAULT!!
			ProfilePictureView profilePictureView;
			profilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
			profilePictureView.setProfileId("107457569994960");
			UserInfo ui = UserInfo.getInstance();
			((TextView) findViewById(R.id.fbUsrName)).setText(ui.getFirstName());
		} else {
			Log.i("Fiuber MainActivity", "User has logged in with FB");
			// If here, the user has logged in using Facebook.
			// Let's initialize UserInfo based on that
			FbLogger flogger = new FbLogger();
			flogger.initializeUserInfoFacebook();

			// TODO: check if it's first time, or if user's just logging in
			// if(!userInAppServer())
			//		RegisterInAppServer();
			// else
			//		initializeUserFromAppServer();
			// TODO: Move all of this outside
			// Facebook fields
			Profile curProfile = Profile.getCurrentProfile();
			ProfilePictureView profilePictureView;
			profilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
			profilePictureView.setProfileId(curProfile.getId());
			Log.d("MainActivity", "Profile id is:" + curProfile.getId());
			((TextView) findViewById(R.id.fbUsrName)).setText("Token: " + AccessToken.getCurrentAccessToken().getToken());
			//((TextView) findViewById(R.id.fbUsrName)).setText(UserInfo.getInstance().getFirstName());
		}

		FirebaseMessaging.getInstance().subscribeToTopic(UserInfo.getInstance().getUserId());
		mbr = new MyBroadcastReceiver();

		Log.i("MainActivity", "User loaded state is:" + UserInfo.getInstance().getUserStatus().getCode());
		// TODO: Delete on near future
		restApiBtn = (Button) findViewById(R.id.restApiBtn);
		restApiBtn.setEnabled(false);
		restApiBtn.setVisibility(View.INVISIBLE);
		restApiBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, ChoosePassengerActivity.class);
			}
		});

		chatBtn = (Button) findViewById(R.id.chatBtn);
		// Overloeaded button for everything! Muahahaha
		anActBtn = (Button) findViewById(R.id.anActBtn);

		editProfBtn = (Button) findViewById(R.id.editProfBtn);

		setButtons();

	}

	private void setButtons(){
		// Set chat button
		if(!UserInfo.getInstance().getUserStatus().chatEnabled()){
			chatBtn.setEnabled(false);
			chatBtn.setVisibility(View.INVISIBLE);
		} else {
			chatBtn.setEnabled(true);
			chatBtn.setVisibility(View.VISIBLE);
			chatBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ActivityChanger.getInstance().gotoActivity(MainActivity.this, ChatActivity.class);
				}
			});
		}
		// Set super button overloaded
		UserStatus uSta = UserInfo.getInstance().getUserStatus();
		if(uSta.tripCreationEnabled()){
			anActBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ActivityChanger.getInstance().gotoActivity(MainActivity.this, SelectTripActivity.class);
				}
			});
		} else if(uSta.choosePassengerEnabled()){
			if(UserInfo.getInstance().getCars().size() == 0){
				Toast.makeText(getApplicationContext(), "You need to register a car first!", Toast.LENGTH_SHORT).show();
			} else {
				anActBtn.setText("Find trips");
				anActBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ActivityChanger.getInstance().gotoActivity(MainActivity.this, SelectTripActivity.class);
					}
				});
			}
		} else if(uSta.tripOtherInfoEnabled()){
			anActBtn.setText("Trip info");
			anActBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ActivityChanger.getInstance().gotoActivity(MainActivity.this, TripOtherInfoActivity.class);
				}
			});
		} else if(uSta.tripEnRouteEnabled()){
			//anActBtn.setText("Trip on course");
			anActBtn.setText("NOT IMPLEMENTED YET");
			anActBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ActivityChanger.getInstance().gotoActivity(MainActivity.this, TripInfoActivity.class);
				}
			});
		}

		// Set profile button (center circle)
		editProfBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, ProfileActivity.class);
			}
		});
	}

	@Override
	public void onResume(){
		super.onResume();
		setButtons();
		IntentFilter intFil = new IntentFilter();
		intFil.addAction("com.example.android.onMessageReceived");
		registerReceiver(mbr, intFil);
	}

	@Override
	public void onPause(){
		super.onPause();
		setButtons();
		unregisterReceiver(mbr);
	}

	/**
	 * Simple method for loging out.
	 * @param view The current {@link View}
	 */
	public void logout(View view) {
		Log.i("Fiuber Main activity", "Logging out user");
		UserInfo.getInstance().seppuku();
		LoginManager.getInstance().logOut();
		if (FirebaseAuth.getInstance().getCurrentUser() != null)
			AuthUI.getInstance().signOut(this);
		String PREFS_FILE = "AuthFile";
		getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit().clear().apply();
		ActivityChanger.getInstance().gotoLogInScreen(this);
	}


// --------------------------------------------------------------------------------------------------------

	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			String state = extras.getString("extra");
			Toast.makeText(getApplicationContext(), state, Toast.LENGTH_SHORT).show();
			setButtons();
		}
	}

// --------------------------------------------------------------------------------------------------------
	/**
	 * Auxiliar class to handle logins with Faceboook
	 */
	public class FbLogger implements RestUpdate {
		private int step;

		/**
		 * Class constructor
		 */
		FbLogger(){
			step = 0;
		}

		public void logUser() {
			try {
				step = 1;
				Jsonator jnator = new Jsonator();
				UserInfo ui = UserInfo.getInstance();
				String toSendJson = jnator.writeUserLoginCredentials(ui.getUserId(), " ", ui.getFbToken());
				ConexionRest conn = new ConexionRest(this);
				String urlReq = conn.getBaseUrl() + "/users/login";
				Log.d("MainActivity", "JSON to send: "+ toSendJson);
				conn.generatePost(toSendJson, urlReq, null);
			}
			catch(Exception e){
				Log.e("MainActivity", "FB log in error: ", e);
			}
		}

		protected Boolean checkUserLog(String servResponse) {
			Jsonator jnator = new Jsonator();
			if(jnator.userLoggedInIsOk(servResponse)){
				String prevFbToken = UserInfo.getInstance().getFbToken();
				jnator.readUserLoggedInInfo(servResponse);
				UserInfo ui = UserInfo.getInstance();

				ui.initializeUserInfo(ui.getUserId(), ui.getEmail(), ui.getFirstName(),
						ui.getLastName(), ui.getCountry(), ui.getBirthdate(), "",
						prevFbToken, ui.getAppServerToken());
				return true;
			}
			else
				return false;
		}

		@Override
		public void executeUpdate(String servResponse) {
			if(step == 1) {
				// Acabo de intentar el primer login
				Log.d("ManualSignInActivity", "Response from server: " + servResponse);
				if (!this.checkUserLog(servResponse)) {
					step = 2;
					// TODO: Register user
					}
				}
			if(step == 2){
				// TODO: Check if it was registered ok and login again
				}

		}

		public void initializeUserInfoFacebook() {
			GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
				@Override
				public void onCompleted(JSONObject object, GraphResponse response) {

					final JSONObject json = response.getJSONObject();
					Log.d("MainActivity", "SuperFB Json:" + json.toString());

					try {
						if (json != null) {
							String[] name = json.getString("name").split(" ");
							String mail = json.getString("email");
							String id = json.getString("id");
							String bth = json.getString("birthday");
							String fbTkn = AccessToken.getCurrentAccessToken().getToken();
							UserInfo ui = UserInfo.getInstance();

							ui.initializeUserInfo(id, mail, name[0], name[1], "Argentina", bth, "", fbTkn, "");
							//web.loadData(text, "text/html", "UTF-8");
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			Bundle parameters = new Bundle();
			parameters.putString("fields", "id,name,link,email,location,picture,birthday");
			request.setParameters(parameters);
			request.executeAsync();
			// Till here
		}
	}

}