package com.example.mappe3_s364574;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

        // gjør kartet klart for å brukes
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // gjør et kall til jsonin filen for å hente dataene
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
                // prøver å lage en tilkobling til siden
                try {
                    URL urlen = new URL(urls[0]);
                    HttpsURLConnection conn = (HttpsURLConnection)
                            urlen.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept","application/json");
                    conn.connect();

                    // om den ikke ble koblet til så gis det en melding
                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code :"+
                                conn.getResponseCode());
                    }

                    // hvis det gikk bra så vil datanee bli lagt til i output og steder variablene
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));
                    System.out.println("Output from Server .... \n");
                    while ((s = br.readLine()) != null) {
                        output = output + s;
                        steder = steder + s;
                    }
                    conn.disconnect();

                    // nå så leser vi jsondataene og omgjør de til vanlig strings og får all infoen vi trenger
                    try { JSONArray mat = new JSONArray(output);
                        for (int i = 0; i < mat.length(); i++) {
                            JSONObject jsonobject = mat.getJSONObject(i);
                            String name = jsonobject.getString("NamePlace");
                            String des = jsonobject.getString("Description");
                            String adress = jsonobject.getString("Address");
                            String cLAT = jsonobject.getString("CoordsLAT");
                            String cLONG = jsonobject.getString("CoordsLONG");

                            // lager en string med all oversatt data som skal sendes til siden
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
            // returnerer dataene
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
        // venter til at kartet er klar for bruk slik at dataene fra json tjenesten kan bli gjort om til markører
        if(mMap!=null) {

        // karett loader ferdig og så vil markørene bli opprettet
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // har en try for å få alle datane fra Jsonout.php
                try{
                    JSONArray mat = new JSONArray(steder);
                    for (int i = 0; i < mat.length(); i++) {
                        JSONObject jsonobject = mat.getJSONObject(i);
                        String name = jsonobject.getString("NamePlace");
                        String des = jsonobject.getString("Description");
                        String adress = jsonobject.getString("Address");
                        double cLAT = jsonobject.getDouble("CoordsLAT");
                        double cLONG = jsonobject.getDouble("CoordsLONG");

                        //oppretter LatLng kordinater som blir brukt for markører som har tittel og beskrivelse
                        // så når vi går inn i kartet så er alle markører opprettet
                        LatLng coordinater = new LatLng(cLAT, cLONG);
                        mMap.addMarker(new MarkerOptions().position(coordinater).title("Marker in " + name).snippet("Description: " + des + " Sted: " + adress));

                    }

                }catch (JSONException e) {
                    System.out.println("funket ikke å lage markører");
                }
            }
        });

        // når alle markører er lagd så kan vi lage nye markører ved å trykke hvor som helst på kartet
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                // lager et input felt med sted,beskrivelse og addresse som skal brukes for vår alert
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.custom_alert, null);

                // initialiserer edittextene
                sted = (EditText) view.findViewById(R.id.navnsted);
                beskrivelse = (EditText) view.findViewById(R.id.beskrivelse);
                addresse = (EditText) view.findViewById(R.id.addresse);

                // strings fra strings.xml
                String tittel = getResources().getString(R.string.nymarkør);

                String ja = getResources().getString(R.string.ja);
                String nei = getResources().getString(R.string.nei);

                // lager en alertdialog
                new AlertDialog.Builder(Activity_Maps.this)
                    .setMessage(tittel)
                    .setCancelable(false)
                    .setView(view)

                        // Når man trykker ja så vil datane fra inputfeltet bli tatt
                        // og bli lagt til en ny addMarker så kan vi se på kartet at det ble opprettet en markør
                        // etter at markøren er der så vil vi at den skal være der når appen startes på nytt
                        // så vi kaller på postJSON for å ta datene og lagre det i jsonin.php slik at markøren blir lagret
                        // slik at neste gang så kan vi fortsatt se den og interacte med den
                    .setPositiveButton(ja, new DialogInterface.OnClickListener() {
                        @SuppressLint("ResourceType")
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String stedtxt = sted.getText().toString();
                            String beskrivelsetxt = beskrivelse.getText().toString();
                            String addressetxt = addresse.getText().toString();
                            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in " + stedtxt).snippet("Description: " + beskrivelsetxt + " Sted: " + addressetxt));
                            System.out.println(latLng + " : " + latLng.latitude);
                            postJSON("https://dave3600.cs.oslomet.no/~s364574/jsonin.php?NamePlace=" + stedtxt + "&Description=" + beskrivelsetxt + "&Address=" + addressetxt + "&CoordsLAT=" +latLng.latitude + "&CoordsLONG=" + latLng.longitude);

                        }
                    })
                        // hvis nei er valgt så skjer ingenting
                    .setNegativeButton(nei, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
            }
        });

        // når du trykker på markørern så vil det komme opp
        // en toast av hva brukeren har trykket på (dette er ikke nødvendig men kan ha det)
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String markerName = marker.getTitle();
                Toast.makeText(Activity_Maps.this, "Clicked location is " + markerName, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        }
    }

    // metoden tar urlen om jsonin og executer den
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private void postJSON(String url){

        getJSON post = new getJSON();
        post.execute(url);

    }
}