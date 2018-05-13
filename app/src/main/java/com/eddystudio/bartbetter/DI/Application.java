package com.eddystudio.bartbetter.DI;


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
    }

    public static AppComponent getAppComponet(){
        return appComponet;
    }
}
