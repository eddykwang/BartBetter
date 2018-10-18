package com.eddystudio.bartbetter.DI;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RetrofitModule {

  private final String baseUrl;
  private static final int HTTP_TIMEOUT = 60;

  public RetrofitModule(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @Provides
  @Singleton
  public SharedPreferences provideSharedPreferences(Application application) {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }

  @Provides
  @Singleton
  Cache provideOkHttpCache(Application application) {
    int cacheSize = 10 * 1024 * 1024; // 10 MiB
    Cache cache = new Cache(application.getCacheDir(), cacheSize);
    return cache;
  }

  @Provides
  @Singleton
  OkHttpClient provideOkHttpClient(Cache cache) {
    OkHttpClient.Builder client = new OkHttpClient.Builder();
    client.cache(cache);
    return client
        .connectTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
        .build();
  }

  @Provides
  public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
    Gson gson = new GsonBuilder()
        .setLenient()
        .create();

    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
  }
}
