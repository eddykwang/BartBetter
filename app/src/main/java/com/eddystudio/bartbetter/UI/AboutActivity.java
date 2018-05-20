package com.eddystudio.bartbetter.UI;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.eddystudio.bartbetter.R;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.about_toolbar_layout);
//        collapsingToolbarLayout.setEnabled(true);
//        collapsingToolbarLayout.setTitle("sd");
        setContentView(R.layout.activity_about);
    }
}
