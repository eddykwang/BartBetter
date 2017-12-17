
package com.example.eddystudio.bartable.Repository.Response;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Station {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("abbr")
    @Expose
    private String abbr;
    @SerializedName("etd")
    @Expose
    private List<Etd> etd = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public List<Etd> getEtd() {
        return etd;
    }

    public void setEtd(List<Etd> etd) {
        this.etd = etd;
    }

}
