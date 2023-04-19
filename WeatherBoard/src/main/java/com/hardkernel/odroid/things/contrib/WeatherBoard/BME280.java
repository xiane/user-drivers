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

package com.hardkernel.odroid.things.contrib.WeatherBoard;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * BME280 Userland Driver. sensor for temperature, pressure and humidity.
 * The Driver support I2C connection. But will be support spi 3wire mode.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BME280 implements AutoCloseable {
    private final I2cDevice device;

    /**
     * BME280 chip's I2C Address.
     */
    public static final byte ADDRESS = 0x76;

    private final byte ID = 0x60;

    /**
     * Driver support power mode.
     * SLEEP, FORCE and NORMAL mode is supported.
     * SOFT_RESET_CODE is used to software reset.
     */
    public static class POWER_MODE{
        final static byte SLEEP = 0b00;
        final static byte FORCE = 0b01;
        final static byte NORMAL = 0b11;
        final static byte SOFT_RESET_CODE = (byte) 0xB6;
    }

    /**
     * Oversampling value for temperature, pressure and humidity.
     */
    public static class OVERSAMPLING {
        final static byte SKIP = 0b000;
        final static byte X1 = 0b001;
        final static byte X2 = 0b010;
        final static byte X4 = 0b011;
        final static byte X8 = 0b100;
        final static byte X16 = 0b101;
    }

    /**
     * IIR filter coefficient value.
     */
    public static class FILTER {
        final static byte OFF = 0b00;
        final static byte C2 = 0b001;
        final static byte C4 = 0b010;
        final static byte C8 = 0b011;
        final static byte C16 = 0b100;
    }

    /**
     * inactive duration time on standby in normal mode.
     * value[ms]. ex) D0_5 - 0.5ms, D10 - 10ms
     */
    public static class STANDBY_DURATION {
        final static byte D0_5 = 0b000;
        final static byte D10 = 0b110;
        final static byte D20 = 0b0111;
        final static byte D62_5 = 0b001;
        final static byte D125 = 0b010;
        final static byte D250 = 0b011;
        final static byte D500 = 0b100;
        final static byte D1000 = 0b101;
    }

    /**
     * Register addresses for BME280.
     */
    protected static class reg {
        final static byte CHIP_ID = (byte) 0xD0;
        final static byte RST = (byte) 0xE0;
        final static byte STATUS = (byte) 0xF3;
        final static byte CTRL_MEAS = (byte) 0xF4;
        final static byte CTRL_HUMIDITY = (byte) 0xF2;
        final static byte CONFIG = (byte) 0xF5;

        static class pressure {
            final static byte MSB = (byte) 0xF7;
            final static byte LSB = (byte) 0xF8;
            final static byte XLSB = (byte) 0xF9;
        }

        static class temperature {
            final static byte MSB = (byte) 0xFA;
            final static byte LSB = (byte) 0xFB;
            final static byte XLSB = (byte) 0xFC;
        }

        static class humidity {
            final static byte MSB = (byte) 0xFD;
            final static byte LSB = (byte) 0xFE;
        }
    }

    /**
     * calibration parameter register addresses.
     */
    protected static class cali_reg {
        static class temperature {
            final static byte T1_LSB = (byte) 0x88;
            final static byte T1_MSB = (byte) 0x89;
            final static byte T2_LSB = (byte) 0x8A;
            final static byte T2_MSB = (byte) 0x8B;
            final static byte T3_LSB = (byte) 0x8C;
            final static byte T3_MSB = (byte) 0x8D;
        }

        static class pressure {
            final static byte P1_LSB = (byte) 0x8E;
            final static byte P1_MSB = (byte) 0x8F;
            final static byte P2_LSB = (byte) 0x90;
            final static byte P2_MSB = (byte) 0x91;
            final static byte P3_LSB = (byte) 0x92;
            final static byte P3_MSB = (byte) 0x93;
            final static byte P4_LSB = (byte) 0x94;
            final static byte P4_MSB = (byte) 0x95;
            final static byte P5_LSB = (byte) 0x96;
            final static byte P5_MSB = (byte) 0x97;
            final static byte P6_LSB = (byte) 0x98;
            final static byte P6_MSB = (byte) 0x99;
            final static byte P7_LSB = (byte) 0x9A;
            final static byte P7_MSB = (byte) 0x9B;
            final static byte P8_LSB = (byte) 0x9C;
            final static byte P8_MSB = (byte) 0x9D;
            final static byte P9_LSB = (byte) 0x9E;
            final static byte P9_MSB = (byte) 0x9F;
        }

        static class humidity {
            final static byte H1 = (byte) 0xA1;
            final static byte H2_LSB = (byte) 0xE1;
            final static byte H2_MSB = (byte) 0xE2;
            final static byte H3 = (byte) 0xE3;
            final static byte H4_MSB = (byte) 0xE4;
            final static byte H4_LSB = (byte) 0xE5;
            final static byte H5_MSB = (byte) 0xE6;
            final static byte H6 = (byte) 0xE7;
        }
    }

    /**
     * calibration parameters.
     */
    protected class calibration_param {
        int[] dig_T;
        int[] dig_P;
        int[] dig_H;

        int  t_fine;

        /**
         * Initialize calibration parameter.
         * Read calibration data from registers.
         * @param device target I2C bus device.
         * @throws IOException caused from i2c procedure.
         */
        public calibration_param(I2cDevice device) throws IOException {
            byte[] params = new byte[26];
            dig_T = new int[3];
            dig_P = new int[9];
            dig_H = new int[6];

            device.readRegBuffer(cali_reg.temperature.T1_LSB, params, 26);

            param.dig_T[0] = ((params[1] & 0xFF) << 8) | (params[0] & 0xFF);
            param.dig_T[1] = ((params[3] & 0xFF) << 8) | (params[2] & 0xFF);
            if (param.dig_T[1] > 0x7FFF)
                param.dig_T[1] -= 0x10000;
            param.dig_T[2] = ((params[5] & 0xFF) << 8) | (params[4] & 0xFF);
            if (param.dig_T[2] > 0x7FFF)
                param.dig_T[2] -= 0x10000;

            param.dig_P[0] = ((params[7] & 0xFF) << 8) | (params[6] & 0xFF);
            param.dig_P[1] = ((params[9] & 0xFF) << 8) | (params[8] & 0xFF);
            if (param.dig_P[1] > 0x7FFF)
                param.dig_P[1] -= 0x10000;
            param.dig_P[2] = ((params[11] & 0xFF) << 8) | (params[10] & 0xFF);
            if (param.dig_P[2] > 0x7FFF)
                param.dig_P[2] -= 0x10000;
            param.dig_P[3] = ((params[13] & 0xFF) << 8) | (params[12] & 0xFF);
            if (param.dig_P[3] > 0x7FFF)
                param.dig_P[3] -= 0x10000;
            param.dig_P[4] = ((params[15] & 0xFF) << 8) | (params[14] & 0xFF);
            if (param.dig_P[4] > 0x7FFF)
                param.dig_P[4] -= 0x10000;
            param.dig_P[5] = ((params[17] & 0xFF) << 8) | (params[16] & 0xFF);
            if (param.dig_P[5] > 0x7FFF)
                param.dig_P[5] -= 0x10000;
            param.dig_P[6] = ((params[19] & 0xFF) << 8) | (params[18] & 0xFF);
            if (param.dig_P[6] > 0x7FFF)
                param.dig_P[6] -= 0x10000;
            param.dig_P[7] = ((params[21] & 0xFF) << 8) | (params[20] & 0xFF);
            if (param.dig_P[7] > 0x7FFF)
                param.dig_P[7] -= 0x10000;
            param.dig_P[8] = ((params[23] & 0xFF) << 8) | (params[22] & 0xFF);
            if (param.dig_P[8] > 0x7FFF)
                param.dig_P[8] -= 0x10000;

            param.dig_H[0] = params[25] & 0xFF;

            params = new byte[7];
            device.readRegBuffer(cali_reg.humidity.H2_LSB, params,7);
            param.dig_H[1] = (params[1] & 0xFF << 8 | params[0] & 0xFF);
            if (param.dig_H[1] > 0x7FFF)
                param.dig_H[1] -= 0x10000;
            param.dig_H[2] = (params[2] & 0xFF);
            param.dig_H[3] = ((params[3] & 0xFF) << 4) | (params[4] & 0x0F);
            if (param.dig_H[3] > 0x7FF)
                param.dig_H[3] -= 0x1000;
            param.dig_H[4] = ((params[5] & 0xFF) << 4) | ((params[4] & 0xFF) >> 4);
            if (param.dig_H[4] > 0x7FF)
                param.dig_H[4] -= 0x1000;
            param.dig_H[5] = params[6] & 0xFF;
            if (param.dig_H[5] > 127)
                param.dig_H[5] -= 256;
        }

        /**
         * clear parameters.
         */
        public void close() {
            dig_H = null;
            dig_P = null;
            dig_T = null;
        }
    }

    private byte config_reg = 0x0;
    private byte ctrl_meas_reg = 0x0;
    private byte ctrl_hum_reg = 0x0;

    private final calibration_param param;

    /**
     * Create BME280 instance and initialize with parameters.
     * @param i2cBusName android things i2c target bus name.
     * @param powerMode power mode on initialize sequence. recommended to NORMAL.
     * @param hSampling humidity oversampling rate.
     * @param pSampling pressure oversampling rate.
     * @param tSampling temperature oversampling rate.
     * @throws IOException caused from i2c procedure.
     * @throws IllegalAccessException chip id is incorrect.
     * @throws InterruptedException caused from sleep.
     */
    public BME280(String i2cBusName,
                  byte powerMode, byte hSampling, byte pSampling, byte tSampling)
            throws IOException, IllegalAccessException, InterruptedException {
        PeripheralManager manager = PeripheralManager.getInstance();
        device = manager.openI2cDevice(i2cBusName, ADDRESS);
        param = new calibration_param(device);

        initialize(powerMode, hSampling, pSampling, tSampling);

        // First call to set t_fine value.
        readTemperatureC();
    }

    private void initialize(byte powerMode, byte hSampling, byte pSampling, byte tSampling)
            throws IOException, IllegalAccessException, InterruptedException {
        if (device.readRegByte(reg.CHIP_ID) != ID)
            throw new IllegalAccessException("wrong device");
        readRegs();
        setPowerMode(powerMode);
        setOverSamplingHumidity(hSampling);
        setOverSamplingPressure(pSampling);
        setOverSamplingTemperature(tSampling);
        Thread.sleep(10);
        param.t_fine = 0;
    }

    /**
     * get default CHIP ID.
     * @return CHIP ID.
     */
    public byte getID() {
        return ID;
    }

    private void softRst() throws IOException {
        device.writeRegByte(reg.RST, POWER_MODE.SOFT_RESET_CODE);
    }

    private byte getPowerMode() throws IOException {
        return (byte) (device.readRegByte(reg.CTRL_MEAS) & 0b11);
    }

    /**
     * Set power mode.
     * Please use BME280.POWER_MODE values.
     * SLEEP, FORCE, NORMAL.
     * @param powerMode SLEEP or FORCE or NORMAL
     * @throws IOException caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     * @throws IllegalArgumentException wrong power mode.
     */
    public void setPowerMode (byte powerMode)
            throws IOException, InterruptedException, IllegalArgumentException{
        if (powerMode <=  POWER_MODE.NORMAL) {
            setRegister(new RegisterSetting() {
                @Override
                public void prepareReg() {
                    ctrl_meas_reg = (byte) (ctrl_meas_reg & ~0b11 | powerMode & 0b11);
                }

                @Override
                public void setRegWhenSleep() throws IOException {
                    device.writeRegByte(reg.CTRL_MEAS, ctrl_meas_reg);
                }
            });
        } else
            throw new IllegalArgumentException("Wrong Power Mode " + powerMode);
    }

    /**
     * Set humidity oversampling.
     * Please use BME280.OVERSAMPLING values.
     * @param sampling oversampling value.
     * @throws IOException caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     */
    public void setOverSamplingHumidity(byte sampling)
            throws IOException, InterruptedException {
        setRegister(new RegisterSetting() {
            @Override
            public void prepareReg() {
                ctrl_hum_reg = (byte)(ctrl_hum_reg & ~ 0b0111 | sampling & 0b0111);
            }

            @Override
            public void setRegWhenSleep() throws IOException {
                device.writeRegByte(reg.CTRL_HUMIDITY, ctrl_hum_reg);

            }
        });
    }

    /**
     * Set pressure oversampling.
     * Please use BME280.OVERSAMPLING values.
     * @param sampling oversampling value.
     * @throws IOException caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     */
    public void setOverSamplingPressure(byte sampling)
        throws IOException, InterruptedException {
        setRegister(new RegisterSetting() {
            @Override
            public void prepareReg() {
                ctrl_meas_reg =
                        (byte) (ctrl_meas_reg & ~(0b111 << 2) | ((sampling & 0b111)<<2));
            }

            @Override
            public void setRegWhenSleep() throws IOException {
                device.writeRegByte(reg.CTRL_MEAS, ctrl_meas_reg);
            }
        });
    }

    /**
     * Set temperature oversampling.
     * Please use BME280.OVERSAMPLING values.
     * @param sampling oversampling value.
     * @throws IOException caused from i2c procedure.
     * @throws InterruptedException caused from sleep.
     */
    public void setOverSamplingTemperature(byte sampling)
            throws IOException, InterruptedException {
        setRegister(new RegisterSetting() {
            @Override
            public void prepareReg() {
                ctrl_meas_reg =
                        (byte) (ctrl_meas_reg & ~(0b111 << 5) | ((sampling & 0b111) << 5));
            }
            @Override
            public void setRegWhenSleep() throws IOException {
                device.writeRegByte(reg.CTRL_MEAS, ctrl_meas_reg);
            }
        });
    }

    /**
     * Set SPI 3wire interface.
     * But not yet supported.
     * @param enable on/off
     * @throws IOException caused from i2c procedure.
     * @throws IllegalArgumentException it must be called when SLEEP MODE.
     */
    public void setSpi3w(boolean enable)
            throws IOException, IllegalArgumentException {
        config_reg = (byte) ((config_reg & 0xfe) | (enable? 0: 1));
        if (getPowerMode() == POWER_MODE.SLEEP)
            device.writeRegByte(reg.CONFIG, config_reg);
        else
            throw new IllegalStateException("Config register must be modified in SLEEP MODE");
    }

    /**
     * Control inactive duration standby time in normal mode.
     * It must be called when SLEEP MODE. Please use BME280.STANDBY_DURATION values.
     * @param duration standby time.
     * @throws IOException caused from i2c procedure.
     * @throws IllegalArgumentException it must be called when SLEEP MODE.
     */
    public void setStandbyDuration(byte duration)
            throws IOException, IllegalArgumentException {
        config_reg = (byte) ((config_reg & 0x1f) | ((duration & 0b111) << 5));
        if (getPowerMode() == POWER_MODE.SLEEP)
            device.writeRegByte(reg.CONFIG, config_reg);
        else
            throw new IllegalStateException("Config register must be modified in SLEEP MODE");
    }

    /**
     *  Controls the time constant of the IIR filter.
     * It must be called when SLEEP MODE. Please use BME280.FILTER values.
     * @param coefficient filtering coefficient.
     * @throws IOException caused from i2c procedure.
     * @throws IllegalArgumentException it must be called when SLEEP MODE.
     */
    public void setFilterCoefficient(byte coefficient)
            throws IOException, IllegalArgumentException {
        config_reg = (byte) ((config_reg & 0xe3) | ((coefficient & 0b111) << 2));
        if (getPowerMode() == POWER_MODE.SLEEP)
            device.writeRegByte(reg.CONFIG, config_reg);
        else
            throw new IllegalStateException("Config register must be modified in SLEEP MODE");
    }

    private void setRegister(RegisterSetting sampling)
            throws IOException, InterruptedException {
        sampling.set();
    }

    private abstract class RegisterSetting {
        public abstract void prepareReg();
        public abstract void setRegWhenSleep() throws IOException;

        public void setReg() throws IOException {
            device.writeRegByte(reg.CTRL_HUMIDITY, ctrl_hum_reg);
            device.writeRegByte(reg.CTRL_MEAS, ctrl_meas_reg);
        }

        public void set() throws IOException, InterruptedException {
            prepareReg();
            if (getPowerMode() != POWER_MODE.SLEEP) {
                softRst();
                Thread.sleep(3);
                setReg();
            } else
                setRegWhenSleep();
            readRegs();
        }
    }

    private void readRegs() throws IOException {
        ctrl_hum_reg = device.readRegByte(reg.CTRL_HUMIDITY);
        ctrl_meas_reg = device.readRegByte(reg.CTRL_MEAS);
        config_reg = device.readRegByte(reg.CONFIG);
    }

    /**
     * Get measuring value from status register.
     * Automatically set to true whenever a conversion is running
     * and back to false when the results have been transferred to the data registers.
     * @return measuring status.
     * @throws IOException caused from i2c procedure.
     */
    public boolean getStatusMeasuring() throws IOException {
        short status = device.readRegWord(reg.STATUS);
        return ((status & 0x4) != 0);
    }

    /**
     * Get update value from status register.
     * Automatically set to true when the NVM data are being copied to image registers
     * and back to false when the copying is done.
     * The data are copied at power-on-reset and before every conversion.
     * @return measuring status.
     * @throws IOException caused from i2c procedure.
     */
    public boolean getStatusUpdate() throws IOException {
        short status = device.readRegWord(reg.STATUS);
        return ((status & 0x1) != 0);
    }

    /**
     * Get humidity.
     * @return humidity values (zero to 100).
     * @throws IOException caused from i2c procedure.
     */
    public double readHumidity() throws IOException {
        byte[] buffer = new byte[2];
        device.readRegBuffer(reg.humidity.MSB, buffer, 2);
        long adc_h = (((long)(buffer[0] & 0xFF) << 8) + (long)(buffer[1] & 0xFF));

        // Humidity offset calculations
        double var_H = (param.t_fine - 76800.0);
        var_H = (adc_h - (param.dig_H[3] * 64.0 + param.dig_H[4] / 16384.0 * var_H))
                * (param.dig_H[1] / 65536.0 * (1.0 + param.dig_H[5] / 67108864.0 * var_H * (1.0 + param.dig_H[2] / 67108864.0 * var_H)));
        double humidity = var_H * (1.0 -  param.dig_H[0] * var_H / 524288.0);
        if(humidity > 100.0) {
            humidity = 100.0;
        } else if (humidity < 0.0) {
            humidity = 0.0;
        }

        return humidity;
    }

    /**
     * Get pressure.
     * @return pressure value, hPa.
     * @throws IOException caused from i2c procedure.
     */
    public double readPressure() throws IOException {
        byte[] buffer = new byte[3];
        device.readRegBuffer(reg.pressure.MSB, buffer, 3);
        long adc_p = (((long)(buffer[0] & 0xFF) << 16) | ((long)(buffer[1] & 0xFF) << 8) | (long)(buffer[2] & 0xF0)) >> 4;

        double var1 = (param.t_fine / 2.0) - 64000.0;
        double var2 = var1 * var1 * ((double)param.dig_P[5]) / 32768.0;
        var2 = var2 + var1 * ((double)param.dig_P[4]) * 2.0;
        var2 = (var2 / 4.0) + (((double)param.dig_P[3]) * 65536.0);
        var1 = (((double) param.dig_P[2]) * var1 * var1 / 524288.0 + ((double) param.dig_P[1]) * var1) / 524288.0;
        var1 = (1.0 + var1 / 32768.0) * ((double)param.dig_P[0]);
        double p = 1048576.0 - (double)adc_p;
        p = (p - (var2 / 4096.0)) * 6250.0 / var1;
        var1 = ((double) param.dig_P[8]) * p * p / 2147483648.0;
        var2 = p * ((double) param.dig_P[7]) / 32768.0;

        return (p + (var1 + var2 + ((double)param.dig_P[6])) / 16.0) / 100;
    }

    /**
     * Get temperature on celsius.
     * @return temperature value, celsius.
     * @throws IOException caused from i2c procedure.
     */
    public double readTemperatureC() throws IOException {
        byte[] buffer = new byte[3];
        device.readRegBuffer(reg.temperature.MSB, buffer, 3);
        double[] var = calculateTemperature(buffer);

        return (var[0] + var[1]) / 5120.0;
    }

    /**
     * Get temperature on fahrenheit.
     * @return temperature value, fahrenheit.
     * @throws IOException caused from i2c procedure.
     */
    public double readTemperatureF() throws IOException {
        byte[] buffer = new byte[3];
        device.readRegBuffer(reg.temperature.MSB, buffer, 3);
        double[] var = calculateTemperature(buffer);
        double cTemp = (var[0] + var[1]) / 5120.0;

        return cTemp * 1.8 + 32;
    }

    private double[] calculateTemperature(byte[] buffer) {
        long adc_t = (((long)(buffer[0] & 0xFF) << 16) | ((long)(buffer[1] & 0xFF) << 8) | (long)(buffer[2] & 0xF0)) >> 4;

        double[] var = new double[2];
        var[0] = (((double)adc_t) / 16384.0 - ((double)param.dig_T[0]) / 1024.0) * ((double)param.dig_T[1]);
        var[1] = ((((double)adc_t) / 131072.0 - ((double)param.dig_T[0]) / 8192.0) *
                (((double)adc_t)/131072.0 - ((double)param.dig_T[0])/8192.0)) * ((double)param.dig_T[2]);
        param.t_fine = (int)(var[0] + var[1]);

        return var;
    }

    /**
     * Close i2c bus and reset parameters.
     * @throws IOException caused from i2c procedure.
     */
    @Override
    public void close() throws IOException {
        device.close();
        param.close();
    }
}