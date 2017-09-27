package com.example.Principal;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Provides transparency in making HTTP requests to the app-server via REST API.
 */
public class ConexionRest extends AsyncTask<Void, Integer, String> {

    private String appUrlString = "https://fiuber-app-server-test.herokuapp.com/greet/1";
    private String baseUrlString = "https://fiuber-app-server-test.herokuapp.com";
    private String restMethod = "GET";
    private TextView resultTxtView;
    private String toSendText = ""; // Sólo para enviar con POST
    private URL appUrl;

    /**
     * Generates a GET request to the app-server for retrieving a user data.
     * @param urlRequest The URL for making the request
     * @param txtVw TextView to update with the app-server response
     */
    public void generateGet(String urlRequest, TextView txtVw){
        if(this.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w("Fiuber ConexionRest", "cannot GET: other task running");
            return;
        }
        appUrlString = urlRequest;
        restMethod = "GET";
        resultTxtView = txtVw;
        this.execute();
    }

    /**
     * Generates a POST request to the app-server for creating a new user.
     * @param jsonRequest The string in Json format for the POST
     * @param urlRequest The URL for making the request
     * @param txtVw TextView to update with the app-server response
     */
    public void generatePost(String jsonRequest, String urlRequest, TextView txtVw){
        if(this.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w("Fiuber ConexionRest", "cannot POST: other task running");
            return;
        }

        toSendText = jsonRequest;
        Log.i("Fiuber ConexionRest", "Json is:" + toSendText);
        appUrlString = urlRequest;
        restMethod = "POST";
        resultTxtView = txtVw;
        this.execute();
    }

    /**
     * Generates a DELETE request to the app-server for removing a user data.
     * @param urlRequest The URL for making the request
     * @param txtVw TextView to update with the app-server response
     */
    public void generateDelete(String urlRequest, TextView txtVw){
        if(this.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w("Fiuber ConexionRest", "cannot DELETE: other task running");
            return;
        }
        appUrlString = urlRequest;
        restMethod = "DELETE";
        resultTxtView = txtVw;
        this.execute();
    }

    /**
     STILL NOT IMPLEMENTED
     */
    @Override
    protected void onPreExecute() {

    }

    /**
     STILL NOT IMPLEMENTED
     */
    @Override
    protected void onPostExecute(String aVoid) {
        resultTxtView.setText(aVoid);
    }

    /**
     STILL NOT IMPLEMENTED
     */
    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    /**
     STILL NOT IMPLEMENTED
     */
    @Override
    protected void onCancelled() {

    }

    /**
     * Private method for reading the app-server response after a request.
     * @param conn The {@link HttpsURLConnection} with the app-server
     * @return A {@link StringBuilder} class with the app-server full response
     */
    private StringBuilder readFromConnection(HttpsURLConnection conn){
        StringBuilder outputLine = new StringBuilder();
        try {
            int rsp = conn.getResponseCode();
            Boolean rspIsOk = (rsp == HttpURLConnection.HTTP_OK);
            rspIsOk = rspIsOk || ((rsp == HttpURLConnection.HTTP_CREATED) && (restMethod == "POST"));
            if (rspIsOk) {
                BufferedReader lector = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String curLine = lector.readLine();
                while (curLine != null) {
                    outputLine.append(curLine);
                    curLine = lector.readLine();
                }

                lector.close();
            }
            else {
                Log.e("Fiuber ConexionRest", "cannot connect, error code: " + String.valueOf(rsp));
            }
        }
        catch (Exception e) {
            Log.e("Fiuber ConexionRest", "exception", e);
        }
        return outputLine;
    }

    /**
     * Overrided {@link AsyncTask} method for connection execution. This
     * method is the one which actually make the REST requests.
     */
    @Override
    protected String doInBackground(Void... params) {
        StringBuilder outputLine = new StringBuilder();
        try {
            appUrl = new URL(appUrlString);
            HttpsURLConnection conn = (HttpsURLConnection) appUrl.openConnection();
            conn.setRequestMethod(restMethod);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 1.5; es-Es) HTTP");

            if(restMethod.equals("GET")) {
                outputLine = this.readFromConnection(conn);
            }
            else if(restMethod.equals("POST")){
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                DataOutputStream  os = new DataOutputStream (conn.getOutputStream());
                Log.d("Fiuber ConexionRest", "Trying to send request: " + toSendText);

                os.writeBytes(toSendText);
                os.close();

                // Recibo el ACK
                outputLine = this.readFromConnection(conn);
            }
            else if(restMethod.equals("DELETE")){
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
                conn.connect();

                // Recibo el ACK
                outputLine = this.readFromConnection(conn);
            }
            else {
                Log.e("Fiuber ConexionRest", "invalid REST method: " + restMethod);
            }

            conn.disconnect();

        } catch (Exception e) {
            Log.e("Fiuber ConexionRest", "exception", e);
        }
        return outputLine.toString();
    }

    /**
     * Returns base URL with the app-server connection.
     */
    public String getBaseUrl(){
        return this.baseUrlString;
    }
}