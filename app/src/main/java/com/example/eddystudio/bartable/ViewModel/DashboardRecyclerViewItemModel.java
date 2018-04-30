package com.example.eddystudio.bartable.ViewModel;


import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;

import android.util.Log;
import android.view.View;
import com.example.eddystudio.bartable.Model.Uilt;
import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Etd;

public class DashboardRecyclerViewItemModel {
    public final ObservableField<String> destination = new ObservableField<>("");
    public final ObservableField<String> fromStation = new ObservableField<>("");
    public final ObservableField<String> firstTrain = new ObservableField<>("");
    public final ObservableField<String> secondTrain = new ObservableField<>("");
    public final ObservableField<String> thirdTrain = new ObservableField<>("");
    public final ObservableInt routColor = new ObservableInt(Color.GRAY);
    private  Etd etd;
    private  String from = "";
    private  String to = "";
    private ItemClickListener itemClickListener;

    public DashboardRecyclerViewItemModel(Etd etd, String from, String to) {
        this.etd = etd;
        this.from = from;
        this.to = to;
        updateUi();
    }

    public void setItemClickListener(
        ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void onItemClicked(View view){
        itemClickListener.onItemClicked(from, destination.get(), routColor.get(), view);
    }

    private void updateUi(){

        fromStation.set(Uilt.getFullStationName(from));
        destination.set(Uilt.getFullStationName(to));
        routColor.set(Color.GRAY);

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
            routColor.set(Uilt.materialColorConverter(etd.getEstimate().get(0).getColor()));
        }
    }
}
