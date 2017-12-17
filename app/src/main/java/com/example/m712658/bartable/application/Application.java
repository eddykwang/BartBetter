package com.example.m712658.bartable.application;



public class Application extends android.app.Application {

    public static AppComponent appComponet;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponet = DaggerAppComponent
                .builder()
                .appContextModule(new AppContextModule(getApplicationContext()))
                .build();
    }
}
