package com.kresshy.rubbertester.force;

import java.io.Serializable;
import java.util.List;

import lombok.Data;


// start_{"version":1,"unit":"cm","leap":4,"measurements":[{"unit":"gram","force":0,"count":300}]}_end
@Data
public class ForceMeasurement implements Serializable {
     private int version;
     private String unit;
     private String leap;
     private List<ForceData> measurements;

     public void addForceDataToMeasurements(ForceData data) {
         measurements.add(data);
     }
}
