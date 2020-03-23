package com.kresshy.rubbertester.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.kresshy.rubbertester.R;
import com.kresshy.rubbertester.activity.RTActivity;
import com.kresshy.rubbertester.broadcast.AlarmReceiver;
import com.kresshy.rubbertester.force.ForceCalculator;
import com.kresshy.rubbertester.force.ForceData;
import com.kresshy.rubbertester.force.ForceListener;
import com.kresshy.rubbertester.force.ForceMeasurement;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;


public class ForceFragment extends Fragment implements ForceListener, View.OnClickListener {
    private static final String TAG = "ForceFragment";

    private LinearLayout bottomLayout;
    private LinearLayout forceGraphContainer;

    private LineGraphView forceGraph;
    private GraphViewData[] forceData;
    private GraphViewSeries forceSeries;

    private TextView pullForceTextView;
    private TextView pullLengthTextView;
    private TextView releaseLengthTextView;
    private TextView pullWorkTextView;
    private TextView releaseWorkTextView;
    private TextView possibleRotCountTextView;

    private Button startButton;
    private Button stopButton;
    private Button resetButton;
    private Button maxForceReachedButton;

    private boolean measurementEnabled = false;

    private int measurementCount = 0;
    private int releaseMeasurementCount = 0;
    private int numberOfSamples = 7000;
    private int maximumForce = 35000; // grams
    private int minimumForce = 100;
    private boolean maximumForceReached = false;
    private boolean enableSounds = true;
    private boolean aboveMaximumForce = false;
    private boolean belowMinimumForce = true;
    private boolean zeroValueHandlingMode = false;
    private boolean disableUIChangesForUnitTesting = false;
    private List<ForceMeasurement> zeroMeasurementList;

    private List<ForceMeasurement> pullForceMeasurementList;
    private List<ForceMeasurement> releaseForceMeasurementList;

    private ForceCalculator pullForceCalculator;
    private ForceCalculator releaseForceCalculator;

    final private int yellowColor = Color.YELLOW;
    final private int redColor = Color.RED;
    final private int lightGreyColor = Color.LTGRAY;

    private double leap = 10.825;
    private double coefficientValue = 1.6;

    private OnFragmentInteractionListener mListener;

    public ForceFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public ForceFragment(List<ForceMeasurement> pullForceMeasurementList, List<ForceMeasurement> releaseForceMeasurementList) {
        coefficientValue = 1.6;
        leap = 10.825;

        this.pullForceMeasurementList = pullForceMeasurementList;
        this.releaseForceMeasurementList = releaseForceMeasurementList;
        pullForceCalculator = new ForceCalculator(pullForceMeasurementList, leap);
        releaseForceCalculator = new ForceCalculator(releaseForceMeasurementList, leap);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mListener.registerForceDataReceiver(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()
        );

        coefficientValue = Double.parseDouble(sharedPreferences.getString("pref_coefficient_value", "1.6"));
        leap = Double.parseDouble(sharedPreferences.getString("pref_step_value", "10.825"));

        pullForceMeasurementList = new ArrayList<>();
        releaseForceMeasurementList = new ArrayList<>();
        pullForceCalculator = new ForceCalculator(pullForceMeasurementList, leap);
        releaseForceCalculator = new ForceCalculator(releaseForceMeasurementList, leap);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_force, container, false);

        // keep screen on
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        forceGraphContainer = (LinearLayout) view.findViewById(R.id.forceGraphContainer);
        pullForceTextView = (TextView) view.findViewById(R.id.pullForceText);
        pullLengthTextView = (TextView) view.findViewById(R.id.pullLengthText);
        releaseLengthTextView = (TextView) view.findViewById(R.id.releaseLengthText);
        pullWorkTextView = (TextView) view.findViewById(R.id.pullWork);
        releaseWorkTextView = (TextView) view.findViewById(R.id.releaseWork);
        possibleRotCountTextView = (TextView) view.findViewById(R.id.possibleRotCount);

        startButton = (Button) view.findViewById(R.id.forceStartButton);
        startButton.setOnClickListener(this);

        stopButton = (Button) view.findViewById(R.id.forceStopButton);
        stopButton.setOnClickListener(this);

        maxForceReachedButton = (Button) view.findViewById(R.id.forceMaxButton);
        maxForceReachedButton.setOnClickListener(this);

        resetButton = (Button) view.findViewById(R.id.forceResetButton);
        resetButton.setOnClickListener(this);

