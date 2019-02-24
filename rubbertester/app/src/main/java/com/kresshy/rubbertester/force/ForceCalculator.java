package com.kresshy.rubbertester.force;

import java.util.List;

public class ForceCalculator {

    private List<ForceMeasurement> forceMeasurementList;

    public ForceCalculator(List<ForceMeasurement> forceMeasurementList) {
        this.forceMeasurementList = forceMeasurementList;
    }

    public int calculateMaximumWorkLoad() {
        int maximumWorkLoad = 0;

        for(ForceMeasurement measurement: forceMeasurementList) {
            final int leap = Integer.parseInt(measurement.getLeap());

            for (ForceData data: measurement.getMeasurements()) {
                maximumWorkLoad += data.getForce() * leap;
            }
        }

        return maximumWorkLoad;
    }
}
