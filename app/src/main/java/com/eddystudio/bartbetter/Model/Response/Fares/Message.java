
package com.eddystudio.bartbetter.Model.Response.Fares;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("co2_emissions")
    @Expose
    private Co2Emissions co2Emissions;

    public Co2Emissions getCo2Emissions() {
        return co2Emissions;
    }

    public void setCo2Emissions(Co2Emissions co2Emissions) {
        this.co2Emissions = co2Emissions;
    }

}
