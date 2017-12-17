package com.example.m712658.bartable.HomePage;


import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.util.Log;

public class MainRecyclerViewModel {
    public final ObservableField<String> destination = new ObservableField<>("");
    public final ObservableField<String> firstTrain = new ObservableField<>("");
    public final ObservableField<String> secondTrain = new ObservableField<>("");
    public final ObservableField<String> thirdTrain = new ObservableField<>("");
    public final ObservableInt routColor = new ObservableInt(1);

    public MainRecyclerViewModel(String local, String firstTrain, String secondTrain, String thirdTrain, int routColor) {
        this.destination.set(local);
        this.firstTrain.set(firstTrain);
        this.secondTrain.set(secondTrain);
        this.thirdTrain.set(thirdTrain);
        this.routColor.set(routColor);
    }

    public MainRecyclerViewModel() {

    }
}
