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

package com.hardkernel.odroid.things.contrib.Shtc1;

import static java.lang.Thread.sleep;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.List;

/**
 * Basic Driver for I2c based Shtc1.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Shtc1 implements AutoCloseable {
    protected I2cDevice i2c;

    private final int address = 0x70;

    public enum Precision {
        High,
        Low
    }

    private Precision precision = Precision.High;

    private boolean block = true;

    private static final int LONG_CMD = 0x10000000;

    private static class CMD {
        static byte[] HIGH_PRECISION_BLOCK = {0x7C, (byte)0xA2};
        static byte[] HIGH_PRECISION_NON_BLOCK = {0x78, 0x66};
        static byte[] LOW_PRECISION_BLOCK = {0x64, 0x58};
        static byte[] LOW_PRECISION_NON_BLOCK = {0x60, (byte)0x9c};
        static final int READ_ID = LONG_CMD | 0xEFC8;
    }

    public Shtc1(String i2cBus)
            throws IOException {
        // get Peripheral Manager for managing the i2c device.
        PeripheralManager manager = PeripheralManager.getInstance();

        List<String> i2cBusList = manager.getI2cBusList();
        if (i2cBusList.contains(i2cBus))
            i2c = manager.openI2cDevice(i2cBus, address);
        else
            i2c = manager.openI2cDevice(i2cBusList.get(0), address);
    }

    public short getId() throws IOException {
        byte[] ids = new byte[2];
        i2c.readRegBuffer(CMD.READ_ID, ids, 2);
        return (short) (((ids[0] & 0xff) <<8) | (ids[1] & 0xff));
    }

    private final short ID_MASK = 0x3F;
    private final short ID_BIT = 0x7;

    public boolean isCorrectId() throws IOException {
        short id = getId();
        return (id & ID_MASK) == ID_BIT;
    }

    public boolean isCorrectId(short id) {
        return (id & ID_MASK) == ID_BIT;
    }

    public void setPrecision(Precision val) {
        precision = val;
    }

    private byte[] getCMD() {
        switch (precision) {
            case Low:
                return block?
                        CMD.LOW_PRECISION_BLOCK:
                        CMD.LOW_PRECISION_NON_BLOCK;
            case High:
                return block?
                        CMD.HIGH_PRECISION_BLOCK:
                        CMD.HIGH_PRECISION_NON_BLOCK;
        }
        return new byte[0];
    }

    private int getWaitTime() {
        switch (precision) {
            case Low:
                return 1;
            case High:
                return 15;
            default:
                return 0;
        }
    }

    protected byte[] _read()
            throws IOException, InterruptedException {
        byte[] cmd = getCMD();
        int waitTime = getWaitTime();

        byte[] buffer= new byte[6];

        i2c.write(cmd, cmd.length);

        if(block)
            sleep(waitTime);

        i2c.read(buffer, buffer.length);

        return buffer;
    }

    /**
     * read temperature.
     * @return reading data.
     * @throws IOException error when reading from shtc.
     * @throws InterruptedException some interrupt can caused from i2c read.
     */
    public float readTemperature() throws IOException, InterruptedException {
        byte[] data = _read();
        int val = (((data[0] & 0xFF) << 8) | (data[1] & 0xFF));

        float mid = (float)val / 65536;

        return mid * 175 - 45;
    }

    /**
     * read relative humidity.
     * @return reading data.
     * @throws IOException error when reading from shtc.
     * @throws InterruptedException some interrupt can caused from i2c read.
     */
    public float readHumidity() throws IOException, InterruptedException {
        byte[] data = _read();
        int val = (((data[3] & 0xFF) << 8) | (data[4] & 0xFF));

        float mid = (float)val / 65536;

        return mid * 100;
    }

    /**
     * close shtc device driver to control.
     * @throws IOException I2c bus command exception.
     */
    @Override
    public void close() throws IOException {
        i2c.close();
    }
}