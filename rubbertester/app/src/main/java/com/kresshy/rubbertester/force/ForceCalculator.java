package com.kresshy.rubbertester.force;

import java.util.List;

public class ForceCalculator {

    private List<ForceMeasurement> forceMeasurementList;

    public ForceCalculator(List<ForceMeasurement> forceMeasurementList) {
        this.forceMeasurementList = forceMeasurementList;
    }

    public double calculateMaximumWorkLoad() {
        double maximumWorkLoad = 0;

        for(ForceMeasurement measurement: forceMeasurementList) {
            final int leap = measurement.getLeap();

            for (ForceData data: measurement.getMeasurements()) {
                maximumWorkLoad += (data.getForce() * 5.4125);
            }
        }

        return maximumWorkLoad;
    }
}
