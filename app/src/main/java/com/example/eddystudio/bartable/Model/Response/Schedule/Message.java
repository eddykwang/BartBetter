
package com.example.eddystudio.bartable.Model.Response.Schedule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("co2_emissions")
    @Expose
    private Co2Emissions co2Emissions;
    @SerializedName("legend")
    @Expose
    private String legend;

    public Co2Emissions getCo2Emissions() {
        return co2Emissions;
    }

    public void setCo2Emissions(Co2Emissions co2Emissions) {
        this.co2Emissions = co2Emissions;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

}
