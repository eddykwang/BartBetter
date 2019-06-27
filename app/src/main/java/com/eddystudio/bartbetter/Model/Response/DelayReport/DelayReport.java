
package com.eddystudio.bartbetter.Model.Response.DelayReport;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DelayReport {

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
