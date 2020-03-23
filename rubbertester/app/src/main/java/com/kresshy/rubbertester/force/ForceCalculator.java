package com.kresshy.rubbertester.force;

import java.util.List;

public class ForceCalculator {

    private List<ForceMeasurement> forceMeasurementList;
    private double leap = 10.825;

    public ForceCalculator(List<ForceMeasurement> forceMeasurementList, double leap) {
        this.forceMeasurementList = forceMeasurementList;
        this.leap = leap;
    }

    public double calculateMaximumWorkLoad() {
        double maximumWorkLoad = 0;

        for (ForceMeasurement measurement : forceMeasurementList) {
            final int leap = measurement.getLeap();

            for (ForceData data : measurement.getMeasurements()) {
                maximumWorkLoad += (data.getForce() * this.leap);
            }
        }

        return maximumWorkLoad;
    }
}
