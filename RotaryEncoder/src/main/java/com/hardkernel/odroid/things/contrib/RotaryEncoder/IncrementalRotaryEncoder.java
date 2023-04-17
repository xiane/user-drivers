/*
 * Copyright 2023 Hardkernel Inc.
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

package com.hardkernel.odroid.things.contrib.RotaryEncoder;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Driver for GPIO based Incremental Rotary Encoder.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class IncrementalRotaryEncoder implements AutoCloseable {

    private static final String TAG = IncrementalRotaryEncoder.class.getSimpleName();
    private final RotaryListener mListener;
    private final Gpio dt, mSwitch, mClock;
    private GpioCallback mSwitchCallback = null;

    private boolean mDoLoop = false;

    /**
     * Interface definition for Rotary Encoder rolling do clock wise (cw) and counter clock wise (ccw).
     */
    public interface RotaryListener {
        void cw();
        void ccw();
    }

    /**
     * Create a new Incremental Rotary Encoder with dt, sw and clk name for GPIOs and rotaryListener.
     * @param dtName GPIO pin for dt.
     * @param swName GPIO pin for sw. You can control callback with registerSwitch / unregisterSwitch.
     * @param clkName GPIO pin for sw.
     * @param rotaryListener method interface stuffs for cw / ccw.
     * @throws IOException Exception about wrong GPIO pin name.
     */
    public IncrementalRotaryEncoder(String dtName, String swName, String clkName,
                                    RotaryListener rotaryListener) throws IOException  {
        mListener = rotaryListener;

        PeripheralManager manager = PeripheralManager.getInstance();

        dt = manager.openGpio(dtName);
        mSwitch = manager.openGpio(swName);
        mClock = manager.openGpio(clkName);

        try {
            dt.setDirection(Gpio.DIRECTION_IN);
            mClock.setDirection(Gpio.DIRECTION_IN);
            mSwitch.setDirection(Gpio.DIRECTION_IN);
            mSwitch.setEdgeTriggerType(Gpio.EDGE_FALLING);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register Switch input Gpio callback.
     * @param callback GPIO Callback for sw.
     * @throws IOException it called from registering or already registered callback.
     */
    public void registerSwitch(GpioCallback callback) throws IOException {
        if (mSwitchCallback == null) {
            mSwitch.registerGpioCallback(callback);
            mSwitchCallback = callback;
        } else {
            throw new IOException("callback is already registered");
        }
    }

    /**
     * Unregister Gpio Callback for sw.
     */
    public void unregisterSwitch() {
        if (mSwitchCallback != null)
            mSwitch.unregisterGpioCallback(mSwitchCallback);
        mSwitchCallback = null;
    }

    /**
     * Start listening for RotaryListener's cw and ccw work.
     * @throws IOException Error about gpio getValue
     */
    public void startEncoder() throws IOException {
        boolean cur_clk, cur_dt;
        boolean prev_clk = false;
        boolean prev_dt = false;

        mDoLoop = true;

        while (mDoLoop) {
            cur_clk = mClock.getValue();
            cur_dt = dt.getValue();

            if (cur_clk && cur_dt) {
                if (!prev_clk && prev_dt)
                    mListener.cw();
                else if (prev_clk && !prev_dt)
                    mListener.ccw();
            }

            prev_clk = cur_clk;
            prev_dt = cur_dt;
        }
    }

    /**
     * Stop listening for RotaryListener's works.
     */
    public void stopEncoder() {
        mDoLoop = false;
    }

    @Override
    public void close() throws Exception {
        mDoLoop = false;
        mClock.close();
        dt.close();
        if (mSwitchCallback != null)
            mSwitch.unregisterGpioCallback(mSwitchCallback);
        mSwitchCallback = null;
        mSwitch.close();
    }
}