        bottomLayout = (LinearLayout) view.findViewById(R.id.forceGraphBackground);

        createViewForForceGraph(forceGraphContainer);
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        // release flag to keep screen on
        getActivity().getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mListener = null;
    }

    @Override
    public void measurementReceived(ForceMeasurement forceMeasurement) {
        Timber.d("Measurement received.");
        if (measurementEnabled) {
            handleIncomingMeasurement(forceMeasurement);
        } else {
            Timber.d("Measurement not activated.");
        }
    }

    @Override
    public void enableMeasurementForLoad() {
        measurementEnabled = true;
        enableSounds = false;
    }

    @Override
    public void disableMeasurementForLoad() {
        measurementEnabled = false;
        enableSounds = true;
        stopButton.performClick();
    }

    public void disableSounds() {
        enableSounds = false;
    }

    // returns false if there weren't any zero value.
    private boolean handleIfValueIsNull(ForceMeasurement forceMeasurement) {
        if (zeroValueHandlingMode) {
            Timber.d("Zero Handling Mode Activated.");
            for (ForceData data : forceMeasurement.getMeasurements()) {
                // Not just the previous but the current one is also a zero value...
                if (data.getForce() == 0.0) {
                    // adding to the list and staying in zero value handling mode.
                    zeroMeasurementList.add(forceMeasurement);
                    return zeroValueHandlingMode;
                }
            }

            // This means some of the previous measurement was zero. We are going to calculate an
            // average of the previous measurements.
            List<ForceData> previousMeasurementData;
            if (!maximumForceReached) {
                previousMeasurementData = pullForceMeasurementList
                        .get(pullForceMeasurementList.size() - 1).getMeasurements();
            } else {
                if (releaseMeasurementCount > 0) {
                    previousMeasurementData = releaseForceMeasurementList
                            .get(releaseForceMeasurementList.size() - 1).getMeasurements();
                } else {
                    previousMeasurementData = pullForceMeasurementList
                            .get(pullForceMeasurementList.size() - 1).getMeasurements();
                }
            }

            List<ForceData> zeroMeasurementData = new ArrayList<>();

            for (ForceMeasurement measurement : zeroMeasurementList) {
                zeroMeasurementData.addAll(measurement.getMeasurements());
            }

            List<ForceData> currentMeasurementData = forceMeasurement.getMeasurements();

            List<ForceData> allMeasurementData = new ArrayList<>();
            allMeasurementData.addAll(previousMeasurementData);
            allMeasurementData.addAll(zeroMeasurementData);
            allMeasurementData.addAll(currentMeasurementData);

            int i = 0;

            while (i < allMeasurementData.size()) {
                if (allMeasurementData.get(i).getForce() == 0.0) {
                    break;
                }
                i++;
            }


            List<Double> averagesList = new ArrayList<>();
            for (int k = 0; k < zeroMeasurementData.size(); k++) {
                double sumValue = 0.0;
                if (k == 0) {
                    // The last correct value in the list.
                    sumValue += allMeasurementData.get(i - 1).getForce();
                } else {
                    // There are some averages in the list from this point.
                    sumValue += averagesList.get(k - 1);
                }
                // The zero value(s) in the list.
                sumValue += allMeasurementData.get(i + k).getForce();
                // The next correct value in the list after the zero(s).
                sumValue += allMeasurementData.get(i + zeroMeasurementData.size()).getForce();
                averagesList.add(sumValue / (2 + zeroMeasurementData.size() - k));
            }

            List<ForceData> correctedData = new ArrayList<>();
            List<ForceData> incorrectData = new ArrayList<>();
            incorrectData.addAll(zeroMeasurementData);
            incorrectData.addAll(currentMeasurementData);
            for (int j = 0; j < zeroMeasurementData.size() + currentMeasurementData.size(); j++) {
                if (j < zeroMeasurementData.size()) {
                    ForceData data = incorrectData.get(j);
                    data.setForce(averagesList.get(j));
                    correctedData.add(data);
                } else {
                    correctedData.add(incorrectData.get(j));
                }
            }

            forceMeasurement.setMeasurements(correctedData);
            // We have corrected the Zero value
            zeroValueHandlingMode = false;
        } else {
            Timber.d("Zero Handling Mode Deactivated.");
            // Looking for a zero value in the measurements.
            for (ForceData data : forceMeasurement.getMeasurements()) {
                // We allow the first measurement to be zero, otherwise it's a wrong measurement.
                if (data.getForce() == 0.0 && measurementCount > 0) {
                    Timber.d("Found Zero Activating Handling Mode.");
                    // Starting of the zero value(s)...
                    // Cleaning out the existing list and adding the first zero value.
                    zeroMeasurementList = new ArrayList<>();
                    zeroMeasurementList.add(forceMeasurement);
                    zeroValueHandlingMode = true;
                }
            }
        }

        return zeroValueHandlingMode;
    }

    private void handleIncomingMeasurement(ForceMeasurement forceMeasurement) {
        for (ForceData data : forceMeasurement.getMeasurements()) {
            if (data.getForce() >= minimumForce) {
                belowMinimumForce = false;
            } else {
                belowMinimumForce = true;
            }
        }

        if (belowMinimumForce) {
            Timber.d("Measurement below minimum force.");
            return;
        }

        // Check if we are still above the maximum allowed pulling force.
        for (ForceData data : forceMeasurement.getMeasurements()) {
            if (!(data.getForce() >= maximumForce)) {
                aboveMaximumForce = false;
            }
        }
        // If we are above the maximum force we do nothing... otherwise continue with calculations.
        if (aboveMaximumForce) {
            return;
        }

        if (handleIfValueIsNull(forceMeasurement)) {
            Timber.d("Zero measurement received");
            return;
        }

        Timber.d("Handling Incoming measurement");
        // Store all valid (nonzero filtered) measurements for serialization and other calculations.
        if (!maximumForceReached) {
            pullForceMeasurementList.add(forceMeasurement);
        } else {
            releaseForceMeasurementList.add(forceMeasurement);
        }

        for (ForceData data : forceMeasurement.getMeasurements()) {
            measurementCount++;
            if (maximumForceReached) {
                releaseMeasurementCount++;
            }

            colorBackgroundOnForce(data.getForce());

            if (!disableUIChangesForUnitTesting) {
                forceSeries.appendData(new GraphViewData(
                        measurementCount * this.leap,
                        data.getForce()
                ), true, numberOfSamples);


                pullForceTextView.setText(getStringValueInKg(data.getForce()) + " kg");
                if (!maximumForceReached) {
                    pullLengthTextView.setText(getStringValueInCm(measurementCount * this.leap) + " cm");
                } else {
                    releaseLengthTextView.setText(getStringValueInCm(releaseMeasurementCount * this.leap) + " cm");
                }
            }

            colorBackgroundOnForce(data.getForce());
            alertSoundOnForce(data.getForce());

            if (data.getForce() >= maximumForce) {
                maximumForceReached = true;
                aboveMaximumForce = true;
                if (!disableUIChangesForUnitTesting) {
                    pullWorkTextView.setText(
                            getStringValueInKg(
                                    getValueInCm(
                                            pullForceCalculator.calculateMaximumWorkLoad()
                                    )
                            ));
                }
                if (enableSounds) {
                    Intent intent = new Intent(getActivity(), AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            getActivity().getApplicationContext(), 0, intent, 0);

                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                            + (2 * 1000 * 60), pendingIntent);
                }

                Timber.d("Maximum pull force is reached and calculating pull work");
            }
        }

        Timber.d("Number of data records: " + measurementCount);
    }

    private void createViewForForceGraph(LinearLayout container) {
        Timber.d("Creating GraphView For Force measurement");
        forceGraph = new LineGraphView(getActivity().getApplicationContext(), "Force Measurement");
        forceGraph.setScrollable(true);
        forceGraph.setViewPort(0, numberOfSamples);
        forceGraph.setGraphViewStyle(getGraphViewStyle());

        forceGraph.setHorizontalLabels(getHorizontalLabelsForGraph());

        GraphViewData[] forceData = new GraphViewData[1];
        forceData[0] = new GraphViewData(0, 0);
        this.forceData = forceData;

        GraphViewSeries forceSeries = new GraphViewSeries(
                "Pull force",
                new GraphViewSeries.GraphViewSeriesStyle(Color.BLUE, 9),
                forceData
        );

        this.forceSeries = forceSeries;
        forceGraph.addSeries(forceSeries);

        Timber.d("Adding GraphView For Pull Force to LayoutContainer");
        container.addView(forceGraph);
    }

    private String[] getHorizontalLabelsForGraph() {
        return new String[]{"7m", "6m", "5m", "4m", "3m", "2m", "1m", "0m"};
    }

    private String getStringValueInKg(double force) {
        return String.format("%.4f", (force / 1000));
    }

    private String getStringValueInCm(double length) {
        return String.format("%.2f", (length / 10));
    }

    private double getValueInKg(double force) {
        return (force / 1000);
    }

    private double getValueInCm(double length) {
        return (length / 10);
    }

    // TODO color background reaching a certain force on `bottonLayout`. 90% yellow, 95% red,
    // 98% flashing red. When reaching maximum force revert to default or pick a cool down color.
    private void colorBackgroundOnForce(double force) {
        if (disableUIChangesForUnitTesting) {
            return;
        }

        if (force < maximumForce * 0.9) {
            bottomLayout.setBackgroundColor(lightGreyColor);
        } else if (force >= maximumForce * 0.9 && force < maximumForce * 0.98) {
            bottomLayout.setBackgroundColor(yellowColor);
        } else {
            bottomLayout.setBackgroundColor(redColor);
        }
    }

    private void alertSoundOnForce(double force) {
        if (enableSounds) {
            if (force >= maximumForce * 0.75 && force < maximumForce * 0.85) {
                final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_LOW_L, 150);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (toneGenerator != null) {
                            Timber.d(TAG, "ToneGenerator released");
                            toneGenerator.release();
                        }
                    }

                }, 200);
            } else if (force >= maximumForce * 0.90 && force < maximumForce * 0.99) {
                final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_MED_L, 300);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (toneGenerator != null) {
                            Timber.d(TAG, "ToneGenerator released");
                            toneGenerator.release();
                        }
                    }

                }, 300);
            } else if (force >= maximumForce) {
                final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 500);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (toneGenerator != null) {
                            Timber.d(TAG, "ToneGenerator released");
                            toneGenerator.release();
                        }
                    }

                }, 500);
            }
        }
    }

    private GraphViewStyle getGraphViewStyle() {
        GraphViewStyle graphViewStyle = new GraphViewStyle(Color.BLACK, Color.BLACK, Color.GRAY);
        graphViewStyle.setVerticalLabelsAlign(Paint.Align.LEFT);
        graphViewStyle.setVerticalLabelsWidth(80);
        return graphViewStyle;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forceStartButton:
                ((RTActivity) getActivity()).initializeMeasurementStore();
                measurementEnabled = true;
                Timber.d("Starting measurement");
                break;
            case R.id.forceStopButton:
                measurementEnabled = false;
                releaseWorkTextView.setText(
                        getStringValueInKg(
                                getValueInCm(
                                        releaseForceCalculator.calculateMaximumWorkLoad()
                                )
                        ));

                possibleRotCountTextView.setText(getStringValueInCm(((measurementCount - releaseMeasurementCount) * leap) * coefficientValue));
                Timber.d("Stopping measurement and calculating release work");
                break;
            case R.id.forceMaxButton:
                maximumForceReached = true;
                pullWorkTextView.setText(
                        getStringValueInKg(
                                getValueInCm(
                                        pullForceCalculator.calculateMaximumWorkLoad()
                                )
                        ));
                Timber.d("Maximum pull force is reached and calculating pull work");
                break;
            case R.id.forceResetButton:
                forceGraphContainer.removeAllViewsInLayout();

                createViewForForceGraph(forceGraphContainer);

                pullForceMeasurementList = new ArrayList<>();
                releaseForceMeasurementList = new ArrayList<>();

                pullForceCalculator = new ForceCalculator(pullForceMeasurementList, leap);
                releaseForceCalculator = new ForceCalculator(releaseForceMeasurementList, leap);

                measurementCount = 0;
                releaseMeasurementCount = 0;
                maximumForceReached = false;
                aboveMaximumForce = false;
                belowMinimumForce = true;
                zeroValueHandlingMode = false;

                pullForceTextView.setText("0");
                pullLengthTextView.setText("0");
                pullWorkTextView.setText("0");
                releaseLengthTextView.setText("0");
                releaseWorkTextView.setText("0");
                possibleRotCountTextView.setText("0");

                ((RTActivity) getActivity()).initializeMeasurementStore();
                Timber.d("Resetting application");
                break;
        }
    }

    public void disableUIChangesForTesting() {
        this.disableUIChangesForUnitTesting = true;
    }

    public void enableMeasurementForTesting() {
        this.measurementEnabled = true;
    }

    public interface OnFragmentInteractionListener {
        void registerForceDataReceiver(ForceListener forceListener);
    }
}
