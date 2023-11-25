package com.example.mappe3_s364574;

import androidx.annotation.NonNull;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mappe3_s364574.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import android.annotation.SuppressLint;
import android.os.AsyncTask;

import javax.net.ssl.HttpsURLConnection;

public class Activity_Maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    EditText sted, beskrivelse, addresse;
    String steder = "";


    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getJSON task = new getJSON();
        task.execute("https://dave3600.cs.oslomet.no/~s364574/jsonout.php");

    }



    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class getJSON extends AsyncTask<String, Void,String> {
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
                        steder = steder + s;
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
            System.out.println(ss);
        }
    }

    //gjør kartet klar med markørere fra jsonobjektet

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if(mMap!=null) {

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                try{
                    JSONArray mat = new JSONArray(steder);
                    for (int i = 0; i < mat.length(); i++) {
                        JSONObject jsonobject = mat.getJSONObject(i);
                        String name = jsonobject.getString("NamePlace");
                        String des = jsonobject.getString("Description");
                        String adress = jsonobject.getString("Address");
                        double cLAT = jsonobject.getDouble("CoordsLAT");
                        double cLONG = jsonobject.getDouble("CoordsLONG");

                        LatLng coordinater = new LatLng(cLAT, cLONG);
                        mMap.addMarker(new MarkerOptions().position(coordinater).title("Marker in " + name).snippet("Description: " + des + " Sted: " + adress));

                    }

                }catch (JSONException e) {
                    System.out.println("funket ikke å lage markører");
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                Toast.makeText(Activity_Maps.this, "CLICK ON MAP" + latLng, Toast.LENGTH_SHORT).show();
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.custom_alert, null);

                sted = (EditText) view.findViewById(R.id.navnsted);
                beskrivelse = (EditText) view.findViewById(R.id.beskrivelse);
                addresse = (EditText) view.findViewById(R.id.addresse);

                new AlertDialog.Builder(Activity_Maps.this)
                        .setMessage("ny Markør")
                        .setCancelable(false)
                        .setView(view)
                        .setPositiveButton("ja", new DialogInterface.OnClickListener() {
                            @SuppressLint("ResourceType")
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String stedtxt = sted.getText().toString();
                                String beskrivelsetxt = beskrivelse.getText().toString();
                                String addressetxt = addresse.getText().toString();
                                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in " + stedtxt).snippet("Description: " + beskrivelsetxt + " Sted: " + addressetxt));
                                System.out.println(latLng + " : " + latLng.latitude);

                            }
                        })
                        .setNegativeButton("nei", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // on marker click we are getting the title of our marker
                // which is clicked and displaying it in a toast message.
                String markerName = marker.getTitle();
                Toast.makeText(Activity_Maps.this, "Clicked location is " + markerName, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private void postJSON(String url){

        getJSON post = new getJSON();
        post.execute(url);

    }

}