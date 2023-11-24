package com.example.mappe3_s364574;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mappe3_s364574.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.net.ssl.HttpsURLConnection;

public class Activity_Maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        try{

            JSONArray mat = new JSONArray(steder);

            for (int i = 0; i < mat.length(); i++) {
                JSONObject jsonobject = mat.getJSONObject(i);
                String name = jsonobject.getString("NamePlace");
                String des = jsonobject.getString("Description");
                String adress = jsonobject.getString("Address");
                double cLAT = jsonobject.getDouble("CoordsLAT");
                double cLONG = jsonobject.getDouble("CoordsLONG");

                LatLng cordinater = new LatLng(cLAT, cLONG);
                mMap.addMarker(new MarkerOptions().position(cordinater).title("Marker in " + name));

            }

        }catch (JSONException e) {
            System.out.println("shit funker ikke");
        }

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                Toast.makeText(Activity_Maps.this, "CLICK ON MAP" + latLng, Toast.LENGTH_SHORT).show();

                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in this place"));

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