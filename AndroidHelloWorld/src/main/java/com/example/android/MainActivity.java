package com.example.android;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RatingBar;
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
import org.w3c.dom.Text;

/**
 * The main menu {@link Activity} of the app. The starting point for the very
 * users operations.
 */
public class MainActivity extends Activity {
	Button bigRedButton, anActBtn;
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

		bigRedButton = (Button) findViewById(R.id.bigRedButton);


		chatBtn = (Button) findViewById(R.id.chatBtn);
		// Overloeaded button for everything! Muahahaha
		anActBtn = (Button) findViewById(R.id.anActBtn);

		editProfBtn = (Button) findViewById(R.id.editProfBtn);

		setButtons();
		// Force confirm driver
		if(UserInfo.getInstance().getUserStatus() == UserStatus.P_EXAMINING_DRIVER){
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent();
					intent.putExtra("extra", "Please confirm your driver");
					intent.setAction("com.example.android.onMessageReceived");
					String ouiDest = UserInfo.getInstance().getOtherUser().getDest();
					while((ouiDest == null) || (ouiDest.length() < 1)){
						SystemClock.sleep(1000);
						ouiDest = UserInfo.getInstance().getOtherUser().getDest();
					}
					sendBroadcast(intent);
				}
			}, 3000);
		}

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
		anActBtn.setEnabled(true);
		anActBtn.setVisibility(View.VISIBLE);
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
			anActBtn.setText("Trip on course");
			anActBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ActivityChanger.getInstance().gotoActivity(MainActivity.this, TripEnRouteActivity.class);
				}
			});
		} else if(uSta == UserStatus.P_EXAMINING_DRIVER){
			anActBtn.setEnabled(false);
			anActBtn.setVisibility(View.INVISIBLE);
		}

		// Set profile button (center circle)
		editProfBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, ProfileActivity.class);
			}
		});

		if(uSta.tripCanStart()) {
			bigRedButton.setEnabled(true);
			bigRedButton.setVisibility(View.VISIBLE);
			bigRedButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// Start trip!
					try {
						ConexionRest conn = new ConexionRest(null);
						String tripId = PathInfo.getInstance().getTripId();
						String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
						Log.d("MainActivity", "URL to start trip: " + tripUrl);
						conn.generatePost("{ \"action\": \"start\" }", tripUrl, null);
						if(UserInfo.getInstance().isDriver())
							UserInfo.getInstance().setUserStatus(UserStatus.D_TRAVELLING);
						else
							UserInfo.getInstance().setUserStatus(UserStatus.P_TRAVELLING);

						setButtons();
					} catch (Exception e) {
						Log.e("MainActivity", "starting trip error: ", e);
					}
				}
			});
		} else {
			bigRedButton.setEnabled(false);
			bigRedButton.setVisibility(View.INVISIBLE);
		}
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

	public void confirmRejectDriver(){

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

		public void confirmRejectDriver(){
			if(UserInfo.getInstance().getUserStatus() == UserStatus.P_EXAMINING_DRIVER){
				final Dialog dialog = new Dialog(MainActivity.this);
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
				dialog.setTitle("A driver wants to pick you up!");
				dialog.setContentView(R.layout.confirm_driver_input_box);
				TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
				txtMessage.setText("Please confirm or reject driver");
				txtMessage.setTextColor(Color.parseColor("#ff2222"));

				final OtherInfoFragment fr = (OtherInfoFragment) getFragmentManager().findFragmentById(R.id.fragDriverInfo);
				fr.updateFragmentElements(UserInfo.getInstance().getOtherUser(), false);

				RatingBar rStars = (RatingBar) dialog.findViewById(R.id.ratingBarShowing);
				LayerDrawable strs = (LayerDrawable) rStars.getProgressDrawable();
				strs.getDrawable(2).setColorFilter(Color.parseColor("#ffd700"), PorterDuff.Mode.SRC_ATOP);
				//rStars.getProgressDrawable().getDra setColorFilter(Color.parseColor("#ffd700"), PorterDuff.Mode.SRC_ATOP);
				rStars.setRating((float) UserInfo.getInstance().getOtherUser().getDriverRate());
				TextView txtViewRateCount = (TextView) dialog.findViewById(R.id.txtViewRateCount);
				int rateCount = UserInfo.getInstance().getOtherUser().getDriverRateCount();
				txtViewRateCount.setText("Driver rating (" + rateCount + " reviews):");

				Button btConfirm =(Button) dialog.findViewById(R.id.confirmDriver);
				btConfirm.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							ConexionRest conn = new ConexionRest(null);
							String tripId = PathInfo.getInstance().getTripId();
							String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
							Log.d("MainActivity", "URL to confirm driver: " + tripUrl);
							conn.generatePost("{ \"action\": \"confirm\" }", tripUrl, null);
						} catch (Exception e) {
							Log.e("MainActivity", "Confirming driver error: ", e);
						}
						getFragmentManager().beginTransaction().remove(fr).commit();
						dialog.dismiss();
					}
				});

				Button btCancel = (Button) dialog.findViewById(R.id.rejectDriver);
				btCancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							ConexionRest conn = new ConexionRest(null);
							String tripId = PathInfo.getInstance().getTripId();
							String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
							Log.d("MainActivity", "URL to cancel driver: " + tripUrl);
							conn.generatePost("{ \"action\": \"reject\" }", tripUrl, null);
						} catch (Exception e) {
							Log.e("MainActivity", "Cancelling driver error: ", e);
						}
						getFragmentManager().beginTransaction().remove(fr).commit();
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			String state = extras.getString("extra");
			Toast.makeText(getApplicationContext(), state, Toast.LENGTH_SHORT).show();
			setButtons();
			this.confirmRejectDriver();
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