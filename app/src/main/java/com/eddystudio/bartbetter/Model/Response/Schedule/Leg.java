
package com.eddystudio.bartbetter.Model.Response.Schedule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Leg {

    @SerializedName("@order")
    @Expose
    private String order;
    @SerializedName("@origin")
    @Expose
    private String origin;
    @SerializedName("@destination")
    @Expose
    private String destination;
    @SerializedName("@origTimeMin")
    @Expose
    private String origTimeMin;
    @SerializedName("@origTimeDate")
    @Expose
    private String origTimeDate;
    @SerializedName("@destTimeMin")
    @Expose
    private String destTimeMin;
    @SerializedName("@destTimeDate")
    @Expose
    private String destTimeDate;
    @SerializedName("@line")
    @Expose
    private String line;
    @SerializedName("@bikeflag")
    @Expose
    private String bikeflag;
    @SerializedName("@trainHeadStation")
    @Expose
    private String trainHeadStation;
    @SerializedName("@load")
    @Expose
    private String load;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigTimeMin() {
        return origTimeMin;
    }

    public void setOrigTimeMin(String origTimeMin) {
        this.origTimeMin = origTimeMin;
    }

    public String getOrigTimeDate() {
        return origTimeDate;
    }

    public void setOrigTimeDate(String origTimeDate) {
        this.origTimeDate = origTimeDate;
    }

    public String getDestTimeMin() {
        return destTimeMin;
    }

    public void setDestTimeMin(String destTimeMin) {
        this.destTimeMin = destTimeMin;
    }

    public String getDestTimeDate() {
        return destTimeDate;
    }

    public void setDestTimeDate(String destTimeDate) {
        this.destTimeDate = destTimeDate;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getBikeflag() {
        return bikeflag;
    }

    public void setBikeflag(String bikeflag) {
        this.bikeflag = bikeflag;
    }

    public String getTrainHeadStation() {
        return trainHeadStation;
    }

    public void setTrainHeadStation(String trainHeadStation) {
        this.trainHeadStation = trainHeadStation;
    }

    public String getLoad() {
        return load;
    }

    public void setLoad(String load) {
        this.load = load;
    }

}
