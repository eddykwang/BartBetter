package com.eddystudio.bartbetter.DI;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppContextModule {
    Application application;
    AppContextModule(Application application){
        this.application = application;
    }

    @Provides
    @Singleton
    public Application application(){
        return application;
    }

}
