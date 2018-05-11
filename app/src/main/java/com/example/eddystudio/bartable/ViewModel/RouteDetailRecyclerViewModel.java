package com.example.eddystudio.bartable.ViewModel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;

import com.example.eddystudio.bartable.Model.Response.Schedule.Fare;
import com.example.eddystudio.bartable.Model.Response.Schedule.Leg;
import com.example.eddystudio.bartable.Model.Response.Schedule.ScheduleFromAToB;
import com.example.eddystudio.bartable.Model.Response.Schedule.Trip;
import com.example.eddystudio.bartable.Model.Uilt;

import java.util.ArrayList;
import java.util.List;

public class RouteDetailRecyclerViewModel {
    public ObservableField<String> firstRouteOriginStation = new ObservableField<>("");
    public ObservableField<String> firstRouteOriginDepartingTime = new ObservableField<>("");
    public ObservableField<String> firstRouteDestinationTrain = new ObservableField<>("");
    public ObservableField<String> firstRouteDestinationStation = new ObservableField<>("");
    public ObservableField<String> firstRouteArravingTime = new ObservableField<>("");
    public ObservableInt firstRouteColor = new ObservableInt(Color.GRAY);
    public ObservableInt secondRouteColor = new ObservableInt(Color.GRAY);
    public ObservableField<String> secondRouteOriginStation = new ObservableField<>("");
    public ObservableField<String> secondRouteOriginDepartingTime = new ObservableField<>("");
    public ObservableField<String> secondRouteDestinationTrain = new ObservableField<>("");
    public ObservableField<String> secondRouteDestinationStation = new ObservableField<>("");
    public ObservableField<String> secondRouteArrayingTime = new ObservableField<>("");
    public ObservableField<String> clipperPrice = new ObservableField<>("");
    public ObservableField<String> cashPrice = new ObservableField<>("");
    public ObservableField<String> seniorPrice = new ObservableField<>("");
    public ObservableField<String> youthPrice = new ObservableField<>("");
    public ObservableBoolean haveSecondRoute = new ObservableBoolean(false);

    private final Trip trip;

    public RouteDetailRecyclerViewModel(Trip trip){
        this.trip = trip;
        updateUi();
    }

    private void updateUi(){
        updateFares();
        updateRoutes();
    }

    private void updateFares(){
        if (trip.getFares() != null) {
            for (Fare fare : trip.getFares().getFare()) {
                switch (fare.getClass_()) {
                    case "clipper":
                        clipperPrice.set("$ " + fare.getAmount());
                        break;
                    case "cash":
                        cashPrice.set("$ " + fare.getAmount());
                        break;
                    case "rtcclipper":
                        seniorPrice.set("$ " + fare.getAmount());
                        break;
                    case "student":
                        youthPrice.set("$ " + fare.getAmount());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void updateRoutes() {
        if (trip.getLeg() != null) {
            List<Leg> legs = trip.getLeg();

            Leg firstRouteInfo = legs.get(0);
            firstRouteOriginStation.set(Uilt.getFullStationName(firstRouteInfo.getOrigin()));
            firstRouteOriginDepartingTime.set(firstRouteInfo.getOrigTimeMin());
            firstRouteDestinationTrain.set(Uilt.getFullStationName(firstRouteInfo.getTrainHeadStation()));
            firstRouteDestinationStation.set(Uilt.getFullStationName(firstRouteInfo.getDestination()));
            firstRouteArravingTime.set(firstRouteInfo.getDestTimeMin());
            firstRouteColor.set(Uilt.routeColorMatcher(firstRouteInfo.getLine()));

            if (legs.size() > 1) {
                haveSecondRoute.set(true);
                Leg secondRouteInfo = legs.get(1);
                secondRouteOriginStation.set(Uilt.getFullStationName(secondRouteInfo.getOrigin()));
                secondRouteOriginDepartingTime.set(secondRouteInfo.getOrigTimeMin());
                secondRouteDestinationTrain.set(Uilt.getFullStationName(secondRouteInfo.getTrainHeadStation()));
                secondRouteDestinationStation.set(Uilt.getFullStationName(secondRouteInfo.getDestination()));
                secondRouteArrayingTime.set(secondRouteInfo.getDestTimeMin());
                secondRouteColor.set(Uilt.routeColorMatcher(secondRouteInfo.getLine()));
            }
        }
    }
}
