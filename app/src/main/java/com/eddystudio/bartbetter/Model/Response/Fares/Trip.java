
package com.eddystudio.bartbetter.Model.Response.Fares;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Trip {

    @SerializedName("fare")
    @Expose
    private String fare;
    @SerializedName("discount")
    @Expose
    private Discount discount;

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

}
