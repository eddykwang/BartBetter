package com.eddystudio.bartbetter.ViewModel;


import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

public class NotificationViewModel {
    public enum ClickedItemType {
        MAP, ABOUT
    }

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

    private OnOtherItemClickListener itemClickListener;

    public void onMapClicked() {
        itemClickListener.onItemClick(ClickedItemType.MAP);
    }

    public void onAboutClicked() {
        itemClickListener.onItemClick(ClickedItemType.ABOUT);
    }

    public void setItemClickListener(OnOtherItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }



    public interface OnOtherItemClickListener {
        void onItemClick(ClickedItemType type);
    }
}
