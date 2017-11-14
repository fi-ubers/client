package com.example.Principal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

/**
 * The main menu {@link Activity} of the app. The starting point for the very
 * users operations.
 */
public class MainActivity extends Activity {
	Button restApiBtn, anActBtn;
	Button editProfBtn, chatBtn;

	/**
	 * Activity onCreate method.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Fiuber MainActivity", "Main activity started!");

/*		TODO: Put this on the right place
		if (FirebaseAuth.getInstance().getCurrentUser() != null)
			AuthUI.getInstance().signOut(this);*/

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

		restApiBtn = (Button) findViewById(R.id.restApiBtn);
		restApiBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, RestApiActivity.class);
			}
		});

		chatBtn = (Button) findViewById(R.id.chatBtn);
		chatBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, ChatActivity.class);
			}
		});

		anActBtn = (Button) findViewById(R.id.anActBtn);
		anActBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, SelectTripActivity.class);
			}
		});

		editProfBtn = (Button) findViewById(R.id.editProfBtn);
		editProfBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, ProfileActivity.class);
			}
		});

	}

	/**
	 * Simple method for loging out.
	 * @param view The current {@link View}
	 */
	public void logout(View view) {
		Log.i("Fiuber Main activity", "Logging out user");
		UserInfo.getInstance().seppuku();
		LoginManager.getInstance().logOut();
		ActivityChanger.getInstance().gotoLogInScreen(this);
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