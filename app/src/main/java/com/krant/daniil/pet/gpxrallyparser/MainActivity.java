package com.krant.daniil.pet.gpxrallyparser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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
    private static final int PICKFILE_RESULT_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GPXDataRoutine.setContext(getApplicationContext());
        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(view -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
    }
}