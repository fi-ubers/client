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

import org.json.JSONObject;

/**
 * The main menu {@link Activity} of the app. The starting point for the very
 * users operations.
 */
public class MainActivity extends Activity {
	Button restApiBtn, anActBtn;
	Button editProfBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Fiuber MainActivity", "Main activity started!");
		if(AccessToken.getCurrentAccessToken() == null) {
			// If here, either user has logged in manually or
			// haven't logged in at all. If user has logged in,
			// UserInfo must have been initialized. If it's not,
			// then user has never signed in.
			if(!UserInfo.getInstance().wasInitialized()) {
				Log.e("Fiuber MainActivity", "Token missed!");
				ActivityChanger.getInstance().gotoLogInScreen(this);
				}
			// If here, user already signed in manually.
			// TODO: Handle manual log in
			Log.i("Fiuber MainActivity", "User has logged in manually");
			}
		else {
			Log.i("Fiuber MainActivity", "User has logged in with FB");
			// If here, the user has logged in using Facebook.
			// Let's initialize UserInfo based on that
			// TODO: check if it's first time, or if user's just loggin in
			// if(!userInAppServer())
			//		RegisterInAppServer();
					initializeUserInfoFacebook();
			// else
			//		initializeUserFromAppServer();
			// TODO: Move all of this outside
			// Facebook fields
			Profile curProfile = Profile.getCurrentProfile();
			ProfilePictureView profilePictureView;
			profilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
			profilePictureView.setProfileId(curProfile.getId());


			((TextView) findViewById(R.id.fbUsrName)).setText(UserInfo.getInstance().getUserName());
		}

		restApiBtn = (Button) findViewById(R.id.restApiBtn);
		restApiBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, RestApiActivity.class);
			}
		});

        anActBtn = (Button) findViewById(R.id.anActBtn);
        anActBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ActivityChanger.getInstance().gotoActivity(MainActivity.this, SelectTripActivity.class);
            }
        });

		editProfBtn = (Button) findViewById(R.id.editProfBtn);
		editProfBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				ActivityChanger.getInstance().gotoActivity(MainActivity.this, ProfileActivity.class);
			}
		});

	}

	/**
	 * Simple method for loging out.
	 */
	public void logout(View view) {
		Log.i("Fiuber Main activity", "Logging out user");
		UserInfo.getInstance().seppuku();
		LoginManager.getInstance().logOut();
		ActivityChanger.getInstance().gotoLogInScreen(this);
	}

	private void initializeUserInfoFacebook(){
		// New shit
		GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
			@Override
			public void onCompleted(JSONObject object, GraphResponse response) {

				final JSONObject json = response.getJSONObject();

				try {
					if(json != null){
						String name = json.getString("name");
						String mail = json.getString("email");
						String id = json.getString("id");
						String bth = json.getString("birthday");
						UserInfo.getInstance().initializeUserInfo(mail, name, id, bth);
						//web.loadData(text, "text/html", "UTF-8");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Bundle parameters = new Bundle();
		parameters.putString("fields", "id,name,link,email,picture,birthday");
		request.setParameters(parameters);
		request.executeAsync();
		// Till here
	}

}
