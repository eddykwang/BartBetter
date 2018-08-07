package com.eddystudio.bartbetter.DI;


import android.util.Log;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

public class Application extends android.app.Application {

  private static AppComponent appComponet;

  @Override
  public void onCreate() {
    super.onCreate();
    appComponet = DaggerAppComponent
        .builder()
        .appContextModule(new AppContextModule(this))
        .retrofitModule(new RetrofitModule("http://api.bart.gov/"))
        .build();

    Twitter.initialize(this);
    TwitterConfig config = new TwitterConfig.Builder(this)
        .logger(new DefaultLogger(Log.DEBUG))
        .twitterAuthConfig(new TwitterAuthConfig("CONSUMER_KEY", "CONSUMER_SECRET"))
        .debug(true)
        .build();
    Twitter.initialize(config);
  }

  public static AppComponent getAppComponet() {
    return appComponet;
  }
}
