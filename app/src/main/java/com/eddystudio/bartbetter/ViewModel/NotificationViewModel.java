package com.eddystudio.bartbetter.ViewModel;


import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

public class NotificationViewModel {
    public final ObservableField<String> delayDescription = new ObservableField<>("");
    public final ObservableBoolean isViewDetailChecked = new ObservableBoolean(false);
    public final ObservableField<String> reportTime = new ObservableField<>("");
    public final ObservableField<String> reportDate = new ObservableField<>("");
    public final ObservableField<String> reportStation = new ObservableField<>("");
    public final ObservableBoolean isDelayReportProgressVisible = new ObservableBoolean(false);
    public final ObservableBoolean isElevatorProgressVisible = new ObservableBoolean(false);
    public final ObservableField<String> elevatorStatus = new ObservableField<>("");
    public final ObservableField<String> elevatorStation = new ObservableField<>("");
    public final ObservableBoolean isNotDelay = new ObservableBoolean(true);
    public final ObservableBoolean isElevaorWorking = new ObservableBoolean(true);
}
