package com.krant.daniil.pet.gpxrallyparser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.krant.daniil.pet.gpxrallyparser.databinding.ActivityMainBinding;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.SectionsPagerAdapter;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.list.ListItemClicked;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.list.ListViewItemHolder;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.map.RouteFollowingListener;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.map.ZoomToMarker;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private ActivityMainBinding mBinding;
    private ViewPager mViewPager;
    private TabLayout mTabs;
    private FrameLayout mOpenFileHintLayout;
    private FloatingActionButton mOpenFileFab;
    private FloatingActionButton mStartLocationTrackFab;
    private RequestShowMarkerOnMap mShowMapGoToMarker;
    private static ZoomToMarker mZoomToMarker;
    private static HashSet<RouteFollowingListener> mRouteFollowingListeners;
    private boolean mIsRedrawActivityNeeded = true;
    private LocationManager mLocationManager;
    private boolean mVoiceFollowingEnabled = false;

    private static final int PICKFILE_RESULT_CODE = 42;
    private static final long LOCATION_UPDATE_TIMEOUT_MS = 4000;
    private final static int LOCATION_REQUEST = 4242;
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRouteFollowingListeners = new HashSet<>();
        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
        mShowMapGoToMarker = new RequestShowMarkerOnMap();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GPXDataRoutine.setContext(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsRedrawActivityNeeded) {
            mBinding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(mBinding.getRoot());

            mOpenFileFab = mBinding.fab;
            mViewPager = mBinding.viewPager;
            mTabs = mBinding.tabs;
            mOpenFileHintLayout = mBinding.openHintLayout;
            mStartLocationTrackFab = mBinding.startLocationFab;
            Button chooseFileButton = mBinding.chooseFileButton;
            TextView authorText = mBinding.author;

            chooseFileButton.setOnClickListener(new OpenFileClickListener());
            mOpenFileFab.setOnClickListener(new OpenFileClickListener());
            authorText.setOnClickListener(view -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.linkedin.com/in/daniilkrant/"));
                startActivity(browserIntent);
            });
            mStartLocationTrackFab = findViewById(R.id.start_location_fab);
            mStartLocationTrackFab.setOnClickListener(view -> {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                mVoiceFollowingEnabled = !mVoiceFollowingEnabled;
                for (RouteFollowingListener listener : mRouteFollowingListeners) {
                    if (mVoiceFollowingEnabled) {
                        mStartLocationTrackFab.setImageResource(android.R.drawable.ic_media_pause);
                        listener.startVoiceFollowing();
                    } else {
                        mStartLocationTrackFab.setImageResource(android.R.drawable.ic_media_play);
                        listener.stopVoiceFollowing();
                    }
                }
            });
            mIsRedrawActivityNeeded = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mOpenFileHintLayout.setVisibility(View.GONE);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
            Uri filePath = data.getData();
            new ParseTask(filePath).execute();
        } else {
            mIsRedrawActivityNeeded = true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_REQUEST) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_UPDATE_TIMEOUT_MS, 0, this);
                }
            }
        }
    }

    private boolean parseFile(Uri fileUri) {
        InputStream fileInputStream;
        try {
            fileInputStream = getApplicationContext().getContentResolver().openInputStream(fileUri);
            if (GPXDataRoutine.getInstance().parseGpx(fileInputStream)) {
                return true;
            }
        } catch (FileNotFoundException e) {
            showError(getApplicationContext().getString(R.string.file_not_found));
            e.printStackTrace();
        }
        return false;
    }

    private void showError(String error) {
        Snackbar.make(mBinding.viewPager, error,
                Snackbar.LENGTH_LONG).show();
        mIsRedrawActivityNeeded = true;
        onResume();
    }

    private void showUI() {
        mOpenFileHintLayout.setVisibility(View.GONE);
        mOpenFileFab.setVisibility(View.VISIBLE);
        mStartLocationTrackFab.setVisibility(View.VISIBLE);
    }

    public static void setZoomToMarker(ZoomToMarker zoomToMarker) {
        mZoomToMarker = zoomToMarker;
    }

    public static void addLocationChangedListener(RouteFollowingListener routeFollowingListener) {
        mRouteFollowingListeners.add(routeFollowingListener);
    }

    public static void removeLocationChangedListener(RouteFollowingListener routeFollowingListener) {
        mRouteFollowingListeners.remove(routeFollowingListener);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        for (RouteFollowingListener listener : mRouteFollowingListeners) {
            listener.onLocationObtained(location);
        }
    }


    class OpenFileClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        }
    }

    class ParseTask extends AsyncTask<Void, Void, Boolean> {
        Uri mFilePath;
        ProgressDialog mProgress;
        SectionsPagerAdapter mSectionsPagerAdapter;

        public ParseTask(Uri filePath) {
            mFilePath = filePath;
            mProgress = new ProgressDialog(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setTitle(getApplicationContext().getString(R.string.loading_title));
            mProgress.setMessage(getApplicationContext().getString(R.string.loading_text));
            mProgress.setCancelable(false);
            mProgress.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean res = parseFile(mFilePath);
            if (res) {
                mSectionsPagerAdapter = new SectionsPagerAdapter(
                        MainActivity.this, getSupportFragmentManager());
            }
            return res;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mTabs.setupWithViewPager(mViewPager);
                ListViewItemHolder.setListItemClicked(mShowMapGoToMarker);
                showUI();
            } else {
                showError(getApplicationContext().getString(R.string.file_not_parsed));
            }
            mProgress.cancel();
        }
    }

    class RequestShowMarkerOnMap implements ListItemClicked {

        @Override
        public void itemClicked(int position) {
            TabLayout.Tab mapTab = mTabs.getTabAt(1);
            mTabs.selectTab(mapTab);
            if (mZoomToMarker != null) {
                mZoomToMarker.zoomToMarker(position);
            }
        }
    }


}