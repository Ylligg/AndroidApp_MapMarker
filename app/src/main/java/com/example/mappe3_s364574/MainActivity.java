package com.example.mappe3_s364574;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {
        TextView textView;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

                // har en textview for å displaye dataene til jsonout.php
                textView = (TextView) findViewById(R.id.jasontekst);
                // sender request til jsonout.php

                getJSON task = new getJSON();
                task.execute("https://dave3600.cs.oslomet.no/~s364574/jsonout.php");

                // har en knapp for å gå til kart med markører
                Button button = findViewById(R.id.button);

                // når man trykker knappen så vil man gå til kartet
                button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, Activity_Maps.class);
                                startActivity(intent);
                        }
                });
        }

        // samme funksjon som i activity_maps se kommentarer der
        @SuppressLint("StaticFieldLeak")
        @SuppressWarnings("deprecation")
        private class getJSON extends AsyncTask<String, Void,String> {
                JSONObject jsonObject;
                @Override
                protected String doInBackground(String... urls) {
                        String retur = "";
                        String s = "";
                        String output = "";
                        for (String url : urls) {

                                try {
                                URL urlen = new URL(urls[0]);
                                HttpsURLConnection conn = (HttpsURLConnection)
                                        urlen.openConnection();
                                conn.setRequestMethod("GET");
                                conn.setRequestProperty("Accept","application/json");
                                conn.connect();

                                if (conn.getResponseCode() != 200) {
                                        throw new RuntimeException("Failed : HTTP error code :"+
                                                conn.getResponseCode());
                                }
                                BufferedReader br = new BufferedReader(new InputStreamReader(
                                        (conn.getInputStream())));
                                System.out.println("Output from Server .... \n");
                                while ((s = br.readLine()) != null) {
                                        output = output + s;
                                }
                                conn.disconnect();

                                try { JSONArray mat = new JSONArray(output);
                                        for (int i = 0; i < mat.length(); i++) {
                                                JSONObject jsonobject = mat.getJSONObject(i);
                                                String name = jsonobject.getString("NamePlace");
                                                String des = jsonobject.getString("Description");
                                                String adress = jsonobject.getString("Address");
                                                String cLAT = jsonobject.getString("CoordsLAT");
                                                String cLONG = jsonobject.getString("CoordsLONG");
                                                System.out.println("mjay"+ name);
                                                retur = retur + "Place: "+ name +" Description: "+ des +" Address: "+ adress +" Latitude: "+ cLAT +" Longitude: "+ cLONG + "\n\n";
                                        }
                                        return retur;
                                } catch (JSONException e) {
                                        e.printStackTrace();
                                }
                                return retur;
                        } catch (Exception e) {
                                return "Noe gikk feil" + e;
                        }
                }
                return retur;
        }
        @Override
        protected void onPostExecute(String ss) {
                textView.setText(ss);
        }
    }
}
