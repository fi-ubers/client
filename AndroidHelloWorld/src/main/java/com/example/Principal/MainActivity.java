package com.keylesson.Principal;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.example.Principal.ConexionRest;


public class MainActivity extends Activity {

	EditText userName, userId;
	Button getBtn, postBtn, deleteBtn;
	TextView resultTxtView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Fiuber Main activity", "Main activity started!");

		userName = (EditText) findViewById(R.id.userName);
		userId = (EditText) findViewById(R.id.userId);
		getBtn = (Button) findViewById(R.id.getBtn);
		postBtn = (Button) findViewById(R.id.postBtn);
		deleteBtn = (Button) findViewById(R.id.deleteBtn);
		resultTxtView = (TextView) findViewById(R.id.resultTxtView);

		// Intento generar un GET
		getBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				String uName = "";
				Integer uId = 0;
				try {
					uId = Integer.parseInt(userId.getText().toString());
				}
				catch(Exception e){
					Log.e("Fiuber Main activity", "exception", e);
				}

				ConexionRest conThread = new ConexionRest();
				conThread.generarGet(uId, resultTxtView);
			}
		});

		// Intento generar un POST
		postBtn.setOnClickListener(new View.OnClickListener(){
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

				ConexionRest conThread = new ConexionRest();
				conThread.generarPost(uId, uName, resultTxtView);
			}
		});

		// Intento generar un DELETE
		deleteBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				Integer uId = 0;
				try {
					uId = Integer.parseInt(userId.getText().toString());
				}
				catch(Exception e){
					Log.e("Fiuber Main activity", "exception", e);
				}

				ConexionRest conThread = new ConexionRest();
				conThread.generarDelete(uId, resultTxtView);
			}
		});

	}



}
