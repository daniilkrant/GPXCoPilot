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
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.krant.daniil.pet.gpxrallyparser.databinding.ActivityMainBinding;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.SectionsPagerAdapter;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.list.ListItemClicked;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.list.ListViewFragment;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.list.ListViewItemHolder;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.map.MapViewFragment;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.RouteFollowingListener;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.map.ZoomToMarker;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private ActivityMainBinding mBinding;
    private ViewPager mViewPager;
    private TabLayout mTabs;
    private FrameLayout mOpenFileHintLayout;
    private FloatingActionButton mOpenFileFab;
    private FloatingActionButton mStartLocationTrackFab;
    private FloatingActionButton mEnableDisableSoundFab;
    private FloatingActionButton mOpenFilterFab;
    private Slider mFilterSlider;
    private CardView mSliderLayout;
    private RequestShowMarkerOnMap mShowMapGoToMarker;
    private static ZoomToMarker mZoomToMarker;
    private static HashMap<String, RouteFollowingListener> mRouteFollowingListeners;
    private boolean mIsRedrawActivityNeeded = true;
    private LocationManager mLocationManager;
    private boolean mFollowingEnabled = false;
    private boolean mVoiceEnabled = false;
    private boolean mFileOpened = false;
    private boolean mFilterOpened = false;

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

        mRouteFollowingListeners = new HashMap<>();
        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
        mShowMapGoToMarker = new RequestShowMarkerOnMap();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LexicalProcessor lexicalProcessor = new LexicalProcessor(getApplicationContext());
        GPXDataRoutine.getInstance().setLexicalProcessor(lexicalProcessor);
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
            mEnableDisableSoundFab = mBinding.enableSoundFab;
            mOpenFilterFab = mBinding.filterFab;
            mFilterSlider = mBinding.filterSlider;
            mSliderLayout = mBinding.sliderLayout;
            Button chooseFileButton = mBinding.chooseFileButton;
            TextView authorText = mBinding.author;

            TooltipCompat.setTooltipText(mOpenFileFab, getString(R.string.open_file_fab_tooltip));
            TooltipCompat.setTooltipText(mStartLocationTrackFab,getString(R.string.enable_location_fab_tooltip));
            TooltipCompat.setTooltipText(mEnableDisableSoundFab,getString(R.string.enable_sound_fab_tooltip));
            TooltipCompat.setTooltipText(mOpenFilterFab,getString(R.string.filter_fab_tooltip));

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
                mFollowingEnabled = !mFollowingEnabled;
                try {
                    if (mFollowingEnabled) {
                        mStartLocationTrackFab.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        mStartLocationTrackFab.setImageResource(android.R.drawable.ic_media_play);
                    }
                    notifyListenerFollowStatusChange(mFollowingEnabled);
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            });

            mEnableDisableSoundFab.setOnClickListener(view1 -> {
                mVoiceEnabled = !mVoiceEnabled;
                try {
                    if (mVoiceEnabled) {
                        mEnableDisableSoundFab.setImageResource(android.R.drawable.ic_lock_silent_mode);
                    } else {
                        mEnableDisableSoundFab.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    }
                    notifyListenerVoiceStatusChange(mVoiceEnabled);
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            });

            mOpenFilterFab.setOnClickListener(view -> {
                mFilterOpened = !mFilterOpened;
                if (mFilterOpened) {
                    mSliderLayout.setVisibility(View.VISIBLE);
                } else {
                    mSliderLayout.setVisibility(View.GONE);
                }

            });

            mFilterSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull @NotNull Slider slider) {

                }

                @Override
                public void onStopTrackingTouch(@NonNull @NotNull Slider slider) {
                    GPXDataRoutine.getInstance().
                            updateListenersWithFilteredRallyPoints((int) slider.getValue());
                    Log.e("Log", slider.getValue() + "");
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
        mEnableDisableSoundFab.setVisibility(View.VISIBLE);
        mOpenFilterFab.setVisibility(View.VISIBLE);
        mFileOpened = true;
    }

    public static void setZoomToMarker(ZoomToMarker zoomToMarker) {
        mZoomToMarker = zoomToMarker;
    }

    public static void addLocationChangedListener(RouteFollowingListener routeFollowingListener) {
        mRouteFollowingListeners.put(routeFollowingListener.getClass().getSimpleName(),
                routeFollowingListener);
    }

    public static void removeLocationChangedListener(RouteFollowingListener routeFollowingListener) {
        mRouteFollowingListeners.remove(routeFollowingListener.getClass().getSimpleName());
    }

    private RouteFollowingListener getCurrentRouteFollowerListener() {
        if (mTabs.getSelectedTabPosition() == 0) {
            return mRouteFollowingListeners.get(ListViewFragment.class.getSimpleName());
        } else return mRouteFollowingListeners.get(MapViewFragment.class.getSimpleName());
    }

    private void notifyListenerFollowStatusChange(boolean isStart) {
        for (String key : mRouteFollowingListeners.keySet()) {
            if (isStart) {
                mRouteFollowingListeners.get(key).startFollowing();
            } else {
                mRouteFollowingListeners.get(key).stopFollowing();
            }
        }
    }

    private void notifyListenerVoiceStatusChange(boolean isEnabled) {
        for (String key : mRouteFollowingListeners.keySet()) {
            if (isEnabled) {
                mRouteFollowingListeners.get(key).onSoundEnabled();
            } else {
                mRouteFollowingListeners.get(key).onSoundDisabled();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (mFileOpened) {
            if (getCurrentRouteFollowerListener() != null) {
                getCurrentRouteFollowerListener().onLocationObtained(location);
            }
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