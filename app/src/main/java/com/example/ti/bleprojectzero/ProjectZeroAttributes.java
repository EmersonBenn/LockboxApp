/*
 * Filename:   ProjectZeroAttributes.java
 *
 * This class contains GATT attributes for the Project Zero Demo
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

import java.util.HashMap;
import java.util.UUID;

/**
 *
 */
public class ProjectZeroAttributes {

    public static String LED_SERVICE = "F0001110-0451-4000-B000-000000000000";
    public static String BUTTON_SERVICE = "F0001120-0451-4000-B000-000000000000";
    public static String DATA_SERVICE = "F0001130-0451-4000-B000-000000000000";
    public static String LOCKBOXSERVICE = "0000BA55-0000-1000-8000-00805F9B34FB";
    public static String LED0_STATE = "F0001111-0451-4000-B000-000000000000";
    public static String LED1_STATE = "F0001112-0451-4000-B000-000000000000";
    public static String BUTTON0_STATE = "F0001121-0451-4000-B000-000000000000";
    public static String BUTTON1_STATE = "F0001122-0451-4000-B000-000000000000";
    public static String STRING_CHAR = "F0001131-0451-4000-B000-000000000000";
    public static String STREAM_CHAR = "F0001132-0451-4000-B000-000000000000";
    public static String UNLOCK = "F000BEEF-0451-4000-B000-000000000000";
    public static String BATTERY = "F000BEFF-0451-4000-B000-000000000000";
    public static String TIMEOPEN = "F000BEAA-0451-4000-B000-000000000000";
    public static String LOCKSTATUS = "F000BEAB-0451-4000-B000-000000000000";

    public final static UUID UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // UUID for notification descriptor
    public final static UUID UUID_BUTTON_SERVICE = UUID.fromString(BUTTON_SERVICE);

    private static HashMap<String, String> gattAttributes = new HashMap();
    static {
        // Services
        gattAttributes.put(LED_SERVICE.toLowerCase(), "Led Service");
        gattAttributes.put(BUTTON_SERVICE.toLowerCase(), "Button Service");
        gattAttributes.put(DATA_SERVICE.toLowerCase(), "Data Service");
        gattAttributes.put(LOCKBOXSERVICE.toLowerCase(), "Lockbox Service");
        // Characteristics
        gattAttributes.put(LED0_STATE.toLowerCase(), "Led0 State");
        gattAttributes.put(LED1_STATE.toLowerCase(), "Led1 State");
        gattAttributes.put(BUTTON0_STATE.toLowerCase(), "Button0 State");
        gattAttributes.put(BUTTON1_STATE.toLowerCase(), "Button1 State");
        gattAttributes.put(STRING_CHAR.toLowerCase(), "String char");
        gattAttributes.put(STREAM_CHAR.toLowerCase(), "Stream char");
        gattAttributes.put(UNLOCK.toLowerCase(), "Unlock");
        gattAttributes.put(BATTERY.toLowerCase(), "Battery");
        gattAttributes.put(TIMEOPEN.toLowerCase(), "TimeOpen");
        gattAttributes.put(LOCKSTATUS.toLowerCase(), "LockStatus");


    }

    /**
     * Search the map for the attribute name of a given UUID
     *
     * @param uuid        UUID to search for
     * @param defaultName Name to return if the UUID is not found in the map
     *
     * @return Name of attribute with given UUID
     */
    public static String lookup(String uuid, String defaultName) {
        String name = gattAttributes.get(uuid);
        return name == null ? defaultName : name;
    }

    /**
     * @return Map of UUIDs and attribute names used in the Project Zero demo
     */
    public static HashMap<String, String> gattAttributes(){
        return gattAttributes;
    }

}
