package com.aleksandardamnjanovic.usbserialmonitorandplotter;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;


public class Plotter extends Fragment {

    View view;
    LineData data;
    LineDataSet f;
    LineChart lineChart;
    public static boolean running= false;
    EditText plotterSendData;
    Button plotterSendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_plotter, container, false);

        plotterSendButton = view.findViewById(R.id.plotterSendButton);
        plotterSendData= view.findViewById(R.id.plotterSendData);
        lineChart = view.findViewById(R.id.lineChart);

        plotterSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = plotterSendData.getText().toString();
                plotterSendData.setText("");
                synchronized (MainActivity.port){
                    try {
                        MainActivity.port.write(message.getBytes(), MainActivity.timeOut);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("USB Serial Data Plotter");
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);

        data = new LineData();
        data.setValueTextColor(Color.BLACK);
        lineChart.setData(data);

        LineDataSet set = new LineDataSet(null, "first set");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1.5f);
        set.setColor(Color.GREEN);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(.1f);
        data.addDataSet(set);
        f= (LineDataSet) data.getDataSetByLabel("first set", true);
        data.notifyDataChanged();

        return view;
    }

    int counter=0;
    @Override
    public void onResume() {
        super.onResume();

        running = false;
        try {
            Thread.sleep((long)(MainActivity.interval * 1.1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter= 0;
        running= true;

        if(MainActivity.port == null)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean open= false;
                synchronized (MainActivity.port){
                    if(MainActivity.port!=null)
                        if(MainActivity.port.isOpen())
                            open= true;
                }

                while (open && running) {

                    byte[] message= new byte[MainActivity.packSize];
                    try {
                        synchronized (MainActivity.port){
                            MainActivity.port.read(message, MainActivity.interval);
                        }
                        int value = -1;
                        String t = new String(message, "UTF-8").trim();
                        if(MainActivity.isInteger(t) && t.length()>0) {

                            if(counter>20){
                                f.removeEntryByXValue(f.getEntryForIndex(0).getX());
                            }

                            value= Integer.valueOf(t);
                            f.addEntry(new Entry(counter, value));
                            counter +=1;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    data.removeDataSet(0);
                                    data.addDataSet(f);
                                    lineChart.setData(data);
                                    lineChart.moveViewToX(counter);
                                    data.notifyDataChanged();
                                    lineChart.invalidate();
                                    lineChart.animate();
                                }
                            });
                            Thread.sleep(MainActivity.interval);
                        }
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    open= false;
                    synchronized (MainActivity.port){
                        if(MainActivity.port!=null)
                            if(MainActivity.port.isOpen())
                                open= true;
                    }

                }
            }
        }).start();

    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
    }

}