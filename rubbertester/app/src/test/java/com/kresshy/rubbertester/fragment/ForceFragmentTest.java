package com.kresshy.rubbertester.fragment;

import com.google.gson.Gson;
import com.kresshy.rubbertester.force.ForceData;
import com.kresshy.rubbertester.force.ForceMeasurement;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ForceFragmentTest {

    @Test
    public void testAllMeasurementReceived() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        ForceData data1 = new ForceData();
        data1.setForce(1);
        ForceData data2 = new ForceData();
        data2.setForce(3);
        ForceData data3 = new ForceData();
        data3.setForce(5);
        ForceData data4 = new ForceData();
        data4.setForce(7);
        ForceData data5 = new ForceData();
        data5.setForce(6);

        List<ForceData> measurementData1 = new ArrayList<>();
        measurementData1.add(data1);
        List<ForceData> measurementData2 = new ArrayList<>();
        measurementData1.add(data2);
        List<ForceData> measurementData3 = new ArrayList<>();
        measurementData1.add(data3);
        List<ForceData> measurementData4 = new ArrayList<>();
        measurementData1.add(data4);
        List<ForceData> measurementData5 = new ArrayList<>();
        measurementData1.add(data5);

        ForceMeasurement measurement1 = new ForceMeasurement();
        measurement1.setMeasurements(measurementData1);
        ForceMeasurement measurement2 = new ForceMeasurement();
        measurement2.setMeasurements(measurementData2);
        ForceMeasurement measurement3 = new ForceMeasurement();
        measurement3.setMeasurements(measurementData3);
        ForceMeasurement measurement4 = new ForceMeasurement();
        measurement4.setMeasurements(measurementData4);
        ForceMeasurement measurement5 = new ForceMeasurement();
        measurement5.setMeasurements(measurementData5);

        measurements.add(measurement1);
        measurements.add(measurement2);
        measurements.add(measurement3);
        measurements.add(measurement4);
        measurements.add(measurement5);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(5, pullForceMeasurements.size());
    }

    @Test
    public void testAllMeasurementAndReachedMaximumForce() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        ForceData data1 = new ForceData();
        data1.setCount(1);
        data1.setForce(5);
        ForceData data2 = new ForceData();
        data2.setCount(1);
        data2.setForce(6);
        ForceData data3 = new ForceData();
        data3.setCount(1);
        data3.setForce(35001);
        ForceData data4 = new ForceData();
        data4.setCount(1);
        data4.setForce(40);
        ForceData data5 = new ForceData();
        data5.setCount(1);
        data5.setForce(18);

        List<ForceData> measurementData1 = new ArrayList<>();
        measurementData1.add(data1);
        List<ForceData> measurementData2 = new ArrayList<>();
        measurementData2.add(data2);
        List<ForceData> measurementData3 = new ArrayList<>();
        measurementData3.add(data3);
        List<ForceData> measurementData4 = new ArrayList<>();
        measurementData4.add(data4);
        List<ForceData> measurementData5 = new ArrayList<>();
        measurementData5.add(data5);

        ForceMeasurement measurement1 = new ForceMeasurement();
        measurement1.setMeasurements(measurementData1);
        ForceMeasurement measurement2 = new ForceMeasurement();
        measurement2.setMeasurements(measurementData2);
        ForceMeasurement measurement3 = new ForceMeasurement();
        measurement3.setMeasurements(measurementData3);
        ForceMeasurement measurement4 = new ForceMeasurement();
        measurement4.setMeasurements(measurementData4);
        ForceMeasurement measurement5 = new ForceMeasurement();
        measurement5.setMeasurements(measurementData5);

        measurements.add(measurement1);
        measurements.add(measurement2);
        measurements.add(measurement3);
        measurements.add(measurement4);
        measurements.add(measurement5);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(3, pullForceMeasurements.size());
        Assert.assertEquals(2, releaseForceMeasurements.size());
    }

    @Test
    public void testZeroValueWhenPulling() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        ForceData data1 = new ForceData();
        data1.setCount(1);
        data1.setForce(5);
        ForceData data2 = new ForceData();
        data2.setCount(1);
        data2.setForce(0);
        ForceData data3 = new ForceData();
        data3.setCount(1);
        data3.setForce(35001);
        ForceData data4 = new ForceData();
        data4.setCount(1);
        data4.setForce(40);
        ForceData data5 = new ForceData();
        data5.setCount(1);
        data5.setForce(18);

        List<ForceData> measurementData1 = new ArrayList<>();
        measurementData1.add(data1);
        List<ForceData> measurementData2 = new ArrayList<>();
        measurementData2.add(data2);
        List<ForceData> measurementData3 = new ArrayList<>();
        measurementData3.add(data3);
        List<ForceData> measurementData4 = new ArrayList<>();
        measurementData4.add(data4);
        List<ForceData> measurementData5 = new ArrayList<>();
        measurementData5.add(data5);

        ForceMeasurement measurement1 = new ForceMeasurement();
        measurement1.setMeasurements(measurementData1);
        ForceMeasurement measurement2 = new ForceMeasurement();
        measurement2.setMeasurements(measurementData2);
        ForceMeasurement measurement3 = new ForceMeasurement();
        measurement3.setMeasurements(measurementData3);
        ForceMeasurement measurement4 = new ForceMeasurement();
        measurement4.setMeasurements(measurementData4);
        ForceMeasurement measurement5 = new ForceMeasurement();
        measurement5.setMeasurements(measurementData5);

        measurements.add(measurement1);
        measurements.add(measurement2);
        measurements.add(measurement3);
        measurements.add(measurement4);
        measurements.add(measurement5);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(2, pullForceMeasurements.size());
        // The second element is corrected containing two measurements.
        Assert.assertEquals(2, pullForceMeasurements.get(1).getMeasurements().size());
        // This should be the calculated average of the two measurements.
        Assert.assertEquals(11668, pullForceMeasurements.get(1).getMeasurements().get(0).getForce(), 1);
        Assert.assertEquals(2, releaseForceMeasurements.size());
    }

    @Test
    public void testZeroValueWhenReleaseTurnover() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        ForceData data1 = new ForceData();
        data1.setCount(1);
        data1.setForce(5);
        ForceData data2 = new ForceData();
        data2.setCount(1);
        data2.setForce(11);
        ForceData data3 = new ForceData();
        data3.setCount(1);
        data3.setForce(35001);
        ForceData data4 = new ForceData();
        data4.setCount(1);
        data4.setForce(0);
        ForceData data5 = new ForceData();
        data5.setCount(1);
        data5.setForce(5);

        List<ForceData> measurementData1 = new ArrayList<>();
        measurementData1.add(data1);
        List<ForceData> measurementData2 = new ArrayList<>();
        measurementData2.add(data2);
        List<ForceData> measurementData3 = new ArrayList<>();
        measurementData3.add(data3);
        List<ForceData> measurementData4 = new ArrayList<>();
        measurementData4.add(data4);
        List<ForceData> measurementData5 = new ArrayList<>();
        measurementData5.add(data5);

        ForceMeasurement measurement1 = new ForceMeasurement();
        measurement1.setMeasurements(measurementData1);
        ForceMeasurement measurement2 = new ForceMeasurement();
        measurement2.setMeasurements(measurementData2);
        ForceMeasurement measurement3 = new ForceMeasurement();
        measurement3.setMeasurements(measurementData3);
        ForceMeasurement measurement4 = new ForceMeasurement();
        measurement4.setMeasurements(measurementData4);
        ForceMeasurement measurement5 = new ForceMeasurement();
        measurement5.setMeasurements(measurementData5);

        measurements.add(measurement1);
        measurements.add(measurement2);
        measurements.add(measurement3);
        measurements.add(measurement4);
        measurements.add(measurement5);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(3, pullForceMeasurements.size());
        Assert.assertEquals(1, releaseForceMeasurements.size());
        // The first element is corrected containing two measurements.
        Assert.assertEquals(2, releaseForceMeasurements.get(0).getMeasurements().size());
        // This should be the calculated average of the two measurements.
        Assert.assertEquals(11668, releaseForceMeasurements.get(0).getMeasurements().get(0).getForce(), 1);
    }


    @Test
    public void testZeroValueWhenRelease() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        ForceData data1 = new ForceData();
        data1.setCount(1);
        data1.setForce(5);
        ForceData data2 = new ForceData();
        data2.setCount(1);
        data2.setForce(35001);
        ForceData data3 = new ForceData();
        data3.setCount(1);
        data3.setForce(10000);
        ForceData data4 = new ForceData();
        data4.setCount(1);
        data4.setForce(0);
        ForceData data5 = new ForceData();
        data5.setCount(1);
        data5.setForce(5);

        List<ForceData> measurementData1 = new ArrayList<>();
        measurementData1.add(data1);
        List<ForceData> measurementData2 = new ArrayList<>();
        measurementData2.add(data2);
        List<ForceData> measurementData3 = new ArrayList<>();
        measurementData3.add(data3);
        List<ForceData> measurementData4 = new ArrayList<>();
        measurementData4.add(data4);
        List<ForceData> measurementData5 = new ArrayList<>();
        measurementData5.add(data5);

        ForceMeasurement measurement1 = new ForceMeasurement();
        measurement1.setMeasurements(measurementData1);
        ForceMeasurement measurement2 = new ForceMeasurement();
        measurement2.setMeasurements(measurementData2);
        ForceMeasurement measurement3 = new ForceMeasurement();
        measurement3.setMeasurements(measurementData3);
        ForceMeasurement measurement4 = new ForceMeasurement();
        measurement4.setMeasurements(measurementData4);
        ForceMeasurement measurement5 = new ForceMeasurement();
        measurement5.setMeasurements(measurementData5);

        measurements.add(measurement1);
        measurements.add(measurement2);
        measurements.add(measurement3);
        measurements.add(measurement4);
        measurements.add(measurement5);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(2, pullForceMeasurements.size());
        Assert.assertEquals(2, releaseForceMeasurements.size());
        // The second element is corrected containing two measurements.
        Assert.assertEquals(2, releaseForceMeasurements.get(1).getMeasurements().size());
        // This should be the calculated average of the two measurements.
        Assert.assertEquals(3335, releaseForceMeasurements.get(1).getMeasurements().get(0).getForce(), 1);
    }


    @Test
    public void testMeasurementsSkippedAboveMaximumForce() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        ForceData data1 = new ForceData();
        data1.setCount(1);
        data1.setForce(5);
        ForceData data2 = new ForceData();
        data2.setCount(1);
        data2.setForce(35001);
        ForceData data3 = new ForceData();
        data3.setCount(1);
        data3.setForce(36000);
        ForceData data4 = new ForceData();
        data4.setCount(1);
        data4.setForce(36000);
        ForceData data5 = new ForceData();
        data5.setCount(1);
        data5.setForce(5);

        List<ForceData> measurementData1 = new ArrayList<>();
        measurementData1.add(data1);
        List<ForceData> measurementData2 = new ArrayList<>();
        measurementData2.add(data2);
        List<ForceData> measurementData3 = new ArrayList<>();
        measurementData3.add(data3);
        List<ForceData> measurementData4 = new ArrayList<>();
        measurementData4.add(data4);
        List<ForceData> measurementData5 = new ArrayList<>();
        measurementData5.add(data5);

        ForceMeasurement measurement1 = new ForceMeasurement();
        measurement1.setMeasurements(measurementData1);
        ForceMeasurement measurement2 = new ForceMeasurement();
        measurement2.setMeasurements(measurementData2);
        ForceMeasurement measurement3 = new ForceMeasurement();
        measurement3.setMeasurements(measurementData3);
        ForceMeasurement measurement4 = new ForceMeasurement();
        measurement4.setMeasurements(measurementData4);
        ForceMeasurement measurement5 = new ForceMeasurement();
        measurement5.setMeasurements(measurementData5);

        measurements.add(measurement1);
        measurements.add(measurement2);
        measurements.add(measurement3);
        measurements.add(measurement4);
        measurements.add(measurement5);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(2, pullForceMeasurements.size());
        Assert.assertEquals(1, releaseForceMeasurements.size());
        // The second element is corrected containing two measurements.
        Assert.assertEquals(5, pullForceMeasurements.get(0).getMeasurements().get(0).getForce(), 1);
        Assert.assertEquals(35001, pullForceMeasurements.get(1).getMeasurements().get(0).getForce(), 1);
        Assert.assertEquals(5, releaseForceMeasurements.get(0).getMeasurements().get(0).getForce(), 1);
    }

    @Test
    public void testMeasurementsWhenMultipleZeroValuesComing() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        Gson gson = new Gson();

        String measurement1 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":5,\"count\":1545}]}";
        ForceMeasurement forceMeasurement1 = gson.fromJson(measurement1, ForceMeasurement.class);
        String measurement2 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":0,\"count\":1546}]}";
        ForceMeasurement forceMeasurement2 = gson.fromJson(measurement2, ForceMeasurement.class);
        String measurement3 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":0,\"count\":1547}]}";
        ForceMeasurement forceMeasurement3 = gson.fromJson(measurement3, ForceMeasurement.class);
        String measurement4 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":35001,\"count\":1548}]}";
        ForceMeasurement forceMeasurement4 = gson.fromJson(measurement4, ForceMeasurement.class);
        String measurement5 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":40,\"count\":1549}]}";
        ForceMeasurement forceMeasurement5 = gson.fromJson(measurement5, ForceMeasurement.class);
        String measurement6 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":2,\"count\":1550}]}";
        ForceMeasurement forceMeasurement6 = gson.fromJson(measurement6, ForceMeasurement.class);

        measurements.add(forceMeasurement1);
        measurements.add(forceMeasurement2);
        measurements.add(forceMeasurement3);
        measurements.add(forceMeasurement4);
        measurements.add(forceMeasurement5);
        measurements.add(forceMeasurement6);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(2, pullForceMeasurements.size());
        Assert.assertEquals(2, releaseForceMeasurements.size());
    }

    @Test
    public void testMeasurementsBelowMinimumForce() {
        List<ForceMeasurement> pullForceMeasurements = new ArrayList<>();
        List<ForceMeasurement> releaseForceMeasurements = new ArrayList<>();

        ForceFragment SUT = new ForceFragment(pullForceMeasurements, releaseForceMeasurements);

        List<ForceMeasurement> measurements = new ArrayList<>();

        Gson gson = new Gson();

        String measurement1 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":5,\"count\":1545}]}";
        ForceMeasurement forceMeasurement1 = gson.fromJson(measurement1, ForceMeasurement.class);
        String measurement2 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":99,\"count\":1546}]}";
        ForceMeasurement forceMeasurement2 = gson.fromJson(measurement2, ForceMeasurement.class);
        String measurement3 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":120,\"count\":1547}]}";
        ForceMeasurement forceMeasurement3 = gson.fromJson(measurement3, ForceMeasurement.class);
        String measurement4 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":35001,\"count\":1548}]}";
        ForceMeasurement forceMeasurement4 = gson.fromJson(measurement4, ForceMeasurement.class);
        String measurement5 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":10001,\"count\":1549}]}";
        ForceMeasurement forceMeasurement5 = gson.fromJson(measurement5, ForceMeasurement.class);
        String measurement6 = "{\"version\":1,\"unit\":\"cm\",\"leap\":4,\"measurements\":[{\"unit\":\"gram\",\"force\":10,\"count\":1550}]}";
        ForceMeasurement forceMeasurement6 = gson.fromJson(measurement6, ForceMeasurement.class);

        measurements.add(forceMeasurement1);
        measurements.add(forceMeasurement2);
        measurements.add(forceMeasurement3);
        measurements.add(forceMeasurement4);
        measurements.add(forceMeasurement5);
        measurements.add(forceMeasurement6);

        SUT.enableMeasurementForTesting();
        SUT.disableUIChangesForTesting();
        SUT.disableSounds();

        for (ForceMeasurement measurement : measurements) {
            SUT.measurementReceived(measurement);
        }

        Assert.assertEquals(2, pullForceMeasurements.size());
        Assert.assertEquals(1, releaseForceMeasurements.size());
    }

}