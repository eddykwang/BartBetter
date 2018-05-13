package com.example.eddystudio.bartable.ViewModel;


import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;

import android.view.View;

import com.example.eddystudio.bartable.Model.Response.Schedule.Trip;
import com.example.eddystudio.bartable.Model.Uilt;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Etd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardRecyclerViewItemModel {
    public final ObservableField<String> destination = new ObservableField<>("");
    public final ObservableField<String> fromStation = new ObservableField<>("");
    public final ObservableField<String> firstTrain = new ObservableField<>("");
    public final ObservableField<String> secondTrain = new ObservableField<>("");
    public final ObservableField<String> thirdTrain = new ObservableField<>("");
    public final ObservableInt routeColor = new ObservableInt(Color.GRAY);
    public final ObservableInt routeColor2 = new ObservableInt(Color.GRAY);
    private String from = "";
    private String to = "";
    private ItemClickListener itemClickListener;

    public DashboardRecyclerViewItemModel(List<Trip> trips, String from, String to) {
        this.from = from;
        this.to = to;
        try {
            updateUi(trips, from, to);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setItemClickListener(
            ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void onItemClicked(View view) {
        itemClickListener.onItemClicked(from, to, routeColor.get(), view);
    }

    private void updateUi(List<Trip> trips, String origin, String dest) throws ParseException {
        fromStation.set(Uilt.getFullStationName(origin));
        destination.set(Uilt.getFullStationName(dest));
        if (trips.size() > 0) {
            routeColor.set(Uilt.routeColorMatcher(trips.get(0).getLeg().get(0).getLine()));
            if (trips.get(0).getLeg().size() > 1) {
                routeColor2.set(Uilt.routeColorMatcher(trips.get(0).getLeg().get(1).getLine()));
            } else {
                routeColor2.set(routeColor.get());
            }

            firstTrain.set(Uilt.timeMinutesCalculator(trips.get(0).getLeg().get(0).getOrigTimeDate() + " " +
                    trips.get(0).getLeg().get(0).getOrigTimeMin()));
            if (trips.get(1) != null) {
                secondTrain.set(Uilt.timeMinutesCalculator(trips.get(1).getLeg().get(0).getOrigTimeDate() + " " +
                        trips.get(1).getLeg().get(0).getOrigTimeMin()));
                if (trips.get(2) != null) {
                    thirdTrain.set(Uilt.timeMinutesCalculator(trips.get(2).getLeg().get(0).getOrigTimeDate() + " " +
                            trips.get(2).getLeg().get(0).getOrigTimeMin()));
                }
            }
        }
    }
}
