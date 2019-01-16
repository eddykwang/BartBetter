package com.eddystudio.bartbetter.ViewModel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.view.View;

import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Estimate;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.Model.Uilt;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.eddystudio.bartbetter.Model.Uilt.getFullStationName;
import static com.eddystudio.bartbetter.Model.Uilt.materialColorConverter;

public class DashboardRecyclerViewItemVM {
  public final ObservableField<String> destination = new ObservableField<>("");
  public final ObservableField<String> fromStation = new ObservableField<>("");
  public final ObservableField<String> firstTrain = new ObservableField<>("");
  public final ObservableField<String> secondTrain = new ObservableField<>("");
  public final ObservableField<String> thirdTrain = new ObservableField<>("");
  public final ObservableField<String> trainNameLength = new ObservableField<>("");
  public final ObservableInt routeColor = new ObservableInt(Color.GRAY);
  public final ObservableInt routeColor2 = new ObservableInt(Color.GRAY);


  private String from = "";
  private String to = "";
  private ItemClickListener itemClickListener;


  public DashboardRecyclerViewItemVM(List<Etd> etds, String from, String to) {
    this.from = from;
    this.to = to;
    updateUi(etds, from, to);
  }


  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public void setItemClickListener(
      ItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public void onItemClicked(View view) {
    if(itemClickListener != null) {
      itemClickListener.onItemClicked(from, to, routeColor.get(), view);
    }
  }

  private void updateUi(List<Etd> etds, String origin, String dest) {
    fromStation.set(Uilt.getFullStationName(origin));
    destination.set(Uilt.getFullStationName(dest));

    if(etds.isEmpty()) return;


    if(etds.get(0).getEstimate() != null) {
      routeColor.set(materialColorConverter(etds.get(0).getEstimate().get(0).getColor()));
      trainNameLength.set(etds.get(0).getEstimate().get(0).getLength() + " car " + getFullStationName(etds.get(0).getDestination()) + " train");
    } else {
      firstTrain.set("Unavailable");
    }

    Set<String> timeList = new HashSet<>();

    for(Etd etd : etds) {
      for(Estimate estimate : etd.getEstimate()) {
        timeList.add(estimate.getMinutes().equals("Leaving") ? "0" : estimate.getMinutes());
      }
    }

    List<String> tl = new ArrayList<>(timeList);
    tl.sort((t1, t2) -> Integer.parseInt(t1) - Integer.parseInt(t2));

    if(tl.size() > 0) {
      firstTrain.set(tl.get(0));
    }
    if(tl.size() > 1) {
      secondTrain.set(tl.get(1));
    }
    if(tl.size() > 2) {
      thirdTrain.set(tl.get(2));
    }

  }
}
