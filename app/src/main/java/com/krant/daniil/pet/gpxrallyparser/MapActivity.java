package com.krant.daniil.pet.gpxrallyparser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        TextToSpeech.OnInitListener, GoogleMap.OnMarkerClickListener {

    private final static String TITLE_ID_DELIM = ": ";

    private GoogleMap mMap;
    GPXDataRoutine mGpxParser;
    TextToSpeech mTextToSpeech;
    LexicalProcessor mLexicalProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mGpxParser = new GPXDataRoutine(getApplicationContext());
        mLexicalProcessor = new LexicalProcessor(getApplicationContext());
        mTextToSpeech = new TextToSpeech(this, this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mGpxParser.parseGpx();
            ArrayList<RallyPoint> rallyPoints = new ArrayList<>(mGpxParser.getRallyPoints());
            addMarkersToMap(rallyPoints);

            mMap.setOnMarkerClickListener(this);
            mMap.moveCamera(CameraUpdateFactory.
                    newLatLngZoom(new LatLng(rallyPoints.get(0).getLatitude(),
                    rallyPoints.get(0).getLongitude()), 20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            }
        } else {
            Log.e("error", "Failed to Initialize");
        }
    }

    public void textToSpeech(String text) {
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        textToSpeech(removeIDFromMarkerTitle(marker.getTitle()));
        return false;
    }

    private String addIdToMarkerTitle(String title, int id) {
        return id + TITLE_ID_DELIM + title;
    }

    private String removeIDFromMarkerTitle(String title) {
        return title.substring(title.indexOf(TITLE_ID_DELIM) + TITLE_ID_DELIM.length());
    }

    private void addMarkersToMap(ArrayList<RallyPoint> rallyPoints) {
        for (RallyPoint rp: rallyPoints) {
            LatLng point = new LatLng(rp.getLatitude(), rp.getLongitude());
            String title = addIdToMarkerTitle(mLexicalProcessor.getHint(rp), rp.getId());
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(title));
        }
    }
}