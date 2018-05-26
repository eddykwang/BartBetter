package com.eddystudio.bartbetter.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.eddystudio.bartbetter.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("About");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Element adsElement = new Element();
//        adsElement.setTitle("Advertise with us");

        View aboutPage = new AboutPage(this)
                .setDescription(getString(R.string.aboutpage_description))
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher_round)
                .addItem(new Element().setTitle("Version 1.0.1b"))
//                .addItem(adsElement)
                .addGroup("Connect with us")
                .addEmail("eddy.studio.dev@gmail.com")
                .addWebsite("https://github.com/eddykwang/BartBetter")
//                .addFacebook("the.medy")
//                .addTwitter("medyo80")
//                .addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
                .addPlayStore("com.eddystudio.bartbetter")
//                .addInstagram("medyo80")
                .addGitHub("/eddykwang/BartBetter")
                .create();

        setContentView(aboutPage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
