package com.eddystudio.bartbetter.ViewModel;


import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;

import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Uilt;

public class HomePageRecyclerViewItemModel {
    public String from = "";
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
        String to = "";
        if (destination != null){
            to = etd.getAbbreviation();
        }
        itemClickListener.onItemClicked(from, to, routColor.get(), view);
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
        this.routColor.set(Uilt.materialColorConverter(etd.getEstimate().get(0).getColor()));
    }

}
