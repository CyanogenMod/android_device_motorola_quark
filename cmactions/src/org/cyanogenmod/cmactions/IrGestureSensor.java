/*
 * Copyright (c) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cyanogenmod.cmactions;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class IrGestureSensor implements ActionableSensor, SensorEventListener {
    private static final String TAG = "CMActions-IRGestureSensor";

    private SensorHelper mSensorHelper;
    private SensorAction mDozeAction;
    private Sensor mSensor;
    private boolean mIsScreenOn;

    private int mLastEventId;
    private int mGesture = IR_GESTURE_NONE;

    // Something occludes the sensor
    public static final int IR_GESTURE_OCCLUSION = 1;
    // No occlusion
    public static final int IR_GESTURE_NONE      = 2;
    // Hand wave in front of the phone (send doze)
    public static final int IR_GESTURE_HAND_WAVE = 4;

    public IrGestureSensor(SensorHelper sensorHelper, SensorAction dozeAction) {
        mSensorHelper = sensorHelper;
        mDozeAction = dozeAction;

        mSensor = sensorHelper.getIrGestureSensor();
    }

    public void setScreenOn(boolean isScreenOn) {
       mIsScreenOn = isScreenOn;
       Log.d(TAG, "mIsScreenOn: " + isScreenOn);
    }

    private void reset(int newEventId) {
        mGesture = IR_GESTURE_NONE;
        mLastEventId = newEventId;
    }

    @Override
    public void enable() {
        Log.d(TAG, "Enabling");
        mSensorHelper.registerListener(mSensor, this);
    }

    @Override
    public void disable() {
        Log.d(TAG, "Disabling");
        mSensorHelper.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG, "events num: " + event.values.length);
        //for (int i = 0; i < event.values.length; i++) {
        //    Log.d(TAG, "event[" + i + "]: " + event.values[i]);
        //}

        int eventId = (int)event.values[0];
        if (eventId != mLastEventId) {
            reset(eventId);
        }

        mGesture = (int)event.values[1];

        if (!mIsScreenOn && mGesture == IR_GESTURE_HAND_WAVE) {
            Log.d(TAG, "mGesture: " + mGesture);
            mDozeAction.action();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
