package com.example.Principal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 *
 */
public class RestApiActivity extends Activity{

    EditText userName, userId;
    Button getBtn, postBtn, deleteBtn;
    TextView resultTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);

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
                Integer uId = 0;
                try {
                    uId = Integer.parseInt(userId.getText().toString());
                }
                catch(Exception e){
                    Log.e("Fiuber MainActivity", "exception", e);
                }

                ConexionRest conThread = new ConexionRest();
                String appUrlString = conThread.getBaseUrl() + "/greet/" + String.valueOf(uId);
                conThread.generateGet(appUrlString, resultTxtView);
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
                    Log.e("Fiuber MainActivity", "exception", e);
                }

                ConexionRest conThread = new ConexionRest();

                Jsonator jsntr = new Jsonator();
                String postReq = jsntr.writeUser(uId, uName);
                String appUrlString = conThread.getBaseUrl() + "/greet";

                conThread.generatePost(postReq, appUrlString, resultTxtView);
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
                    Log.e("Fiuber MainActivity", "exception", e);
                }

                ConexionRest conThread = new ConexionRest();
                String appUrlString = conThread.getBaseUrl() + "/greet/" + String.valueOf(uId);
                conThread.generateDelete(appUrlString, resultTxtView);
            }
        });
    }
}
