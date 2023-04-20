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

package com.hardkernel.odroid.things.contrib.Si1132;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Si1132 Userland Driver. Sensor for ultraviolet and ambient light (visible and infrared light).
 * The Device support I2C connection.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Si1132 implements AutoCloseable {
    private final I2cDevice device;

    /**
     * Si1132 chip's I2C Address.
     */
    public static final byte ADDRESS = 0x60;

    private final byte ID = 0x32;

    /**
     * Commands
     */
    protected static class CMD {
        static class ALS {
            public final byte FORCE = 0x06;
            public final byte PAUSE = 0x0A;
            public static final byte AUTO = 0x0E;
        }
        public static final byte BUS_ADDRESS = 0x02;
        public static final byte GET_CAL = 0x12;
        public static final byte NOP = 0x00;
        static class PARAM {
            public static final byte QUERY = (byte) 0x80;
            public static final byte SET = (byte) 0xA0;

        }
        public static final byte RESET = 0x01;
    }

    /**
     * Parameters
     */
    protected static class PARAM {
        public static final byte I2C_ADDR = 0x00;
        public static final byte CHLIST = 0x01;
        static class CHLIST_SET {
            public static final byte EN_UV = (byte) 0x80;
            public static final byte EN_AUX = 0x40;
            public static final byte EN_ALS_IR = 0x20;
            public static final byte EN_ALS_VIS = 0x10;
        }
        public static final byte ALS_ENCODING = 0x06;
        public static final byte ALS_IR_ADCMUX = 0x0E;
        public static final byte AUX_ADCMUX = 0x0F;
        public static final byte ALS_VIS_ADC_COUNTER = 0x10;
        public static final byte ALS_VIS_ADC_GAIN = 0x11;
        public static final byte ALS_VIS_ADC_MISC = 0x12;
        static class ALSVISADCMISC {
            public static final byte VIS_RANGE = 0x20;

        }
        public static final byte ALS_IR_ADC_COUNTER = 0x1D;
        public static final byte ALS_IR_ADC_GAIN = 0x1E;
        public static final byte ALS_IR_ADC_MISC = 0x1F;
        static class ALSIRADCMISC {
            public static final byte IR_RANGE = 0x20;
        }

        static class ADCCOUNTER {
            public static final byte CLK1 = 0x0;
            public static final byte CLK7 = 0x10;
            public static final byte CLK15 = 0x20;
            public static final byte CLK31 = 0x30;
            public static final byte CLK63 = 0x40;
            public static final byte CLK127 = 0x50;
            public static final byte CLK255 = 0x60;
            public static final byte CLK511 = 0x70;

        }
        static class ADCMUX {
            public static final byte SMALLIR = 0x00;
            public static final byte LARGEIR = 0x03;
        }
    }

    /**
     * Registers
     */
    protected static class REG {
        public static final byte PART_ID = 0x00;
        public static final byte INT_CFG = 0x03;
        static class INTCFG {
            public static final byte INT_OE = 0x01;
        }
        public static final byte IRQ_ENABLE = 0x04;
        static class IRQ_EN {
            public static final byte ALS_IE = 0x01;

        }
        public static final byte IRQ_MODE1 = 0x05;
        public static final byte IRQ_MODE2 = 0x06;
        public static final byte HW_KEY = 0x07;
        public static final byte MEAS_RATE0 = 0x08;
        public static final byte MEAS_RATE1 = 0x09;
        public static final byte UCOEF0 = 0x13;
        public static final byte UCOEF1 = 0x14;
        public static final byte UCOEF2 = 0x15;
        public static final byte UCOEF3 = 0x16;
        public static final byte PARAM_WR = 0x17;
        public static final byte COMMAND = 0x18;
        public static final byte IRQ_STATUS = 0x21;
        public static final byte ALS_VIS_DATA0 = 0x22;
        public static final byte ALS_VIS_DATA1 = 0x23;
        public static final byte ALS_IR_DATA0 = 0x24;
        public static final byte ALS_IR_DATA1 = 0x25;
        public static final byte AUX_DATA0 = 0x2C;
        public static final byte AUX_DATA1 = 0x2D;
    }

    /**
     * Create Si1132 instance and initialize.
     * @param i2cBusName android things i2c target bus name.
     * @throws IOException caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     */
    public Si1132(String i2cBusName)
            throws IOException, InterruptedException {
        PeripheralManager manager = PeripheralManager.getInstance();
        device = manager.openI2cDevice(i2cBusName, ADDRESS);
        initialize();
    }
    
    private void initialize() throws IOException, InterruptedException {
        if (device.readRegByte(REG.PART_ID) == ID) {
            reset();

            byte[] ucoef = {0x7B, 0x6B, 0x01, 0x00};
            device.writeRegBuffer(REG.UCOEF0, ucoef, 4);

            byte chlist = (byte) (PARAM.CHLIST_SET.EN_UV |
                    PARAM.CHLIST_SET.EN_ALS_IR |
                    PARAM.CHLIST_SET.EN_ALS_VIS);
            writeParam(PARAM.CHLIST, chlist);

            device.writeRegByte(REG.INT_CFG, REG.INTCFG.INT_OE);
            device.writeRegByte(REG.IRQ_ENABLE, REG.IRQ_EN.ALS_IE);

            writeParam(PARAM.ALS_IR_ADCMUX, PARAM.ADCMUX.SMALLIR);
            Thread.sleep(10);
            // fastest clocks, clock div 1
            writeParam(PARAM.ALS_IR_ADC_GAIN, 0x00);
            Thread.sleep(10);
            // take 511 clocks to measure
            writeParam(PARAM.ALS_IR_ADC_COUNTER, PARAM.ADCCOUNTER.CLK511);
            //in high range mode
            writeParam(PARAM.ALS_IR_ADC_MISC, PARAM.ALSIRADCMISC.IR_RANGE);
            Thread.sleep(10);
            // fastest clocks
            writeParam(PARAM.ALS_VIS_ADC_GAIN, 0);
            Thread.sleep(10);
            // take 511 clocks to measure
            writeParam(PARAM.ALS_VIS_ADC_COUNTER, PARAM.ADCCOUNTER.CLK511);
            // in high range mode (not normal signal)
            writeParam(PARAM.ALS_VIS_ADC_MISC, PARAM.ALSVISADCMISC.VIS_RANGE);
            Thread.sleep(10);
            device.writeRegByte(REG.MEAS_RATE0, (byte) 0xFF);
            device.writeRegByte(REG.COMMAND, CMD.ALS.AUTO);
        }
    }

    private void reset() throws IOException, InterruptedException {
        device.writeRegByte(REG.MEAS_RATE0, (byte) 0);
        device.writeRegByte(REG.MEAS_RATE1, (byte) 0);
        device.writeRegByte(REG.IRQ_ENABLE, (byte) 0);
        device.writeRegByte(REG.IRQ_MODE1, (byte) 0);
        device.writeRegByte(REG.IRQ_MODE2, (byte) 0);
        device.writeRegByte(REG.INT_CFG, (byte) 0);
        device.writeRegByte(REG.IRQ_STATUS, (byte) 0xFF);

        device.writeRegByte(REG.COMMAND, CMD.RESET);
        Thread.sleep(10);
        device.writeRegByte(REG.HW_KEY, (byte) 0x17);
    }
    
    private void writeParam(int param, int val) throws IOException {
        device.writeRegByte(REG.PARAM_WR, (byte) val);
        device.writeRegByte(REG.COMMAND, (byte)(param | CMD.PARAM.SET));
    }

    /**
     * Get Ultra violet index value.
     *
     * @return ultra violet index value.
     * @throws IOException          caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     */
    public double readUV() throws IOException, InterruptedException {
        Thread.sleep(10);
        return device.readRegWord(REG.AUX_DATA0) / 100.0;
    }

    /**
     * Get visible light value.
     * @return visible light ambient measurement.
     * @throws IOException caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     */
    public double readVisible() throws IOException, InterruptedException {
        Thread.sleep(10);
        return ((device.readRegWord(REG.ALS_VIS_DATA0) - 256)/0.282) * 14.5;
    }

    /**
     * Get infrared light value.
     * @return infrared light ambient measurement.
     * @throws IOException caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     */
    public double readIR() throws IOException, InterruptedException {
        Thread.sleep(10);
        return ((device.readRegWord(REG.ALS_IR_DATA0) - 250) / 2.44) * 14.5;
    }

    /**
     * Get default CHIP ID.
     * @return CHIP ID.
     */
    public byte getID() {
        return ID;
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
