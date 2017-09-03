package com.keylesson.Principal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	EditText userName, userId;
	Button requestBtn;
	TextView resultTxtView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Fiuber Main activity", "Main activity started!");

		userName = (EditText) findViewById(R.id.userName);
		userId = (EditText) findViewById(R.id.userId);
		requestBtn = (Button) findViewById(R.id.requestBtn);
		resultTxtView = (TextView) findViewById(R.id.resultTxtView);

		// Intento generar un request
		requestBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				String uName = "";
				Integer uId = 0;
				try {
					uId = Integer.parseInt(userId.getText().toString());
					uName = userName.getText().toString();
				}
				catch(Exception e){
					Log.e("Fiuber Main activity", "exception", e);
				}
				String res = uName + String.valueOf(uId);
				resultTxtView.setText(res);

			}
		});

	}
}
