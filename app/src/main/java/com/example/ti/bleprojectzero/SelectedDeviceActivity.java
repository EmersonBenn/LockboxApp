/*
 * Filename:   SelectedDeviceActivity.java
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

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.firebase.database.*;


public class SelectedDeviceActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";

    private TextView mConnectionState;
    private EditText mDataField;
    private String mDeviceName;
    private MainActivity mMainActivity;
    private HashMap<String, BluetoothGattCharacteristic> mGattCharacteristics = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add back-button to parent activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get name of device that this intent is opened for
        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);

        // Set activity title to the device name
        setTitle(mDeviceName);

        // Get UI elements
        mConnectionState = (TextView) findViewById(R.id.connection_state);
       // mDataField = (EditText) findViewById(R.id.data_value);

        // Get instance of main activity
        mMainActivity = (MainActivity) MainActivity.activity;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register a receiver for broadcast updates
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Do not receive broadcast updates when paused
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainActivity = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect when leaving activity
        mMainActivity.disconnect();
    }

    /**
     * Handles various events fired by MainActivity
     * ACTION_GATT_CONNECTED: connected to a GATT server.
     * ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
     * ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
     * ACTION_DATA_AVAILABLE: received data from the device. This can be a
     * result of read or notification operations.
     * ACTION_WRITE_SUCCESS: data string was successfully written to device
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch(action){
                case MainActivity.ACTION_GATT_CONNECTED:
                    updateConnectionState(R.string.connected);
                    invalidateOptionsMenu();
                    break;
                case MainActivity.ACTION_GATT_DISCONNECTED:
                    updateConnectionState(R.string.disconnected);
                    invalidateOptionsMenu();
                    clearUI();
                    break;
                case MainActivity.ACTION_GATT_SERVICES_DISCOVERED:
                    initializeGattServiceUIElements(mMainActivity.getSupportedGattServices());
                    break;
                case MainActivity.ACTION_DATA_AVAILABLE:
                    // If the a toggle button ID is sent as extra data, call a function to display the button state.
                    // Otherwise, display the extra data in the data service text field
                    if(intent.hasExtra(MainActivity.EXTRA_BUTTON0)) {
                       // setToggleButtonState(R.id.button0_value, intent.getIntExtra(MainActivity.EXTRA_BUTTON0, 0));
                    }
                    else if(intent.hasExtra(MainActivity.EXTRA_BUTTON1)) {
                       // setToggleButtonState(R.id.button1_value, intent.getIntExtra(MainActivity.EXTRA_BUTTON1, 0));
                    }
                    else if(intent.hasExtra(MainActivity.EXTRA_LED0)) {
                        setToggleButtonState(R.id.led0_value, intent.getIntExtra(MainActivity.EXTRA_LED0, 0));
                    }
                    else if(intent.hasExtra(MainActivity.EXTRA_LED1)) {
                       // setToggleButtonState(R.id.led1_value, intent.getIntExtra(MainActivity.EXTRA_LED1, 0));
                    }
                    else {
                        displayData(intent.getStringExtra(MainActivity.EXTRA_DATA));
                    }
                    break;
                case MainActivity.ACTION_WRITE_SUCCESS:
                    // Clear the data service text field when a write has finished
                    displayData("");
                    break;
            }
        }
    };

    /**
     * Update the connection status field
     */
    private void updateConnectionState(final int resourceId) {
        SelectedDeviceActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    /**
     * Display a text string in the data service edit view
     *
     * @param text   The text to display
     */
    private void displayData(String text) {
        if (text != null) {
            mDataField.setText(text);
        }
    }

    /**
     * Display the state of a toggle button. Toggle buttons
     * are used for both LED and button Services.
     *
     * @param id     The ID of button that was clicked
     * @param value  Value of the button
     */
    private void setToggleButtonState(int id, int value) {
        if (id != -1) {
            ToggleButton b = (ToggleButton) findViewById(id);
            if(value == 1){
                b.setChecked(true);
            }
            else{
                b.setChecked(false);
            }
        }
    }

    /**
     * Create an intent filter for actions broadcasted
     * by MainActivity
     *
     * @return The created IntenFilter object
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_GATT_CONNECTED);
        intentFilter.addAction(MainActivity.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(MainActivity.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(MainActivity.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(MainActivity.ACTION_WRITE_SUCCESS);
        return intentFilter;
    }


    /**
     * Cleanup function called on a disconnect
     */
    private void clearUI() {
        mDataField.setText("");
        //  Make sure LEDs and Button elemtents are unchecked
        ToggleButton b = (ToggleButton) findViewById(R.id.led0_value);
        b.setChecked(false);
      //  b = (ToggleButton) findViewById(R.id.led1_value);
       // b.setChecked(false);
      //  b = (ToggleButton) findViewById(R.id.button0_value);
     //   b.setChecked(false);
        //b = (ToggleButton) findViewById(R.id.button1_value);
        //b.setChecked(false);
    }

    /**
     * Iterate through the supported GATT Services/Characteristics,
     * and initialize UI elements displaying them.
     */
    private void initializeGattServiceUIElements(List<BluetoothGattService> gattServices) {

        String uuid;
        String serviceName;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        mGattCharacteristics.clear();

        // Create a set of all UUIDs of the discovered services and characteristics
        Set<String> discoveredServiceUuids = new HashSet<>();
        for (BluetoothGattService s : gattServices) {
            discoveredServiceUuids.add(s.getUuid().toString());
            for(BluetoothGattCharacteristic c :s.getCharacteristics()){
                discoveredServiceUuids.add(c.getUuid().toString());
            }
        }

        // Loop through the Project Zero service/characteristic UUIDs, and verify that all
        // are discovered for current device
        for(String projZeroUuid : ProjectZeroAttributes.gattAttributes().keySet()){
            if(!discoveredServiceUuids.contains(projZeroUuid))
            {
                // The Project Zero attribute is not discovered for this device.
                // Display an error and return to main screen.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Error!");
                builder.setMessage("The expected Project Zero attribute " +
                        ProjectZeroAttributes.lookup(projZeroUuid, "with UUID = "+ projZeroUuid) +
                        " was not discovered. Device will be disconnected.");
                builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Return from activity
                        finish();
                    }
                });
                AlertDialog a = builder.create();
                a.show();
            }
        }

        // Loop through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            // Get name of current service
            uuid = gattService.getUuid().toString();
            serviceName = ProjectZeroAttributes.lookup(uuid, unknownServiceString);

            // Loop through available Characteristics for current service.
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                // Get name of current characteristic
                uuid = gattCharacteristic.getUuid().toString();
                String characteristicName = ProjectZeroAttributes.lookup(uuid, unknownCharaString);

                // Handle LED characteristics
                if(serviceName.contains("Unlock")) {
                    // Get button instance
                    ToggleButton b;
                    //if (characteristicName.contains("Led0")) {
                    b = (ToggleButton) findViewById(R.id.led0_value);
                    //} /*else if (characteristicName.contains("Led1")) {
                     //   b = (ToggleButton) findViewById(R.id.led1_value);
                    //}*/ else{
                    //    continue;
                    //}

                    // Add action for clicking the LED button
                    if(b!= null) {
                        b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                // Write value to 1 if button is checked, and to 0 otherwise
                                byte[] value = new byte[16];
                                if (isChecked) {
                                    value[0] = (byte) 0xFF; //initial key is hardcoaded for now
                                    value[1] = (byte) 0xEE;
                                    value[2] = (byte) 0xDD;
                                    value[3] = (byte) 0xCC;
                                    value[4] = (byte) 0xBB;
                                    value[5] = (byte) 0xAA;
                                    value[6] = (byte) 0x99;
                                    value[7] = (byte) 0x88;
                                    value[8] = (byte) 0x77;
                                    value[9] = (byte) 0x66;
                                    value[10] = (byte) 0x55;
                                    value[11] = (byte) 0x44;
                                    value[12] = (byte) 0x33;
                                    value[13] = (byte) 0x22;
                                    value[14] = (byte) 0x11;
                                    value[15] = (byte) 0x00;

                                    //need to get key from firebase
                                } /*else {

                                    value[0] = (byte) (0 & 0xFF);
                                }*/

                                // Write value
                                gattCharacteristic.setValue(value);
                                mMainActivity.writeCharacteristic(gattCharacteristic); // write value
                            }
                        });
                    }

                    // Read initial values of the LEDs
                    //mMainActivity.readCharacteristic(gattCharacteristic);
                }
                // save discovered characteristics
                mGattCharacteristics.put(characteristicName, gattCharacteristic);
            }
        }
    }

    /**
     * Called when the user clicks the read button for data service
     */
    public void onDataRead(View view) {
        if (mGattCharacteristics != null) {
            // Get characteristic for R/W string
            final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get("String char");
            if(characteristic == null) {
                return;
            }
            // Read it if readable
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                mMainActivity.readCharacteristic(characteristic);
            }
        }
    }

    /**
     * Called when the user clicks the write button for data service
     */
    public void onDataWrite(View view) {
        if (mGattCharacteristics != null) {
            // Get characteristic for R/W string
            final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get("String char");
            if(characteristic == null)
                return;

            // Check if writable
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                // Get text
                String strData = mDataField.getText().toString();
                byte[] data = strData.getBytes();

                // Write it
                characteristic.setValue(data);
                mMainActivity.writeCharacteristic(characteristic);
            }
        }
    }
}
