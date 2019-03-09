package com.kresshy.rubbertester.force;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;


//{"version":1,"unit":"cm","leap":4,"measurements":[{"unit":"gram","force":0,"count":300}]}
@Data
public class ForceMeasurement implements Serializable {
    private int version;
    private String unit;
    private int leap;
    private List<ForceData> measurements;

    public ForceMeasurement() {
        version = 0;
        unit = "";
        leap = 0;
        measurements = new ArrayList<>();
    }

    public ForceMeasurement(int version, String unit, int leap) {
        this.version = version;
        this.unit = unit;
        this.leap = leap;
    }

    public ForceMeasurement(int version, String unit, int leap, List<ForceData> measurements) {
        this.version = version;
        this.unit = unit;
        this.leap = leap;
        this.measurements = measurements;
    }

    public void addForceDataToMeasurements(ForceData data) {
        measurements.add(data);
    }
}
