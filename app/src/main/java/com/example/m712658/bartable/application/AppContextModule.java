package com.example.m712658.bartable.application;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class AppContextModule {
    Context context;
    AppContextModule(Context context){
        this.context = context;
    }

    @Provides
    public Context context(){
        return context;
    }

}
