package com.kresshy.rubbertester.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
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
import com.kresshy.rubbertester.force.ForceCalculator;
import com.kresshy.rubbertester.force.ForceData;
import com.kresshy.rubbertester.force.ForceListener;
import com.kresshy.rubbertester.force.ForceMeasurement;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


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
    private TextView pullWork;
    private TextView releaseWork;

    private Button startButton;
    private Button stopButton;
    private Button resetButton;
    private Button maxForceReachedButton;

    private boolean measurementEnabled = false;

    private int measurementCount = 0;
    private int releaseMeasurementCount = 0;
    private int numberOfSamples = 7000;
    private int maximumForce = 35000; // grams
    private boolean maximumForceReached = false;
    private boolean enableSounds = true;

    private List<ForceMeasurement> pullForceMeasurementList;
    private List<ForceMeasurement> releaseForceMeasurementList;

    private ForceCalculator pullForceCalculator;
    private ForceCalculator releaseForceCalculator;

    final private int yellowColor = Color.YELLOW;
    final private int redColor = Color.RED;
    final private int lightGreyColor = Color.LTGRAY;

    final private double leap = 10.825;

    private OnFragmentInteractionListener mListener;

    public ForceFragment() {
        // Required empty public constructor
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
        pullForceMeasurementList = new ArrayList<>();
        releaseForceMeasurementList = new ArrayList<>();
        pullForceCalculator = new ForceCalculator(pullForceMeasurementList);
        releaseForceCalculator = new ForceCalculator(releaseForceMeasurementList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_force, container, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()
        );

        // keep screen on
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        forceGraphContainer = (LinearLayout) view.findViewById(R.id.forceGraphContainer);
        pullForceTextView = (TextView) view.findViewById(R.id.pullForceText);
        pullLengthTextView = (TextView) view.findViewById(R.id.pullLengthText);
        releaseLengthTextView = (TextView) view.findViewById(R.id.releaseLengthText);
        pullWork = (TextView) view.findViewById(R.id.pullWork);
        releaseWork = (TextView) view.findViewById(R.id.releaseWork);

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

    private void handleIncomingMeasurement(ForceMeasurement forceMeasurement) {
        Timber.d("Handling Incoming measurement");
        // store all measurements for serialization and other calculations
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

            colorBackgroundOnForce(data.getForce());
            alertSoundOnForce(data.getForce());

            if (data.getForce() >= maximumForce) {
                maximumForceReached = true;
                pullWork.setText(
                        getStringValueInKg(
                                getValueInCm(
                                        pullForceCalculator.calculateMaximumWorkLoad()
                                )
                        ));
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
                releaseWork.setText(
                        getStringValueInKg(
                                getValueInCm(
                                        releaseForceCalculator.calculateMaximumWorkLoad()
                                )
                        ));
                Timber.d("Stopping measurement and calculating release work");
                break;
            case R.id.forceMaxButton:
                maximumForceReached = true;
                pullWork.setText(
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

                pullForceCalculator = new ForceCalculator(pullForceMeasurementList);
                releaseForceCalculator = new ForceCalculator(releaseForceMeasurementList);

                measurementCount = 0;
                releaseMeasurementCount = 0;
                maximumForceReached = false;

                ((RTActivity) getActivity()).initializeMeasurementStore();
                Timber.d("Resetting application");
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void registerForceDataReceiver(ForceListener forceListener);
    }
}
