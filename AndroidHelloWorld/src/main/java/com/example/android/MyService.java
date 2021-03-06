package com.example.android;


import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.firebase.database.Transaction;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyService extends FirebaseMessagingService {
	public MyService() {
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		String TAG = "MyService";
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: " + remoteMessage.getFrom());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
			String capello = remoteMessage.getNotification().getBody();
			Intent intent = new Intent();
			intent.putExtra("extra", capello);
			intent.setAction("com.example.android.onMessageReceived");
			StatusUpdater supt = new StatusUpdater(intent);
			supt.updateStatus();
		}

		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	/*	int notificationId = new Random().nextInt(60000);
		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this) .setSmallIcon(R.drawable.ic_notification_small) //a resource for your custom small icon
		.setContentTitle(remoteMessage.getData().get("title")) //the "title" value you sent in your notification
		.setContentText(remoteMessage.getData().get("message")) //ditto
		.setAutoCancel(true) //dismisses the notification on click
		.setSound(defaultSoundUri);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); notificationManager.notify(notificationId , notificationBuilder.build());
*/	}

// ------------------------------------------------------------------------------------------

	private class StatusUpdater implements RestUpdate{
		Intent intent;

		public StatusUpdater(Intent intent){
			this.intent = intent;
		}

		public void updateStatus(){
			try {
				ConexionRest conn = new ConexionRest(this);
				String urlReq = conn.getBaseUrl() + "/users/" + UserInfo.getInstance().getIntegerId();
				Log.d("StatusUpdater", "Updating status at " + urlReq);
				conn.generateGet(urlReq, null);
			}
			catch(Exception e){
				Log.e("LoginActivity", "Manual log in error: ", e);
			}
		}

		@Override
		public void executeUpdate(String servResponse) {
			UserInfo ui = UserInfo.getInstance();
			String mPassword = ui.getPassword();
			String mFbToken = ui.getFbToken();
			String mAppToken = ui.getAppServerToken();

			Jsonator jnator = new Jsonator();
			jnator.readUserLoggedInInfo(servResponse);

			ui.initializeUserInfo(ui.getUserId(), ui.getEmail(), ui.getFirstName(),
					ui.getLastName(), ui.getCountry(), ui.getBirthdate(), mPassword, mFbToken, mAppToken);

			if(ui.getUserStatus() == UserStatus.P_EXAMINING_DRIVER) {
				// Delay update a bit
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						String ouiDest = UserInfo.getInstance().getOtherUser().getDest();
						while((ouiDest == null) || (ouiDest.length() < 1)){
							SystemClock.sleep(1000);
							ouiDest = UserInfo.getInstance().getOtherUser().getDest();
						}
						sendBroadcast(intent);
					}
				}, 3000);
			} else
				sendBroadcast(intent);

		}
	}
}
