package com.example.eddystudio.bartable.HomePage;

import android.databinding.ObservableInt;
import android.util.Log;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by Eddy-Desktop on 12/17/2017.
 */

public class MainViewModel {
    public final ObservableInt clickedPos = new ObservableInt(0);

    public final Subject<Integer> clicked = PublishSubject.create();
}
