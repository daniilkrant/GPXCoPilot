package com.krant.daniil.pet.gpxrallyparser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.krant.daniil.pet.gpxrallyparser.databinding.ActivityMainBinding;
import com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.SectionsPagerAdapter;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPager mViewPager;
    private TabLayout mTabs;
    private FrameLayout mOpenFileHintLayout;
    private FloatingActionButton mFab;
    private AppBarLayout mAppBarLayout;
    private static final int PICKFILE_RESULT_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GPXDataRoutine.setContext(getApplicationContext());
        mFab = binding.fab;
        mViewPager = binding.viewPager;
        mTabs = binding.tabs;
        mOpenFileHintLayout = binding.openHintLayout;
        mAppBarLayout = binding.appBarLayout;
        Button chooseFileButton = binding.chooseFileButton;
        TextView authorText = binding.author;

        mOpenFileHintLayout.setOnClickListener(new OpenFileClickListener());
        chooseFileButton.setOnClickListener(new OpenFileClickListener());
        mFab.setOnClickListener(new OpenFileClickListener());
        authorText.setOnClickListener(view -> {
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");

            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"danbiil988@gmail.com"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GPX4Rally");
            startActivity(Intent.createChooser(emailIntent, "Contact me"));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mOpenFileHintLayout.setVisibility(View.GONE);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
            Uri filePath = data.getData();
            parseFile(filePath);
        }
    }

    private void parseFile(Uri fileUri) {
        InputStream fileInputStream;
        try {
            fileInputStream = getApplicationContext().getContentResolver().openInputStream(fileUri);
            if (GPXDataRoutine.getInstance().parseGpx(fileInputStream)) {
                startUI();
            } else {
                showError(getApplicationContext().getString(R.string.file_not_parsed));
            }
        } catch (FileNotFoundException e) {
            showError(getApplicationContext().getString(R.string.file_not_found));
            e.printStackTrace();
        }
    }

    private void showError(String error) {
        Snackbar.make(binding.viewPager, error,
                Snackbar.LENGTH_LONG).show();
    }

    private void startUI() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
                this, getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPagerAdapter);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
        mFab.setVisibility(View.VISIBLE);
        mAppBarLayout.setVisibility(View.VISIBLE);
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

}