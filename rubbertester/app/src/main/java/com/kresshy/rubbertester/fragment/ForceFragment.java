package com.kresshy.rubbertester.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
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
import com.kresshy.rubbertester.force.ForceData;
import com.kresshy.rubbertester.force.ForceListener;
import com.kresshy.rubbertester.force.ForceMeasurement;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class ForceFragment extends Fragment implements ForceListener, View.OnClickListener {
    private static final String TAG = "ForceFragment";

    private LinearLayout bottomLayout;

    private LineGraphView forceGraph;
    private GraphViewData[] forceData;
    private GraphViewSeries forceSeries;

    private TextView pullForceTextView;

    private Button startButton;
    private Button stopButton;
    private Button maxForceReachedButton;

    private boolean measurementActivated = false;

    private int measurementCount = 1;
    private int numberOfSamples = 300;
    private boolean maximumForce = false;
    private int lastCount = 0;

    private List<ForceData> forceStorage;

    final private int yellowColor = Color.YELLOW;
    final private int redColor = Color.RED;

    final private int flashingFrequency = 100;

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

        LinearLayout forceGraphContainer = (LinearLayout) view.findViewById(R.id.forceGraphContainer);
        pullForceTextView = (TextView) view.findViewById(R.id.pullForceText);

        startButton = (Button) view.findViewById(R.id.forceStartButton);
        stopButton = (Button) view.findViewById(R.id.forceStopButton);
        maxForceReachedButton = (Button) view.findViewById(R.id.forceMaxButton);

        bottomLayout = (LinearLayout) view.findViewById(R.id.forceGraphBackground);

        forceStorage = new ArrayList<>();

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
        if (measurementActivated) {
            handleIncomingMeasurement(forceMeasurement);
        }
    }

    private void handleIncomingMeasurement(ForceMeasurement forceMeasurement) {
        // store all measurements for serialization and other calculations
        forceStorage.addAll(forceMeasurement.getMeasurements());

        if (!maximumForce) {
            for (ForceData data : forceMeasurement.getMeasurements()) {
                forceSeries.appendData(new GraphViewData(
                        data.getCount(),
                        data.getForce()
                ), true, numberOfSamples);
                lastCount += data.getCount();

                if (data.getForce() >= 4000) {
                    maximumForce = true;
                }
            }
        } else {
            for (ForceData data : forceMeasurement.getMeasurements()) {
                lastCount -= data.getCount();
                forceSeries.appendData(new GraphViewData(
                        lastCount,
                        data.getForce()
                ), true, numberOfSamples);
            }
        }


        pullForceTextView.setText(
                forceMeasurement.getMeasurements().get(
                        forceMeasurement.getMeasurements().size() - 1)
                        .getForce() + " gramms"
        );

        measurementCount++;
    }

    private void createViewForForceGraph(LinearLayout container) {
        Timber.d("Creating GraphView For WindSpeed");
        forceGraph = new LineGraphView(getActivity().getApplicationContext(), "Wind Speed");
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
        return new String[]{"40", "35", "30", "25", "20", "15", "10", "5", "0"};
    }

    private GraphViewStyle getGraphViewStyle() {
        GraphViewStyle graphViewStyle = new GraphViewStyle(Color.BLACK, Color.BLACK, Color.GRAY);
        graphViewStyle.setVerticalLabelsAlign(Paint.Align.LEFT);
        graphViewStyle.setVerticalLabelsWidth(80);
        return graphViewStyle;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.forceStartButton:
                measurementActivated = true;
                break;
            case R.id.forceStopButton:
                measurementActivated = false;
                break;
            case R.id.forceMaxButton:
                maximumForce = true;
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void registerForceDataReceiver(ForceListener forceListener);
    }
}
