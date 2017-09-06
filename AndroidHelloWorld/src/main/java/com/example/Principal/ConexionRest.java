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

public class ConexionRest extends AsyncTask<Void, Integer, String> {

    private String appUrlString = "https://fiuber-app-server-test.herokuapp.com/greet/1";
    private String baseUrlString = "https://fiuber-app-server-test.herokuapp.com";
    private String restMethod = "GET";
    private TextView resultTxtView;
    private String toSendText = ""; // Sólo para enviar con POST
    private URL appUrl;


    public void generarGet(Integer userId, TextView txtVw){
        if(this.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w("Fiuber ConexionRest", "cannot GET: other task running");
            return;
        }
        appUrlString = baseUrlString + "/greet/" + String.valueOf(userId);
        restMethod = "GET";
        resultTxtView = txtVw;
        this.execute();
    }

    public void generarPost(Integer userId, String userName, TextView txtVw){
        if(this.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w("Fiuber ConexionRest", "cannot POST: other task running");
            return;
        }
        toSendText = "{ \"user\" : {  \"id\": " + String.valueOf(userId) + ",  \"name\": \""+ userName + "\"} }";
        appUrlString = baseUrlString + "/greet";
        restMethod = "POST";
        resultTxtView = txtVw;
        this.execute();
    }

    public void generarDelete(Integer userId, TextView txtVw){
        if(this.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w("Fiuber ConexionRest", "cannot DELETE: other task running");
            return;
        }
        appUrlString = baseUrlString + "/greet/" + String.valueOf(userId);
        restMethod = "DELETE";
        resultTxtView = txtVw;
        this.execute();
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(String aVoid) {
        resultTxtView.setText(aVoid);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected void onCancelled() {

    }

    // Lee desde la conexión HttpsURLConnection especificada el mensaje
    // que nos envía de respuesta
    private StringBuilder lecturaDesdeConexion(HttpsURLConnection conn){
        StringBuilder lineaSalida = new StringBuilder();
        try {
            int rsp = conn.getResponseCode();
            Boolean rspEsAcorde = (rsp == HttpURLConnection.HTTP_OK);
            rspEsAcorde = rspEsAcorde || ((rsp == HttpURLConnection.HTTP_CREATED) && (restMethod == "POST"));
            if (rspEsAcorde) {
                BufferedReader lector = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String lineaAct = lector.readLine();
                while (lineaAct != null) {
                    lineaSalida.append(lineaAct);
                    lineaAct = lector.readLine();
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
        return lineaSalida;
    }

    @Override
    protected String doInBackground(Void... params) {
        StringBuilder lineaSalida = new StringBuilder();
        try {
            appUrl = new URL(appUrlString);
            HttpsURLConnection conn = (HttpsURLConnection) appUrl.openConnection();
            conn.setRequestMethod(restMethod);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 1.5; es-Es) HTTP");

            if(restMethod == "GET") {
                lineaSalida = this.lecturaDesdeConexion(conn);
            }
            else if(restMethod == "POST"){
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                DataOutputStream  os = new DataOutputStream (conn.getOutputStream());
                Log.d("Fiuber ConexionRest", "Trying to send request: " + toSendText);

                os.writeBytes(toSendText);
                os.close();

                // Recibo el ACK
                lineaSalida = this.lecturaDesdeConexion(conn);
            }
            else if(restMethod == "DELETE"){
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
                conn.connect();

                // Recibo el ACK
                lineaSalida = this.lecturaDesdeConexion(conn);
            }
            else {
                Log.e("Fiuber ConexionRest", "invalid REST method: " + restMethod);
            }

            conn.disconnect();

        } catch (Exception e) {
            Log.e("Fiuber ConexionRest", "exception", e);
        }
        return lineaSalida.toString();
    }
}