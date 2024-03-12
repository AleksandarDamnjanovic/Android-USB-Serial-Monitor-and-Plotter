package com.aleksandardamnjanovic.usbserialmonitorandplotter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    public static UsbDeviceConnection connection;
    public static List<UsbSerialDriver> availableDrivers;
    public static UsbSerialDriver driver;
    public static UsbSerialPort port;
    public static UsbManager manager;
    public static Map<String, UsbDevice> devices;
    public static UsbDevice device = null;
    public static PendingIntent pi;
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private String metaData = "";
    private int fragmentIndex = 1;
    public static int baudRate = 9600;
    public static int interval = 250;
    public static int packSize = 100;
    public static int timeOut = 2000;
    public static int delimiter = 2;
    public static Context topContext;
    public static Activity activity;
    private float x1, x2;
    private static int DISTANCE = 150;
    private GestureDetector gestureDetector;


    public static boolean isInteger(String text) {
        char[] chars = text.toCharArray();
        for (char c : chars)
            if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7' && c != '8' && c != '9')
                return false;

        return true;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {
                    if (manager.hasPermission(device))
                        connect();
                }
            }
        }
    };

    private final BroadcastReceiver detachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                Monitor.running = false;
                Plotter.running = false;

                if (port != null)
                    if (port.isOpen()) {
                        try {
                            port.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        connection.close();
                    }

            }
        }
    };

    private void connect() {
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        driver = availableDrivers.get(0);
        connection = manager.openDevice(driver.getDevice());
        port = driver.getPorts().get(0);

        try {
            synchronized (port) {
                port.open(connection);
                port.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        metaData = "Manufacturer name: " + device.getManufacturerName() + "\n" +
                "Device name: " + device.getDeviceName() + "\n" +
                "Device version: " + device.getVersion() + "\n" +
                "Product name: " + device.getProductName() + "\n" +
                "Serial number: " + device.getSerialNumber();

        changeFragment();
    }

    public static TextView monitorButton, plotterButton, metaButton, settingsButton, connect, disconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monitorButton = findViewById(R.id.MonitorButton);
        plotterButton = findViewById(R.id.PlotterButton);
        metaButton = findViewById(R.id.MetaButton);
        settingsButton = findViewById(R.id.SettingsButton);
        connect = findViewById(R.id.connect);
        disconnect = findViewById(R.id.disconnect);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(receiver, filter);
        IntentFilter fil = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(detachReceiver, fil);

        manager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);

        SharedPreferences prefs = getSharedPreferences("values", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String baud = prefs.getString("baud", "9600");
        baudRate = Integer.valueOf(baud);
        String inter = prefs.getString("interval", "250");
        interval = Integer.valueOf(inter);
        String pack_size = prefs.getString("packSize", "100");
        packSize = Integer.valueOf(pack_size);
        String message_timeout = prefs.getString("timeout", "2000");
        timeOut = Integer.valueOf(message_timeout);
        String delimiter_code = prefs.getString("delimiter", "2");
        delimiter = Integer.valueOf(delimiter_code);

        topContext = this;
        activity = MainActivity.activity;

        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentIndex = 1;
                changeFragment();
                renderTabs();
            }
        });

        plotterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentIndex = 2;
                changeFragment();
                renderTabs();
            }
        });

        metaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentIndex = 3;
                changeFragment();
                renderTabs();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentIndex = 4;
                changeFragment();
                renderTabs();
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices = manager.getDeviceList();
                for (Map.Entry<String, UsbDevice> entry : devices.entrySet())
                    device = entry.getValue();

                if (device == null || devices.size()==0)
                    return;

                pi = PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                manager.requestPermission(device, pi);
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectVoid();
            }
        });

        this.gestureDetector = new GestureDetector(MainActivity.this, this);

        settingsButton.performClick();
    }

    private void changeFragment(){
        FragmentManager m = getSupportFragmentManager();
        FragmentTransaction t = m.beginTransaction();
        switch (fragmentIndex){
            case 1:
                t.replace(R.id.window, new Monitor());
                t.commit();
                break;
            case 2:
                t.replace(R.id.window, new Plotter());
                t.commit();
                break;
            case 3:
                Meta meta = new Meta();
                Bundle b = new Bundle();
                b.putString("text", metaData);
                meta.setArguments(b);
                t.replace(R.id.window, meta);
                t.commit();
                break;
            case 4:
                t.replace(R.id.window, new Settings());
                t.commit();
                break;
        }
    }

    private void renderTabs(){
        monitorButton.setBackground(getDrawable(R.drawable.tab_list));
        plotterButton.setBackground(getDrawable(R.drawable.tab_list));
        metaButton.setBackground(getDrawable(R.drawable.tab_list));
        settingsButton.setBackground(getDrawable(R.drawable.tab_list));
        monitorButton.setTextColor(Color.BLACK);
        plotterButton.setTextColor(Color.BLACK);
        metaButton.setTextColor(Color.BLACK);
        settingsButton.setTextColor(Color.BLACK);

        switch (fragmentIndex){
            case 1:
                monitorButton.setTextColor(Color.WHITE);
                monitorButton.setBackground(getDrawable(R.drawable.tab_selected));
                break;
            case 2:
                plotterButton.setTextColor(Color.WHITE);
                plotterButton.setBackground(getDrawable(R.drawable.tab_selected));
                break;
            case 3:
                metaButton.setTextColor(Color.WHITE);
                metaButton.setBackground(getDrawable(R.drawable.tab_selected));
                break;
            case 4:
                settingsButton.setTextColor(Color.WHITE);
                settingsButton.setBackground(getDrawable(R.drawable.tab_selected));
                break;
        }
    }

    private void swipe(boolean left) {

        if (left) {
            if (fragmentIndex > 1)
                fragmentIndex -= 1;
            else
                return;
        }else {
            if (fragmentIndex < 4)
                fragmentIndex += 1;
            else
                return;
        }

        switch (fragmentIndex) {
            case 1:
                monitorButton.performClick();
                break;
            case 2:
                plotterButton.performClick();
                break;
            case 3:
                metaButton.performClick();
                break;
            case 4:
                settingsButton.performClick();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();

                float disX = x2 - x1;

                if (disX < 0 && Math.abs(disX) > DISTANCE)
                    swipe(false);
                else if (disX > 0 && Math.abs(disX) > DISTANCE)
                    swipe(true);

                break;
        }

        return true;
    }

    private void disconnectVoid() {

        if(port== null)
            return;

        try {
            synchronized (port) {

                if (port == null)
                    return;

                if (port.isOpen()) {
                    Monitor.running = false;
                    Plotter.running = false;
                    port.close();
                    connection.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}