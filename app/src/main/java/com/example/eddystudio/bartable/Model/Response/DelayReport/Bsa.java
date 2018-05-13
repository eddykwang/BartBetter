
package com.example.eddystudio.bartable.Model.Response.DelayReport;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bsa {

    @SerializedName("station")
    @Expose
    private String station;
    @SerializedName("description")
    @Expose
    private Description description;
    @SerializedName("sms_text")
    @Expose
    private SmsText smsText;

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public SmsText getSmsText() {
        return smsText;
    }

    public void setSmsText(SmsText smsText) {
        this.smsText = smsText;
    }

}
