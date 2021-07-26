package com.krant.daniil.pet.gpxrallyparser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

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
    private RelativeLayout mOpenFileHintLayout;
    private static final int PICKFILE_RESULT_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GPXDataRoutine.setContext(getApplicationContext());
        FloatingActionButton fab = binding.fab;
        mViewPager = binding.viewPager;
        mTabs = binding.tabs;
        mOpenFileHintLayout = binding.openHintLayout;

        mOpenFileHintLayout.setOnClickListener(new OpenFileClickListener());
        fab.setOnClickListener(new OpenFileClickListener());
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
            GPXDataRoutine.getInstance().parseGpx(fileInputStream);
            startUI();
        } catch (FileNotFoundException e) {
            Snackbar.make(binding.viewPager, getApplicationContext().getString(R.string.file_not_found),
                    Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void startUI() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
                this, getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPagerAdapter);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
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