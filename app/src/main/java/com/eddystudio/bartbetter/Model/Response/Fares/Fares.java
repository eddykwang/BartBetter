
package com.eddystudio.bartbetter.Model.Response.Fares;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Fares {

    @SerializedName("?xml")
    @Expose
    private Xml xml;
    @SerializedName("root")
    @Expose
    private Root root;

    public Xml getXml() {
        return xml;
    }

    public void setXml(Xml xml) {
        this.xml = xml;
    }

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

}
