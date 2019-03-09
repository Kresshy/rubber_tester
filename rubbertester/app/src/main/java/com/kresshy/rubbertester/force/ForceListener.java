package com.kresshy.rubbertester.force;

public interface ForceListener {
    void measurementReceived(ForceMeasurement forceMeasurement);

    void enableMeasurementForLoad();

    void disableMeasurementForLoad();
}
