
package com.eddystudio.bartbetter.Model.Response.EstimateResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Estimate {

    @SerializedName("minutes")
    @Expose
    private String minutes;
    @SerializedName("platform")
    @Expose
    private String platform;
    @SerializedName("direction")
    @Expose
    private String direction;
    @SerializedName("length")
    @Expose
    private String length;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("hexcolor")
    @Expose
    private String hexcolor;
    @SerializedName("bikeflag")
    @Expose
    private String bikeflag;
    @SerializedName("delay")
    @Expose
    private String delay;

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getHexcolor() {
        return hexcolor;
    }

    public void setHexcolor(String hexcolor) {
        this.hexcolor = hexcolor;
    }

    public String getBikeflag() {
        return bikeflag;
    }

    public void setBikeflag(String bikeflag) {
        this.bikeflag = bikeflag;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

}
