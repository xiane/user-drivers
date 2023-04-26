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

package com.hardkernel.odroid.things.contrib.Mcp300x;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

/**
 * Mcp3004/3008 A/D Converter Driver.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Mcp300x implements AutoCloseable {
    private final SpiDevice device;

    public static class ADC_MODE {
        public static final boolean SINGLE_END = true;
        public static final boolean DIFFERENTIAL = false;
    }

    /**
     * Create Mcp300x instance and initialize.
     * the Default frequency is based on 2.7 V Vdd.
     * @param spiBusName android things spi target bus name.
     * @throws IOException caused from i2c procedure.
     */
    public Mcp300x(String spiBusName)
            throws IOException {
        PeripheralManager manager = PeripheralManager.getInstance();
        device = manager.openSpiDevice(spiBusName);
        device.setMode(SpiDevice.MODE0);
        device.setBitsPerWord(8);
        device.setFrequency(1350000);
        device.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);
    }

    /**
     * Set SPI frequency based on the Vdd value.
     * @param voltage target voltage. must be 5.0 or 2.7 V
     * @throws IllegalAccessException Voltage is wrong.
     * @throws IOException caused from spi procedure.
     */
    public void changeClkByVdd(double voltage) throws IllegalAccessException, IOException {
        if (voltage == 5.0)
            device.setFrequency(3600000);
        else if (voltage == 2.7)
            device.setFrequency(1350000);
        else {
            throw new IllegalAccessException("Voltage must be 5.0 or 2.7 Volts");
        }
    }

    /**
     * Read single channel ADC from mcp300x.
     * @param channel target channel number.
     * @param adcMode target adc mode. single-end or differential.
     * @return result value. zero to 1024.
     * @throws IOException caused from spi procedure.
     */
    public int readADC(int channel, boolean adcMode) throws IOException {
        byte[] message = new byte[3];
        message[0] = 0x01;
        message[1] = (byte) ((adcMode? 0x80 : 0x00) | ((byte)(channel & 0x07) << 4));

        device.transfer(message, message, 3);

        return (message[1] | 0x03) << 8 | message[2] & 0xFF;
    }

    /**
     * Read multi channel ADCs from mcp300x.
     * @param channels target channels array.
     * @param adcMode target adc mode. single-end or differential.
     * @return result value array. zero to 1024.
     * @throws IOException caused from spi procedure.
     */
    public int[] readADC(int[] channels, boolean adcMode) throws IOException {
        int[] result = new int[channels.length];

        for (int i =0; i < channels.length; i++)
            result[i] = readADC(channels[i], adcMode);

        return result;
    }

    /**
     * Close i2c bus.
     * @throws IOException caused from sleep.
     */
    @Override
    public void close() throws IOException {
        device.close();
    }
}
