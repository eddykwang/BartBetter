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
    private Etd etd;
    private String from = "";
    private String to = "";
    private ItemClickListener itemClickListener;

    public DashboardRecyclerViewItemModel(Etd etd, String from, String to) {
        this.etd = etd;
        this.from = from;
        this.to = to;
        updateUi();
    }

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
        itemClickListener.onItemClicked(from, destination.get(), routeColor.get(), view);
    }

    private void updateUi(List<Trip> trips, String origin, String dest) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        fromStation.set(Uilt.getFullStationName(origin));
        destination.set(Uilt.getFullStationName(dest));
        routeColor.set(Uilt.routeColorMatcher(trips.get(0).getLeg().get(0).getLine()));
        if (trips.get(0).getLeg().size() > 1){
            routeColor2.set(Uilt.routeColorMatcher(trips.get(0).getLeg().get(1).getLine()));
        }else {
            routeColor2.set(routeColor.get());
        }

        firstTrain.set(Uilt.timeMinutesCalculator(trips.get(0).getLeg().get(0).getOrigTimeMin()));
        if (trips.get(1) != null){
            secondTrain.set(Uilt.timeMinutesCalculator(trips.get(1).getLeg().get(0).getOrigTimeMin()));
            if (trips.get(2) != null){
                thirdTrain.set(Uilt.timeMinutesCalculator(trips.get(2).getLeg().get(0).getOrigTimeMin()));
            }
        }
    }

    private void updateUi() {

        fromStation.set(Uilt.getFullStationName(from));
        destination.set(Uilt.getFullStationName(to));
        routeColor.set(Color.GRAY);
        routeColor2.set(Color.GRAY);

        if (etd.getEstimate() != null) {
            String m = " minutes";
            String first = "";
            String second = "";
            String third = "";
            if (etd.getEstimate().size() == 1) {
                first = etd.getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving"
                        : etd.getEstimate().get(0).getMinutes() + m;
            } else if (etd.getEstimate().size() == 2) {
                first = etd.getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving"
                        : etd.getEstimate().get(0).getMinutes() + m;
                second = etd.getEstimate().get(1).getMinutes() + m;
            } else {
                first = etd.getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving"
                        : etd.getEstimate().get(0).getMinutes() + m;
                second = etd.getEstimate().get(1).getMinutes() + m;
                third = etd.getEstimate().get(2).getMinutes() + m;
            }

            firstTrain.set(first);
            secondTrain.set(second);
            thirdTrain.set(third);
            fromStation.set(Uilt.getFullStationName(from));
            destination.set(Uilt.getFullStationName(to));
            routeColor.set(Uilt.materialColorConverter(etd.getEstimate().get(0).getColor()));
            routeColor2.set(Uilt.materialColorConverter(etd.getEstimate().get(0).getColor()));
        }
    }
}
