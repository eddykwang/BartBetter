
package com.eddystudio.bartbetter.Model.Response.Stations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Stations {

    @SerializedName("station")
    @Expose
    private List<Station> station = null;

    public List<Station> getStation() {
        return station;
    }

    public void setStation(List<Station> station) {
        this.station = station;
    }

}
