//package dtcsfetching;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothSocket;
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Set;
//import java.util.UUID;
//
//public class Bluetooth {
//    private static final UUID SPPUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    private static final String TAG = BluetoothManager.class.getName();
//    private BluetoothAdapter bluetoothAdapter = null;
//    private BluetoothSocket socket = null;
//    private String userDeviceAddress = null;
//    private String userDeviceName = null;
//
//    public boolean setupBluetooth() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        return bluetoothAdapter != null;
//    }
//    public boolean isBluetoothEnabled() {
//        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
//    }
//    public Set<BluetoothDevice> getPairedDeviceSet() {
//        if (bluetoothAdapter != null) {
//            return bluetoothAdapter.getBondedDevices();
//        } else {
//            return null;
//        }
//    }
//    public synchronized boolean connectBluetoothSocket(final String userDeviceAddress, final String userDeviceName, final int timeout_ms, final ObdProtocols obd_protocol) {
//
//        if (this.userDeviceAddress == userDeviceAddress && socket != null && socket.isConnected()) {
//            return true;
//        }
//
//        closeBluetoothSocket();
//        this.userDeviceAddress = userDeviceAddress;
//        this.userDeviceName = userDeviceName;
//
//        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
//        final BluetoothDevice device = btAdapter.getRemoteDevice(userDeviceAddress);
//        Log.d(TAG, "Starting Bluetooth connection..");
//        try {
//            socket = device.createRfcommSocketToServiceRecord(Bluetooth.SPPUUID);
//        } catch (Exception e) {
//            Log.e("Bluetooth Connection", "Socket couldn't be created");
//            e.printStackTrace();
//            return false;
//        }
//        try {
//            socket.connect();
//            socketConnected(timeout_ms, obd_protocol);
//
//            return true;
//        } catch (Exception e) {
//            //e.printStackTrace();
//            Log.e("BT Connection failed:", e.getMessage());
//            return false;
//        }
//
//    }
//
//    public synchronized void closeBluetoothSocket() {
//        final OutputStream outs = getOutputStream();
//        if (outs != null) {
//            try {
//                outs.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        final InputStream ins = getInputStream();
//        if (ins != null) {
//            try {
//                ins.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (socket != null) {
//            try {
//                if (socket.isConnected()) {
//                    socket.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            socket = null;
//            this.userDeviceAddress = null;
//        }
//        System.out.println("===============================> CLOSING BLUETOOTH SOCKET");
//
//    }
//
//
//    public boolean isConnected()  {
//        return (socket != null && socket.isConnected());
//    }
//
//
//    public void disconnect() {
//        this.closeBluetoothSocket();
//    }
//
//
//    protected OutputStream getOutputStream() {
//        if (socket != null) {
//            try {
//                return socket.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        } else {
//            return null;
//        }
//    }
//
//
//    protected InputStream getInputStream() {
//        if (socket != null) {
//            try {
//                return socket.getInputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        } else {
//            return null;
//        }
//    }
//
//    public String getUserDeviceAddress() {
//        return userDeviceAddress;
//    }
//
//    public String getUserDeviceName() {
//        return userDeviceName;
//    }
//
//
//}
//
//
//
//
