
package com.eddystudio.bartbetter.Model.Response.Schedule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Fares {

    @SerializedName("@level")
    @Expose
    private String level;
    @SerializedName("fare")
    @Expose
    private Fare fare;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Fare getFare() {
        return fare;
    }

    public void setFare(Fare fare) {
        this.fare = fare;
    }

}
