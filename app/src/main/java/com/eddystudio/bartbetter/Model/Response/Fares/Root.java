
package com.eddystudio.bartbetter.Model.Response.Fares;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Root {

    @SerializedName("uri")
    @Expose
    private Uri uri;
    @SerializedName("origin")
    @Expose
    private String origin;
    @SerializedName("destination")
    @Expose
    private String destination;
    @SerializedName("sched_num")
    @Expose
    private String schedNum;
    @SerializedName("trip")
    @Expose
    private Trip trip;
    @SerializedName("fares")
    @Expose
    private RouteFares fares;
    @SerializedName("message")
    @Expose
    private Message message;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
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

    public String getSchedNum() {
        return schedNum;
    }

    public void setSchedNum(String schedNum) {
        this.schedNum = schedNum;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public RouteFares getFares() {
        return fares;
    }

    public void setFares(RouteFares fares) {
        this.fares = fares;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}
