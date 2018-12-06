package com.eddystudio.bartbetter.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eddystudio.bartbetter.R;
import com.github.chrisbanes.photoview.PhotoView;

public class MapActivity extends AppCompatActivity {

  private PhotoView mapView;
  private static String bartMapUrl = "https://www.bart.gov/sites/all/themes/bart_desktop/img/system-map.gif";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setTitle("Map");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.activity_map);
    mapView = findViewById(R.id.bartMapView);
    Glide.with(this).load(bartMapUrl).thumbnail(Glide.with(this).load(R.drawable.loading).apply(new RequestOptions().fitCenter())).into(mapView);
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
