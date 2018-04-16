package com.example.eddystudio.bartable.ViewModel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;

public class RouteDetailViewModel {
  public ObservableField<String> from = new ObservableField<>("");
  public ObservableField<String> to = new ObservableField<>("");
  public ObservableInt color = new ObservableInt();

  public RouteDetailViewModel(String from, String to, int color) {
    this.from.set(from);
    this.to.set(to);
    this.color.set(color);
  }
}
