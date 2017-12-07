package com.example.derrick.webviewpoc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by derrick on 2017-12-04.
 */

public class WebApplicationInterface {

    private Context mContext;
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;

    int bluetoothRequestCode = 100;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    WebApplicationInterface(Context context) {
        this.mContext = context;
    }

    @JavascriptInterface
    public void connectPrinter() {
        try {
            findBT();
            openBT();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void printText(String text) {
        sendData(text);
    }

    @JavascriptInterface
    public void closePrinterConnection() {
        try {
            closeBT();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // close the connection to bluetooth printer.
    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mOutputStream.close();
            mInputStream.close();
            mSocket.close();
            toast("Bluetooth Closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendData(String text) {
        try {
            byte[] mFormat = new byte[]{27, 33, 0};
            byte[] format = new byte[]{ 27, 69, 1 };
            mFormat[2] = ((byte) (0x8 | mFormat[2]));
            mOutputStream.write(format);
            // the text typed by the user
            String msg = text;
            msg += "\n";

            mOutputStream.write(msg.getBytes());
            mFormat = new byte[]{27, 33, 0};
            mOutputStream.write(mFormat);

            mOutputStream.write(msg.getBytes());
            // tell the user data were sent
            toast("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void findBT() {
        try {
            mAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mAdapter == null) {
                return;
            }

            if(!mAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) mContext).startActivityForResult(enableBluetooth, bluetoothRequestCode);
            } else {
                findBTComplete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    public void findBTComplete() {
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d("poc-app", device.getName());
                if (device.getName().equals("BlueTooth Printer")) {
                    mDevice = device;
                    Log.d("poc-app", "printer found");
                    toast("Bluetooth device found");
                    break;
                }
            }
        }
    }

    // tries to open a connection to the bluetooth printer device
    void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            beginListenForData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
 * after opening a connection to bluetooth printer device,
 * we have to listen and check if a data were sent to be printed.
 */
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                toast("data sent");
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @JavascriptInterface
    public String getString() {
        return "this is some string value here";
    }

    @JavascriptInterface
    public String getMaths(String first) {
        Double x = Double.parseDouble(first);
        Double result = x * 1.2 + 5.0;
        return result.toString();
    }

    @JavascriptInterface
    public void toast(String message) {
        Toast.makeText(this.mContext, message, Toast.LENGTH_SHORT).show();
    }
}
