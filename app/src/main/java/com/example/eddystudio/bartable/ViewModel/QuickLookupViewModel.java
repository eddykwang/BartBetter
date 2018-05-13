package com.example.eddystudio.bartable.ViewModel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.util.Log;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class QuickLookupViewModel {
    public final ObservableBoolean showSpinnerProgess = new ObservableBoolean(false);
}
