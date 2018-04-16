package com.example.eddystudio.bartable.ViewModel;


import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;

import android.view.View;
import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Etd;

public class HomePageRecyclerViewItemModel {
    public final String from;
    public final ObservableField<String> destination = new ObservableField<>("");
    public final ObservableField<String> firstTrain = new ObservableField<>("");
    public final ObservableField<String> secondTrain = new ObservableField<>("");
    public final ObservableField<String> thirdTrain = new ObservableField<>("");
    public final ObservableInt routColor = new ObservableInt(1);
    private final Etd etd;

    private ItemClickListener itemClickListener;

    public HomePageRecyclerViewItemModel(String from, Etd etd) {
        this.from = from;
        this.etd = etd;
        updateUi();
    }

    public void setItemClickListener (ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public void onItemClicked(View view){
        itemClickListener.onItemClicked(from, destination.get(), routColor.get(), view);
    }

    private void updateUi(){
        String m = " minutes";
        String first = "";
        String second = "";
        String third = "";
        if (etd.getEstimate().size() == 1) {
            first = etd.getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving" :etd.getEstimate().get(0).getMinutes() + m;
        } else if (etd.getEstimate().size() == 2) {
            first = etd.getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving" : etd.getEstimate().get(0).getMinutes() + m;
            second = etd.getEstimate().get(1).getMinutes() + m;
        } else {
            first = etd.getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving" : etd.getEstimate().get(0).getMinutes() + m;
            second = etd.getEstimate().get(1).getMinutes() + m;
            third = etd.getEstimate().get(2).getMinutes() + m;
        }
        this.destination.set(etd.getDestination());
        this.firstTrain.set(first);
        this.secondTrain.set(second);
        this.thirdTrain.set(third);
        this.routColor.set(matchMaterialColor(etd.getEstimate().get(0).getColor()));
    }

    private int matchMaterialColor(String color){
        int mColor = 1;
        switch (color){
            case "GREEN": mColor = Color.parseColor("#388E3C"); break;
            case "BLUE": mColor = Color.parseColor("#1976D2") ;break;
            case "RED": mColor = Color.parseColor("#D32F2F") ; break;
            case "YELLOW": mColor = Color.parseColor("#FBC02D") ; break;
            default: mColor = R.color.routColor_yellow ;
        }
        return mColor;
    }

}
