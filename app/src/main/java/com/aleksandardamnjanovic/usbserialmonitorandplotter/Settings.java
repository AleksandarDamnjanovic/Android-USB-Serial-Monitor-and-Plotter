package com.aleksandardamnjanovic.usbserialmonitorandplotter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


public class Settings extends Fragment {

    View view;
    EditText baudRateEditText;
    EditText readInterval;
    EditText readPackSize;
    EditText readTimeout;
    RadioButton delim0;
    RadioButton delim1;
    RadioButton delim2;
    Button settingsChangesApplyButton;
    Button settingsChangesRevertButton;
    TextView confirmationMessage;

    WebView aboutView;
    String about= "<h2>User manual and agreement</h2>" +
            "<p>Privacy policy, disclaimer and user agreement for this app you can find " +
            "<a href= \"https://kindspirittechnologyyt.on.drv.tw/USB%20Serial%20Monitor%20and%20Plotter%20Privacy%20Policy/USB%20Serial%20Monitor%20and%20Plotter%20Privacy%20policy.html\">" +
            "here</a></p>" +
            "<p>The author of this app have no responsibility for potential damage on your phone or connected device. " +
            "By using this app, it is presumed that you agree on this statement. Everything you do, you are doing on your" +
            " own personal responsibility. Use of electrical equipment always hold potential danger, therefore tread carefully " +
            "and responsibly.</p>"+
            "<p>The basic purpose of <b>USB Serial Monitor and Plotter</b> is to serve as Android tool for " +
            "communication, monitoring, and plotting of data received from ESP32, ESP8266, Arduino " +
            "and other supported microcontrollers over USB.</p>"+
            "<p>In order to use it, just connect microcontroller over USB to your phone and click on " +
            "<i>Connect</i>. Then give connection permission and you are good to go. If you experience communication " +
            "problems, check parameters in settings and try again. If problem is persistent, please contact " +
            "the author of this app.</p>"+
            "<p>In order to disconnect your device, either click on <i>Disconnect</i> or just plug out your connected device.</p>"+
            "<p>This app is built with help of two github projects. First one is " +
            "<a href=\"https://github.com/PhilJay/MPAndroidChart\">MPAndroidChart<a/> that is licensed under " +
            "<a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache 2.0</a> and second " +
            "<a href=\"https://github.com/mik3y/usb-serial-for-android\">usb-serial-for-android</a> " +
            "that is licensed under " +
            "<a href=\"https://opensource.org/license/mit\">MIT</a></p>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_settings, container, false);
        settingsChangesApplyButton = view.findViewById(R.id.settingsChangesApplyButton);
        settingsChangesRevertButton= view.findViewById(R.id.settingsChangesRevertButton);
        baudRateEditText = view.findViewById(R.id.baudRateEditText);
        readInterval = view.findViewById(R.id.readInterval);
        readPackSize = view.findViewById(R.id.readPackSize);
        readTimeout = view.findViewById(R.id.readTimeout);
        delim0= view.findViewById(R.id.delimiter_0);
        delim1= view.findViewById(R.id.delimiter_1);
        delim2= view.findViewById(R.id.delimiter_2);
        confirmationMessage = view.findViewById(R.id.confirmationText);
        aboutView = view.findViewById(R.id.about);

        aboutView.loadData(about, "text/html","UTF-8");

        settingsChangesApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baud = baudRateEditText.getText().toString();
                String inter = readInterval.getText().toString();
                String pack_size= readPackSize.getText().toString();
                String timeout_t= readTimeout.getText().toString();

                if(baud.length()==0 || inter.length()==0 || pack_size.length()==0 || timeout_t.length()==0){
                    confirmationMessage.setText("Changes not accepted\nFields can't be empty");
                    confirmationMessage.setTextColor(Color.RED);
                    confirmationMessage.setVisibility(View.VISIBLE);
                    return;
                }

                int delim_index=2;
                if(delim0.isChecked())
                    delim_index = 0;
                else if(delim1.isChecked())
                    delim_index = 1;
                else
                    delim_index = 2;

                if(MainActivity.isInteger(baud) && MainActivity.isInteger(inter) &&
                        MainActivity.isInteger(pack_size) && MainActivity.isInteger(timeout_t)){
                    SharedPreferences prefs = MainActivity.topContext.getSharedPreferences("values", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("baud", baud);
                    editor.apply();
                    MainActivity.baudRate = Integer.valueOf(baud);
                    editor.putString("interval", inter);
                    editor.apply();
                    MainActivity.interval = Integer.valueOf(inter);
                    editor.putString("packSize", pack_size);
                    editor.apply();
                    MainActivity.packSize = Integer.valueOf(pack_size);
                    editor.putString("timeout", timeout_t);
                    editor.apply();
                    MainActivity.timeOut = Integer.valueOf(timeout_t);
                    editor.putString("delimiter", String.valueOf(delim_index));
                    editor.apply();
                    MainActivity.delimiter = delim_index;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        confirmationMessage.setTextColor(Color.argb(1f,.15f,.75f,.15f));
                    }
                    confirmationMessage.setText("Changes accepted");
                    confirmationMessage.setVisibility(View.VISIBLE);
                }else{
                    confirmationMessage.setText("Changes not accepted\nAll values must be valid");
                    confirmationMessage.setTextColor(Color.RED);
                    confirmationMessage.setVisibility(View.VISIBLE);
                }

                if(delim_index==0)
                    delim0.setChecked(true);
                else if(delim_index==1)
                    delim1.setChecked(true);
                else
                    delim2.setChecked(true);

                MainActivity.disconnect.performClick();
                if(MainActivity.device!= null)
                    MainActivity.connect.performClick();
            }
        });

        settingsChangesRevertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = MainActivity.topContext.getSharedPreferences("values", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("baud", "9600");
                editor.apply();
                MainActivity.baudRate = 9600;
                baudRateEditText.setText("9600");
                editor.putString("interval", "250");
                editor.apply();
                MainActivity.interval = 250;
                readInterval.setText("250");
                editor.putString("packSize", "100");
                editor.apply();
                MainActivity.packSize = 100;
                readPackSize.setText("100");
                editor.putString("timeout", "2000");
                editor.apply();
                MainActivity.timeOut = 2000;
                readTimeout.setText("2000");
                editor.putString("delimiter", "2");
                editor.apply();
                MainActivity.delimiter = 2;

                delim0.setChecked(false);
                delim1.setChecked(false);
                delim2.setChecked(false);
                switch (MainActivity.delimiter){
                    case 0:
                        delim0.setChecked(true);
                        break;
                    case 1:
                        delim1.setChecked(true);
                        break;
                    case 2:
                        delim2.setChecked(true);
                        break;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    confirmationMessage.setTextColor(Color.argb(1f,.15f,.75f,.15f));
                }
                confirmationMessage.setText("All values set to default");
                confirmationMessage.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        confirmationMessage.setVisibility(View.INVISIBLE);
        SharedPreferences prefs= getContext().getSharedPreferences("values", MODE_PRIVATE);
        baudRateEditText.setText(prefs.getString("baud","9600"));
        readInterval.setText(prefs.getString("interval","250"));
        readPackSize.setText(prefs.getString("packSize", "100"));
        readTimeout.setText(prefs.getString("timeout", "2000"));
        delim0.setChecked(false);
        delim1.setChecked(false);
        delim2.setChecked(false);
        switch (MainActivity.delimiter){
            case 0:
                delim0.setChecked(true);
                break;
            case 1:
                delim1.setChecked(true);
                break;
            case 2:
                delim2.setChecked(true);
                break;
        }

        view.invalidate();
    }
}