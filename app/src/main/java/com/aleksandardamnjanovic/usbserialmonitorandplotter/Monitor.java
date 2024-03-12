package com.aleksandardamnjanovic.usbserialmonitorandplotter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;


public class Monitor extends Fragment {

    View view;
    WebView monitorTextView;
    String sourceText="";

    public static boolean running= false;
    ScrollView scrollView;
    EditText monitorDataSend;
    Button monitorButtonSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_monitor, container, false);
        monitorTextView = view.findViewById(R.id.monitorTextView);
        scrollView= view.findViewById(R.id.scrollView);
        monitorDataSend = view.findViewById(R.id.monitorSendData);
        monitorButtonSend = view.findViewById(R.id.monitorSendButton);

        monitorButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!MainActivity.port.isOpen())
                    return;

                String message= monitorDataSend.getText().toString();
                monitorDataSend.setText("");
                monitorTextView.requestFocus();

                synchronized (MainActivity.port){
                    try {
                        MainActivity.port.write(message.getBytes(), MainActivity.timeOut);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });

        monitorDataSend.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    pause= true;
                else
                    pause= false;
            }
        });

        return view;
    }

    boolean pause= false;
    @Override
    public void onResume() {
        super.onResume();
        monitorTextView.loadData(sourceText, "text/plain","UTF-8");

        running = false;
        try {
            Thread.sleep((long)(MainActivity.interval * 1.1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(MainActivity.port == null)
            return;

        running= true;

        if(MainActivity.port.isOpen()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int breaker= 45;

                    boolean open= false;
                    synchronized (MainActivity.port){
                        if(MainActivity.port!=null)
                            if(MainActivity.port.isOpen())
                                open= true;
                    }

                    while(running && open){

                        if(pause){
                            while(pause){
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            continue;
                        }

                        byte[] pack= new byte[MainActivity.packSize];
                        try {
                            synchronized (MainActivity.port){
                                MainActivity.port.read(pack, MainActivity.timeOut);
                            }
                            String message = new String(pack, "UTF-8").trim();

                            String d= "";
                            if(sourceText.equals("")){
                                sourceText= message;
                            }else{
                                switch (MainActivity.delimiter){
                                    case 0:
                                        d= "\n";
                                        break;
                                    case 1:
                                        d="-";
                                        break;
                                    case 2:
                                        d="";
                                        break;
                                }
                                sourceText = sourceText + d + message;
                            }

                            int len = sourceText.length();
                            if(len > breaker){
                                breaker+=45;
                                sourceText= sourceText+ "\n";
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    monitorTextView.loadData(sourceText, "text/plain","UTF-8");
                                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });

                            Thread.sleep(MainActivity.interval);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
    }
}