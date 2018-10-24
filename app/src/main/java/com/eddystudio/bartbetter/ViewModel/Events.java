package com.eddystudio.bartbetter.ViewModel;

import android.view.View;

import java.util.ArrayList;

public interface Events {
  class LoadingEvent implements Events {
    private final boolean load;

    public LoadingEvent(boolean load) {this.load = load;}

    public boolean isLoad() {
      return load;
    }
  }

  class ErrorEvent implements Events {
    private final Throwable error;

    public ErrorEvent(Throwable error) {this.error = error;}

    public Throwable getError() {
      return error;
    }
  }

  class CompleteEvent implements Events {
    private final ArrayList<QuickLookupRecyclerViewItemVM> bartList;

    public CompleteEvent(ArrayList<QuickLookupRecyclerViewItemVM> bartList) {this.bartList = bartList;}

    public ArrayList<QuickLookupRecyclerViewItemVM> getBartList() {
      return bartList;
    }
  }

  class GoToDetailEvent implements Events {
    private final String from;
    private final String to;
    private final int routeColor;
    private final View view;

    public GoToDetailEvent(String from, String to, int routeColor, View view) {
      this.from = from;
      this.to = to;
      this.routeColor = routeColor;
      this.view = view;
    }

    public String getFrom() {
      return from;
    }

    public String getTo() {
      return to;
    }

    public int getRouteColor() {
      return routeColor;
    }

    public View getView() {
      return view;
    }
  }

  class GetDataEvent implements Events {
    private final Object etdStations;

    public GetDataEvent(Object etdStations) {this.etdStations = etdStations;}

    public Object getData() {
      return etdStations;
    }
  }
}
