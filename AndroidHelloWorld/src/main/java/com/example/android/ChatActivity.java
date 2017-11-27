package com.example.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;


/**
 * An Activity that allows driver and passenger to chat
 */
public class ChatActivity extends Activity {

	ImageView fab;

	private ListView listView;
	/**
	 * Activity onCreate method.
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Create dedicated chat for the pair driver-passenger
		int idMine = UserInfo.getInstance().getIntegerId();
		String idOther = UserInfo.getInstance().getOtherUser().getUserId();
		final String chatName;
		// Chat name is "chat-<driver id>-<passenger id>"
		if(UserInfo.getInstance().isDriver())
			chatName = "chat-" + idMine + "-" + idOther;
		else
			chatName = "chat-" + idOther + "-" + idMine;

		FirebaseApp.initializeApp(this);
		fab = (ImageView) findViewById(R.id.sendBtn);
		final EditText input = (EditText) findViewById(R.id.input);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(input.getText().toString().trim().equals(""))
					return; // empty string, does nothing

				DatabaseReference dr = FirebaseDatabase.getInstance().getReference(chatName);
				String msg = input.getText().toString();
				String uId = UserInfo.getInstance().getUserId();
				String uName = UserInfo.getInstance().getFirstName();
				// Post to app-server the chat message
				try {
					ConexionRest conn = new ConexionRest(null);
					int myId = UserInfo.getInstance().getIntegerId();
					String otherId = UserInfo.getInstance().getOtherUser().getUserId();
					String chatUrl = conn.getBaseUrl() + "/users/" + myId + "/chat";
					Log.d("MainActivity", "URL to post chat: " + chatUrl);
					String toSend = "{ \"receiverId\": "+ otherId + ", \"message\": \"" + msg + "\" }";
					conn.generatePost(toSend, chatUrl, null);
				} catch (Exception e) {
					Log.e("MainActivity", "Posting chat error: ", e);
				}
				// Post to firebase db
				ChatMessage chatMsg = new ChatMessage(msg, uName, uId);
				dr.push().setValue(chatMsg);
				input.setText("");
			}
		});

		listView = (ListView) findViewById(R.id.list);

		if (FirebaseAuth.getInstance().getCurrentUser() == null) {
			Log.d("ChatActivity", "User not logged in into Firebase");
			// Sign in user into Firebase
			UserInfo ui = UserInfo.getInstance();
			try {
				FirebaseAuth.getInstance().signInWithEmailAndPassword(ui.getEmail(), ui.getPassword());
				} catch (Exception e) {
				Log.e("ManualSignInActivity", "Error logging Firebase account: ", e);
				}
			showAllOldMessages(chatName);
			}
			else
				// User is already signed in, show list of messages
				showAllOldMessages(chatName);

	}

	/**
	 * Overrided method for returning to parent {@link Activity}.
	 * @param item {@link MenuItem} clicked on {@link android.app.ActionBar}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.d("ProfileActivity", "Back button pressed on actionBar");
		ActivityChanger.getInstance().gotoActivity(ChatActivity.this, MainActivity.class);
		finish();
		return true;

	}

	/**
	 * Shows the list of messages of the chat. Updates the UI with them.
	 */
	private void showAllOldMessages(String chatName){
		UserInfo ui = UserInfo.getInstance();
		String loggedInUserName = ui.getUserId();

		MessageAdapter adapter = new MessageAdapter(this, ChatMessage.class, R.layout.item_in_message,
				FirebaseDatabase.getInstance().getReference(chatName));
		listView.setAdapter(adapter);
	}


// ----------------------------------------------------------------------------------------------------

	/**
	 * Simple class for modeling messages on driver-passenger chat.
	 */
	public static class ChatMessage {
		private String messageText;
		private String messageUser;
		private String messageUserId;
		private long messageTime;

