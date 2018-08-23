package com.eddystudio.bartbetter.UI;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.eddystudio.bartbetter.BuildConfig;
import com.eddystudio.bartbetter.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


public class AboutActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getSupportActionBar().setTitle("About");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    View aboutPage = new AboutPage(this)
        .setDescription(getString(R.string.aboutpage_description))
        .isRTL(false)
        .setImage(R.mipmap.ic_launcher)
        .addItem(new Element().setTitle("Version" + BuildConfig.VERSION_NAME))
        .addItem(new Element().setTitle("Privacy Policy").setIconDrawable(R.drawable.about_icon_link).setOnClickListener((view -> {
          openUrl("https://eddykwang.github.io/eddystudio/ppbartbetter.html");
        })))
        .addGroup("Connect With Us")
        .addEmail("eddy.studio.dev@gmail.com")
        .addPlayStore("com.eddystudio.bartbetter")
        .addWebsite("https://github.com/eddykwang/BartBetter")
        .addGitHub("/eddykwang/BartBetter")
        .addGroup("Credit To")
        .addItem(new Element().setTitle("RxJava & RxAndroid").setIconDrawable(R.drawable.about_icon_github).setOnClickListener((view -> {
          openUrl("https://github.com/ReactiveX/RxJava");
        })))
        .addItem(new Element().setTitle("Retrofit").setIconDrawable(R.drawable.about_icon_github).setOnClickListener((view -> {
          openUrl("https://github.com/square/retrofit");
        })))
        .addItem(new Element().setTitle("Dagger2").setIconDrawable(R.drawable.about_icon_github).setOnClickListener((view -> {
          openUrl("https://github.com/google/dagger");
        })))
        .addItem(new Element().setTitle("Android Indefinite Pager Indicator").setIconDrawable(R.drawable.about_icon_github).setOnClickListener((view -> {
          openUrl("https://github.com/rbro112/Android-Indefinite-Pager-Indicator");
        })))
        .addItem(new Element().setTitle("Searchable Spinner").setIconDrawable(R.drawable.about_icon_github).setOnClickListener((view -> {
          openUrl("https://github.com/miteshpithadiya/SearchableSpinner");
        })))
        .addItem(new Element().setTitle("PhotoView").setIconDrawable(R.drawable.about_icon_github).setOnClickListener((view -> {
          openUrl("https://github.com/chrisbanes/PhotoView");
        })))
        .addItem(new Element().setTitle("Android About Page").setIconDrawable(R.drawable.about_icon_github).setOnClickListener((view -> {
          openUrl("https://github.com/medyo/android-about-page");
        })))
        .create();

    setContentView(aboutPage);
  }

  private void openUrl(String url) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    startActivity(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
