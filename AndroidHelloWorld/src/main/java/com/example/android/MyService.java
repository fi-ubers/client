package com.example.android;


import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyService extends FirebaseMessagingService {
	public MyService() {
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		String TAG = "MyService";

		// TODO(developer): Handle FCM messages here.
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: " + remoteMessage.getFrom());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
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


}
