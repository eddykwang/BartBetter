package com.example.m712658.bartable.application;

import android.content.Context;

import dagger.Component;

@Component(modules = {AppContextModule.class, RetrofitModule.class})
@AppScope
public interface AppComponent {
    Context injectAppContext();
    Context injectActivityContext();
    //Retrofit inject();
}
