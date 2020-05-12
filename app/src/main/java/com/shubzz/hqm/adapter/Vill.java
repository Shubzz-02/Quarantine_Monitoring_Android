package com.shubzz.hqm.adapter;

public class Vill {
    public String nm, total;

    public Vill() {
    }

    public Vill(String name, String total) {
        this.nm = name;
        this.total = total;
    }

    public String getName() {
        return this.nm;
    }

    public String getTotal() {
        return this.total;
    }
}
