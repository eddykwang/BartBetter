
package com.eddystudio.bartbetter.Model.Response.Fares;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Discount {

    @SerializedName("clipper")
    @Expose
    private String clipper;

    public String getClipper() {
        return clipper;
    }

    public void setClipper(String clipper) {
        this.clipper = clipper;
    }

}
