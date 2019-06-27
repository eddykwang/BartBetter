
package com.eddystudio.bartbetter.Model.Response.ElevatorStatus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ElevatorStatus {

    @SerializedName("root")
    @Expose
    private Root root;

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

}
