/*
 * Filename:   MainActivity.java
 *
 * Copyright (C) 2016 Texas Instruments Incorporated - http://www.ti.com/
 *
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *    Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *    Neither the name of Texas Instruments Incorporated nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
*/

package com.example.ti.bleprojectzero;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static Activity activity = null;

    private boolean mScanning = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings mScanSettings;
    private BluetoothGatt mBluetoothGatt;
    private ArrayList<ScanFilter> mScanFilters = new ArrayList<>();
    private Map<BluetoothDevice, Integer> mBtDevices = new HashMap<>();
    private TableLayout mTableDevices;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;


    // Tag used for logging
    private static final String TAG = "MainActivity";

    // Request codes
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE = 1;
    private final static int MY_PERMISSIONS_REQUEST_ENABLE_BT = 2;

    // Intent actions
    public final static String ACTION_GATT_CONNECTED =
            "com.example.ti.bleprojectzero.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.ti.bleprojectzero.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.ti.bleprojectzero.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.ti.bleprojectzero.ACTION_DATA_AVAILABLE";
    public final static String ACTION_WRITE_SUCCESS =
            "com.example.ti.bleprojectzero.ACTION_WRITE_SUCCESS";

    // Intent extras
    public final static String EXTRA_DATA =
            "com.example.ti.bleprojectzero.EXTRA_DATA";
    public final static String EXTRA_LED0 =
            "com.example.ti.bleprojectzero.EXTRA_LED0";
    public final static String EXTRA_LED1 =
            "com.example.ti.bleprojectzero.EXTRA_LED1";
    public final static String EXTRA_BUTTON0 =
            "com.example.ti.bleprojectzero.EXTRA_BUTTON0";
    public final static String EXTRA_BUTTON1 =
            "com.example.ti.bleprojectzero.EXTRA_BUTTON1";

    // Queue for writing descriptors
    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<>();
    private Queue<BluetoothGattCharacteristic> characteristicQueue = new LinkedList<>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        // Get UI elements
        mTableDevices = (TableLayout) findViewById(R.id.devicesFound);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //  For Android M: Check if app have location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Show explanation on why this is needed
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("This app needs location access");
                    builder.setMessage("Please grant location access so this app can discover bluetooth devices");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
                        }
                    });
                    builder.show();

                } else {
                    // Prompt user for location access
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
                }
            }
        }

        // Configure scan filter on device name, so the scan result
        // only displays devices with Project Zero running.
        ScanFilter filter = new ScanFilter.Builder()
                .setDeviceName("Lockbox")
                .build();
        mScanFilters.add(filter);

        // Configure default scan settings
        mScanSettings = new ScanSettings.Builder().build();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not available or disabled. Display
            // a dialog requesting user permission to enable Bluetooth.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MY_PERMISSIONS_REQUEST_ENABLE_BT);
        } else if (!mScanning) {
            // Start scanning
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mScanning) {
            // Stop scanning
            scanLeDevice(false);
            FirebaseAuth.getInstance().signOut();       //logout user
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // Callback have been received from a permission request
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Coarse location permission granted");
                } else {
                    // Access location was not granted. Display a warning.
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not display any bluetooth scan results.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_PERMISSIONS_REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // Bluetooth was not enabled, end activity
                finish();
                return;
            }
        }
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Connect to a bluetooth device. The connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     *
     * @param btDevice instance of device to connect to.
     */
    public void connectToDevice(BluetoothDevice btDevice) {

        if (mBluetoothGatt == null) {
            mBluetoothGatt = btDevice.connectGatt(this, false, mGattCallback);

            // Stop scanning
            if (mScanning) {
                scanLeDevice(false);
            }
        }
    }

    /**
     * Disconnect from a device. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * Retrieve a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }

        return mBluetoothGatt.getServices();
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }

        // Queue the characteristic to read, since several reads are done on startup
        characteristicQueue.add(characteristic);

        // If there is only 1 item in the queue, then read it. If more than 1, it is handled
        // asynchronously in the callback
        if ((characteristicQueue.size() == 1)) {
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to write to.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enable or disable notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enable         If true, enable notification. Otherwise, disable it.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enable) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }
        // Enable/disable notification
        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);

        // Write descriptor for notification
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(ProjectZeroAttributes.UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR);
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
        writeGattDescriptor(descriptor);

    }

    /**
     * Scan for bluetooth devices
     */
    private void scanLeDevice(final boolean enable) {

        if (mLEScanner == null) {
            Log.d(TAG, "Could not get LEScanner object");
            return;
        }

        if (enable) {
            // Clear list of scanned devices
            mBtDevices.clear();
            mScanning = true;
            mLEScanner.startScan(mScanFilters, mScanSettings, mScanCallback);
        } else {
            mScanning = false;
            mLEScanner.stopScan(mScanCallback);
        }
    }

    /**
     * Bluetooth gatt callback function
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /**
         * Enable notifications on the button clicks
         */
        private void enableButtonNotifications(BluetoothGatt gatt) {
            // Loop through the characteristics for the button service
            for (BluetoothGattCharacteristic characteristic : gatt.getService(ProjectZeroAttributes.UUID_BUTTON_SERVICE).getCharacteristics()) {
                // Enable notification on the characteristic
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    setCharacteristicNotification(characteristic, true);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange. Status: " + status);
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    intentAction = ACTION_GATT_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback", "STATE_DISCONNECTED");
                    intentAction = ACTION_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    // Close connection completely after disconnect, to be able
                    // to start clean.
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "onServicesDiscovered: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Broadcast that services has successfully been discovered
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                // Enable notification on button services
                enableButtonNotifications(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received with error: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {

            // Read action has finished, remove from queue
            characteristicQueue.remove();

            // Broadcast the results
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            } else {
                Log.d(TAG, "onCharacteristicRead error: " + status);
            }

            // Handle the next element from the queues
            if (characteristicQueue.size() > 0)
                mBluetoothGatt.readCharacteristic(characteristicQueue.element());
            else if (descriptorWriteQueue.size() > 0)
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Broadcast data written to the data service string characteristic
                if ((UUID.fromString(ProjectZeroAttributes.STRING_CHAR)).equals(characteristic.getUuid())) {
                    broadcastUpdate(ACTION_WRITE_SUCCESS);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // Broadcast the received notification
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Callback: Error writing GATT Descriptor: " + status);
            }

            // Pop the item that we just finishing writing
            descriptorWriteQueue.remove();

            // Continue handling items if there is more in the queues
            if (descriptorWriteQueue.size() > 0)
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
            else if (characteristicQueue.size() > 0)
                mBluetoothGatt.readCharacteristic(characteristicQueue.element());
        }

        ;

    };

    /**
     * Device scan callback
     */
    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            final BluetoothDevice btDevice = result.getDevice();
            if (btDevice == null) {
                Log.e("ScanCallback", "Could not get bluetooth device");
                return;
            }

            // Check if device already added to list of scanned devices
            String macAddress = btDevice.getAddress();
            for (BluetoothDevice dev : mBtDevices.keySet()) {
                // Device already added, do nothing
                if (dev.getAddress().equals(macAddress)) {
                    return;
                }
            }

            // Add device to list of scanned devices
            mBtDevices.put(btDevice, result.getRssi());

            // Update the device table with the new device
            updateDeviceTable();
        }
    };

    /**
     * Update the table view displaying all scanned devices.
     * This function will clean the current table view and re-add all items that has been scanned
     */
    private void updateDeviceTable() {

        // Clean current table view
        mTableDevices.removeAllViews();

        for (final BluetoothDevice savedDevice : mBtDevices.keySet()) {

            // Get RSSI of this device
            int rssi = mBtDevices.get(savedDevice);

            // Create a new row
            final TableRow tr = new TableRow(MainActivity.this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            // Add Text view for rssi
            TextView tvRssi = new TextView(MainActivity.this);
            tvRssi.setText(Integer.toString(rssi));
            TableRow.LayoutParams params = new TableRow.LayoutParams(0);
            params.setMargins(20, 0, 20, 0);
            tvRssi.setLayoutParams(params);

            // Add Text view for device, displaying name and address
            TextView tvDevice = new TextView(MainActivity.this);
            tvDevice.setText(savedDevice.getName() + "\r\n" + savedDevice.getAddress());
            tvDevice.setLayoutParams(new TableRow.LayoutParams(1));

            // Add a connect button to the right
            Button b = new Button(MainActivity.this);
            b.setText(R.string.button_connect);
            b.setGravity(Gravity.RIGHT);

            // Create action when clicking the connect button
            b.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    // Create the activity for the selected device
                    final Intent intent = new Intent(MainActivity.this, SelectedDeviceActivity.class);
                    intent.putExtra(SelectedDeviceActivity.EXTRAS_DEVICE_NAME, savedDevice.getName());
                    mDatabase = FirebaseDatabase.getInstance().getReference("Lockbox1");
                    Query queryRef = mDatabase.child("Users").child(mFirebaseUser.getUid());
                    queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            //String key = dataSnapshot.getKey();
                            //Log.d(TAG, "Value is: " + value);
                            if (dataSnapshot.exists()) {

                                // Connect to device
                                connectToDevice(savedDevice);

                                // start activity
                                startActivity(intent);
                            } else {
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                                builder.setMessage(R.string.connection_error_message)
                                        .setTitle(R.string.connection_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                android.support.v7.app.AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });


                }
            });

            // Add items to the row
            tr.addView(tvRssi);
            tr.addView(tvDevice);
            tr.addView(b);

            // Add row to the table layout
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTableDevices.addView(tr);
                }
            });
        }
    }

    /**
     * Broadcast an update on the specified action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Broadcast an update on the specified action
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        final Intent intent = new Intent(action);

        if ((UUID.fromString(ProjectZeroAttributes.BUTTON0_STATE)).equals(characteristic.getUuid())) {
            // State of button 0 has changed. Add id and value to broadcast
            intent.putExtra(EXTRA_BUTTON0, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        } else if ((UUID.fromString(ProjectZeroAttributes.BUTTON1_STATE)).equals(characteristic.getUuid())) {
            // State of button 1 has changed. Add id and value to broadcast
            intent.putExtra(EXTRA_BUTTON1, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        } else if ((UUID.fromString(ProjectZeroAttributes.LED0_STATE)).equals(characteristic.getUuid())) {
            // State of led 0 has changed. Add id and value to broadcast
            intent.putExtra(EXTRA_LED0, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        } else if ((UUID.fromString(ProjectZeroAttributes.LED1_STATE)).equals(characteristic.getUuid())) {
            // State of led 1 has changed. Add id and value to broadcast
            intent.putExtra(EXTRA_LED1, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        } else {
            // Write the data formatted as a string
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(EXTRA_DATA, new String(data));
            }
        }

        sendBroadcast(intent);
    }

    /**
     * Write gatt descriptor if queue is ready.
     */
    private void writeGattDescriptor(BluetoothGattDescriptor d) {
        // Add descriptor to the write queue
        descriptorWriteQueue.add(d);
        // If there is only 1 item in the queue, then write it. If more than 1, it will be handled
        // in the onDescriptorWrite callback
        if (descriptorWriteQueue.size() == 1) {
            mBluetoothGatt.writeDescriptor(d);
        }
    }



    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();


    }
}
