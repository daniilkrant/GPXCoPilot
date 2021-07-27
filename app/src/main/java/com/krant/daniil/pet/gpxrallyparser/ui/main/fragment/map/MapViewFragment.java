package com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.krant.daniil.pet.gpxrallyparser.GPXDataRoutine;
import com.krant.daniil.pet.gpxrallyparser.MainActivity;
import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.SpeechProcessor;

import java.util.ArrayList;

public class MapViewFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, ZoomToMarker {

    private final static String TITLE_ID_DELIM = ": ";

    private GoogleMap mMap;
    private SpeechProcessor mSpeechProcessor;
    private View rootView;
    private ArrayList<RallyPoint> mRallyPoints;
    private ArrayList<Marker> mMarkers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mSpeechProcessor = new SpeechProcessor(getContext());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MainActivity.setZoomToMarker(this);
        ArrayList<RallyPoint> rallyPoints = new ArrayList<>();
        try {
            rallyPoints = new ArrayList<>(GPXDataRoutine.getInstance().getRallyPoints());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap = googleMap;
        mMap.setInfoWindowAdapter(new MarkerWindowAdapter(this, rallyPoints));
        addMarkersToMap(rallyPoints);

        mMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.
                newLatLngZoom(new LatLng(rallyPoints.get(0).getLatitude(),
                        rallyPoints.get(0).getLongitude()), 18));

    }

    public void textToSpeech(String text) {
        if (!mSpeechProcessor.textToSpeech(text)) {
            Snackbar.make(rootView, getContext().getString(R.string.tts_not_ready),
                    Snackbar.LENGTH_LONG).show();
        }
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
        mMap.clear();
        mRallyPoints = rallyPoints;
        for (int i = 0; i < rallyPoints.size(); i++) {
            RallyPoint rp = rallyPoints.get(i);
            LatLng point = new LatLng(rp.getLatitude(), rp.getLongitude());
            String title = addIdToMarkerTitle(rp.getHint(), rp.getId());
            BitmapDescriptor color;
            if (rp.isFirst()) {
                Bitmap imageBitmap= BitmapFactory.decodeResource(getResources(),
                        R.drawable.start_marker);
                Bitmap resized = Bitmap.createScaledBitmap(imageBitmap, 150, 150, true);

                color = BitmapDescriptorFactory.fromBitmap(resized);
            } else if (rp.isLast()) {
                Bitmap imageBitmap= BitmapFactory.decodeResource(getResources(),
                        R.drawable.finish_marker);
                Bitmap resized = Bitmap.createScaledBitmap(imageBitmap, 150, 150, true);

                color = BitmapDescriptorFactory.fromBitmap(resized);
            } else {
                color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(title)
                    .icon(color));
            marker.setTag(rp.getId());
            mMarkers.add(marker);
        }

    }

    @Override
    public void zoomToMarker(int number) {
        mMap.moveCamera(CameraUpdateFactory.
                newLatLngZoom(new LatLng(mRallyPoints.get(number).getLatitude(),
                        mRallyPoints.get(number).getLongitude()), 20));
        mMarkers.get(number).showInfoWindow();
    }
}