package com.kresshy.rubbertester.serialization;

import com.kresshy.rubbertester.force.ForceMeasurement;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class SerializableMeasurementStorage implements Serializable {

    List<ForceMeasurement> measurementList;

    public void addMeasurement(ForceMeasurement measurement) {
        this.measurementList.add(measurement);
    }
}
