
package com.eddystudio.bartbetter.Model.Response.EstimateResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Etd {

    @SerializedName("destination")
    @Expose
    private String destination;
    @SerializedName("abbreviation")
    @Expose
    private String abbreviation;
    @SerializedName("limited")
    @Expose
    private String limited;
    @SerializedName("estimate")
    @Expose
    private List<Estimate> estimate = null;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getLimited() {
        return limited;
    }

    public void setLimited(String limited) {
        this.limited = limited;
    }

    public List<Estimate> getEstimate() {
        return estimate;
    }

    public void setEstimate(List<Estimate> estimate) {
        this.estimate = estimate;
    }

}