		/**
		 * Cosntructor for {@link ChatMessage} class.
		 * @param messageText The real message to send
		 * @param messageUser The user who wrote the message (user's firsstName)
		 * @param messageUserId The Id of user who wrote message (user's userId)
		 */
		public ChatMessage(String messageText, String messageUser, String messageUserId) {
			this.messageText = messageText;
			this.messageUser = messageUser;
			messageTime = new Date().getTime();
			this.messageUserId = messageUserId;
		}

		/**
		 * Empty constructor.
		 */
		public ChatMessage(){

		}

		/**
		 * Gets the messageUserId.
		 */
		public String getMessageUserId() {
			return messageUserId;
		}

		/**
		 * Sets the messageText.
		 * @param messageUserId The Id of user who wrote message (user's userId)
		 */
		public void setMessageUserId(String messageUserId) {
			this.messageUserId = messageUserId;
		}

		/**
		 * Gets the messageText.
		 */
		public String getMessageText() {
			return messageText;
		}

		/**
		 * Sets the messageText.
		 * @param messageText The real message to send
		 */
		public void setMessageText(String messageText) {
			this.messageText = messageText;
		}

		/**
		 * Gets the messageUser
		 */
		public String getMessageUser() {
			return messageUser;
		}

		/**
		 * Sets the messageText.
		 * @param messageUser The user who wrote the message (user's firsstName)
		 */
		public void setMessageUser(String messageUser) {
			this.messageUser = messageUser;
		}

		/**
		 * Gets the message creation time.
		 */
		public long getMessageTime() {
			return messageTime;
		}

		/**
		 * Sets the messageText.
		 * @param messageTime Time when this message was created
		 */
		public void setMessageTime(long messageTime) {
			this.messageTime = messageTime;
		}
	}

// ---------------------------------------------------------------------------------------------------------

	/**
	 * And God gave {@link MessageAdapter} the divine purpose of populating
	 * the list of messages in the UI with concrete {@link ChatMessage} sent
	 * between passenger and driver.
	 */
	public class MessageAdapter extends FirebaseListAdapter<ChatMessage> {

		private Activity activity;

		/**
		 * Cosntructor for {@link MessageAdapter} class.
		 * @param activity The {@link Activity} with UI to update
		 * @param modelClass The {@link Class} of object to fill the UI with
		 * @param modelLayout The id of the layout to use to fill the modelClass' objects
		 * @param ref The Firebase {@link DatabaseReference} to use
		 */
		public MessageAdapter(Activity activity, Class<ChatMessage> modelClass, int modelLayout, DatabaseReference ref) {
			super(activity, modelClass, modelLayout, ref);
			this.activity = activity;
		}

		@Override
		protected void populateView(View v, ChatMessage model, int position) {
			Log.d("ChatActivity", "Message is: "+ model.getMessageText());
			TextView messageText = (TextView) v.findViewById(R.id.message_text);
			TextView messageUser = (TextView) v.findViewById(R.id.message_user);
			TextView messageTime = (TextView) v.findViewById(R.id.message_time);

			messageText.setText(model.getMessageText());
			messageUser.setText(model.getMessageUser());

			// Format the date before showing it
			Date msgDate = new Date(model.getMessageTime());
			messageTime.setText(DateFormat.getInstance().format(msgDate));
			//messageTime.setText(DateFormat.getD format("dd-MM-yyyy (HH:mm:ss)", ));
		}

		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
			ChatMessage chatMessage = getItem(position);
			UserInfo ui = UserInfo.getInstance();
			if (chatMessage.getMessageUserId().equals(ui.getUserId()))
				view = activity.getLayoutInflater().inflate(R.layout.item_out_message, viewGroup, false);
			else
				view = activity.getLayoutInflater().inflate(R.layout.item_in_message, viewGroup, false);

			//generating view
			populateView(view, chatMessage, position);

			return view;
		}

		@Override
		public int getViewTypeCount() {
			// return the total number of view types. this value should never change
			// at runtime
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			// return a value between 0 and (getViewTypeCount - 1)
			return position % 2;
		}
	}
}