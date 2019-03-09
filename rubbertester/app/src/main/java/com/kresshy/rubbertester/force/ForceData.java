package com.kresshy.rubbertester.force;

import java.io.Serializable;

import lombok.Data;


// {"unit":"gram","force":0,"count":300}
@Data
public class ForceData implements Serializable {
    private String unit;
    private double force;
    private int count;

    public ForceData() {
        this.unit = "cm";
        this.force = 0;
        this.count = 0;
    }

    public ForceData(double force, int count) {
        this.unit = "cm";
        this.force = force;
        this.count = count;
    }

    public ForceData(String unit, double force, int count) {
        this.unit = unit;
        this.force = force;
        this.count = count;
    }
}
