package com.artk.gallery.ui;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.artk.gallery.R;

public class GalleryActivity extends AppCompatActivity {

    private ViewPager viewPager;

    public static final int SPAN_PORT = 3, SPAN_LAND = 5; // number of columns
    public static int spanCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        spanCount = (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) ? SPAN_PORT : SPAN_LAND;

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
