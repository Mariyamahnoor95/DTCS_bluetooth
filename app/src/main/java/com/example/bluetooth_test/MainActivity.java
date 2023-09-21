package com.example.bluetooth_test;

import com.example.bluetooth_test.BluetoothClientThread;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Handler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.github.pires.obd.exceptions.UnsupportedCommandException;
import com.github.pires.obd.exceptions.UnknownErrorException;

public class MainActivity extends AppCompatActivity {
    // An arbitrary request code for enabling Bluetooth
    private static final UUID SPPUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothSocket socket = null;
    private Handler handler;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    //    private TextView dtcTextView;
    private PrintWriter logWriter;
    private static final String LOG_TAG = "DTCApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// dtcTextView = findViewById(R.id.dtcTextView);
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_ENABLE_BT);
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Clear list
        pairedDevicesArrayAdapter.clear();

        // Check if the BLUETOOTH permission is granted
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            // Query paired devices and add to the list
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        } else {
            // Request the BLUETOOTH permission
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_ENABLE_BT);
        }
    }

    private final AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Do something when a device is clicked
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create a BluetoothDevice object for the clicked device
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            Log.d(TAG, "Starting Bluetooth connection..");
            connectToDevice(device);
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted, yay! You can now call Bluetooth.
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                } else {

                    // Permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to enable Bluetooth", Toast.LENGTH_SHORT).show();

                }
                return;
            }

        }
    }

    private void connectToDevice(final BluetoothDevice device) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a BluetoothSocket
                    if (checkSelfPermission( Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPPUUID);
                    socket.connect();
                    Log.d(LOG_TAG, "Connected to device: " + device.getName());

                    // Send OBD II commands to request DTCs (replace these with actual commands)
                    sendOBDCommand(socket, "ATZ");     // Reset the OBD II device
                    sendOBDCommand(socket, "ATSP0");   // Set OBD II protocol to automatic

                    // Send the command to retrieve DTCs (replace with the appropriate command)
                    String dtcCommand = "ATH1";
                    sendOBDCommand(socket, dtcCommand);

                    // Read and process the response
                    String dtcResponse = readOBDResponse(socket);
                    Log.d(LOG_TAG, "DTCs: " + dtcResponse);

                    // Close the socket when done
                    socket.close();
                } }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();



    }
    private void sendOBDCommand(BluetoothSocket socket, String command) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write((command + "\r").getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readOBDResponse(BluetoothSocket socket) {
        StringBuilder response = new StringBuilder();
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String data = new String(buffer, 0, bytesRead);
                response.append(data);
                if (data.contains(">")) {
                    break;  // End of response
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }



    private void fetchDTCs(BluetoothSocket socket) {
        try {

            // Get the input and output streams
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Create a new TroubleCodesCommand
            TroubleCodesCommand troubleCodesCommand = new TroubleCodesCommand();

            // Run the TroubleCodesCommand
            troubleCodesCommand.run(inputStream, outputStream);

            // Get the result
            String result = troubleCodesCommand.getFormattedResult();

            // TODO: Do something with the result
            System.out.println("DTCs: " + result);

        } catch (IOException | InterruptedException | NoDataException | UnsupportedCommandException | UnableToConnectException | UnknownErrorException e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }

    private void logMessage (String message ){
        Log.d(LOG_TAG , message);
        if(logWriter != null){
            logWriter.println(message);
            logWriter.flush();

        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        // Start a thread to listen for incoming data
        new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];  // buffer store for the stream
                int bytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        // Read from the InputStream
                        bytes = inputStream.read(buffer);
                        // Send the obtained bytes to the UI activity
                        // mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    } catch (IOException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Start a thread to send data
        new Thread(() -> {
            try {
                OutputStream outputStream = socket.getOutputStream();
                String message = "Hello, Bluetooth!";
                outputStream.write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
