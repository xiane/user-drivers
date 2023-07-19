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

package com.hardkernel.odroid.things.contrib.BarcodeScanner;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Barcode Scanner driver with uart callback and gpio.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BarcodeScanner implements AutoCloseable {

    private static final String TAG = BarcodeScanner.class.getSimpleName();

    private final Gpio mTrigger, mReset;
    private BarcodeListener mListener;
    private final UartDevice barcodeLine;

    private byte prefix = -1;
    private byte suffix = 13;

    /**
     * manual scan until trigger on, stop when scanned
     * self_activated continuous scan, delay when scanned
     * sense detecting view change and scan same to self-activated?
     * continuous rapid scan after scanned
     */
    public enum ScanMode {
        manual,
        self_activated,
        sense,
        continuous,
    }

    private ScanMode mScanMode = ScanMode.manual;

    /**
     * Interface definition for Barcode Listening.
     */
    public interface BarcodeListener {
        void getBarcode(String barcode);
    }

    private final UartDeviceCallback callback = new UartDeviceCallback() {
        private String token = "";
        private final byte[] buffer = new byte[20];

        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uartDevice) {
            try {
                int length = uartDevice.read(buffer, 20);
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        if (buffer[i] == prefix) {
                            token = "";
                        } else if (buffer[i] == suffix) {
                            mListener.getBarcode(token);
                            token = "";

                            if (mScanMode == ScanMode.manual) {
                                stopScan();
                            }
                        } else {
                            token += new String(buffer, i, 1, StandardCharsets.UTF_8);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    /**
     * Create a new Barcode Scanner with trigger, reset for GPIO and serial for Uart and BarcodeListener.
     *
     * @param triggerName  GPIO pin for trigger/.
     * @param resetName    GPIO pin for reset/wake.
     * @param serialName   UART to connect serial data.
     * @param listener     method interface barcode listener.
     * @throws IOException Exception about wrong GPIO pin name.
     */
    public BarcodeScanner(String triggerName, String resetName, String serialName,
                          BarcodeListener listener) throws IOException, IllegalArgumentException {
        mListener = listener;

        PeripheralManager manager = PeripheralManager.getInstance();

        mTrigger = manager.openGpio(triggerName);
        mReset = manager.openGpio(resetName);
        barcodeLine = manager.openUartDevice(serialName);

        mTrigger.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        mReset.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        barcodeLine.setBaudrate(9600);
        barcodeLine.setDataSize(8);
        barcodeLine.setParity(UartDevice.PARITY_NONE);
        barcodeLine.setStopBits(1);
        barcodeLine.setHardwareFlowControl(UartDevice.HW_FLOW_CONTROL_NONE);

        barcodeLine.registerUartDeviceCallback(callback);
    }

    /**
     * Set parsing prefix.
     * default value is -1
     * @param prefix parsing prefix value.
     */
    public void setPrefix(byte prefix) {
        this.prefix = prefix;
    }

    /**
     * Set parsing string's suffix.
     * default value is 13
     * @param suffix
     */
    public void setSuffix(byte suffix) {
        this.suffix = suffix;
    }

    public void reset() throws IOException {
        mReset.setValue(true);
        mReset.setValue(false);
        mTrigger.setValue(true);
    }

    public void startScan() throws IOException {
        mTrigger.setValue(false);
    }

    public void stopScan() throws IOException {
        mTrigger.setValue(true);
    }

    public void setScanMode(ScanMode mode) throws IOException {
        mScanMode = mode;
        mTrigger.setValue(true);
    }

    /**
     * Change Barcode listener callback.
     *
     * @param listener Barcode listener callback.
     */
    public void changeListener(BarcodeListener listener) {
        mListener = listener;
    }

    @Override
    public void close() throws Exception {
        stopScan();
        mListener = null;
        barcodeLine.close();
        mTrigger.close();
        mReset.close();
    }
}