package com.artk.gallery;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;

    static List<Picture> data = new ArrayList<>();
    static List<Picture> favorites = new ArrayList<>();
    static final String FAV_FILE = "favorites";
    static int spanCount, screenWidth;

    static final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        spanCount = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? 3 : 5;

        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.container);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        showGreeting();

    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainFragment(), getString(R.string.txt_all_pictures));
        adapter.addFragment(new FavoritesFragment(), getString(R.string.txt_favorites));
        viewPager.setAdapter(adapter);
    }

    private void showGreeting(){
        final String PREFS_FILE = "prefs";
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        boolean firstLaunch = prefs.getBoolean("firstLaunch", true);
        if (firstLaunch) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.dialogText));
            builder.setNeutralButton("OK", null);
            final AlertDialog dialog = builder.create();
            dialog.show();

            SharedPreferences.Editor editor = getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit();
            editor.putBoolean("firstLaunch", false);
            editor.apply();
        }
    }

}
