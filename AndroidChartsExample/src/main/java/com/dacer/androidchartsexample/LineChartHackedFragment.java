package com.dacer.androidchartsexample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dacer.androidcharts.LineView;

import java.util.ArrayList;

/**
 * @author dector
 */
public class LineChartHackedFragment extends Fragment {

    private static final int DATASETS_COUNT = 4;
    private static final int DATA_LENGTH = 10;

    private LineView normalChartView;
    private LineView hackedChartView;
    private Button randomizeButton;

    private ArrayList<ArrayList<Integer>> data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_line_chart_hacked, container, false);

        normalChartView = (LineView) rootView.findViewById(R.id.chart_normal);
        hackedChartView = (LineView) rootView.findViewById(R.id.chart_hacked);

        buildData();

        initLineView(normalChartView);
        initLineView(hackedChartView);

        randomizeButton = (Button) rootView.findViewById(R.id.btnRandomize);
        randomizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildData();
                hackedChartView.setDataList(data);
                normalChartView.setDataList(data);
            }
        });

        return rootView;
    }

    private void initLineView(LineView lineView) {
        ArrayList<String> test = new ArrayList<String>();
        for (int i = 0; i < DATA_LENGTH; i++) {
            test.add("" + (i + 1));
        }

        lineView.setBottomTextList(test);
        lineView.setDrawDotLine(true);
        lineView.setDataList(data);
    }

    private void buildData() {
        data = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < DATASETS_COUNT; i++) {
            data.add(buildRandomList());
        }
    }

    private ArrayList<Integer> buildRandomList() {
        ArrayList<Integer> result = new ArrayList<Integer>();

        final int value1 = (int)(Math.random() * 9 + 1);
        for (int i = 0; i < DATA_LENGTH; i++) {
            result.add((int) (Math.random() * value1));
        }

        return result;
    }
}
