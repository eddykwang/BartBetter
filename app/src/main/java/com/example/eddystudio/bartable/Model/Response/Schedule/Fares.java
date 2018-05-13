
package com.example.eddystudio.bartable.Model.Response.Schedule;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Fares {

    @SerializedName("@level")
    @Expose
    private String level;
    @SerializedName("fare")
    @Expose
    private List<Fare> fare = null;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<Fare> getFare() {
        return fare;
    }

    public void setFare(List<Fare> fare) {
        this.fare = fare;
    }

}
