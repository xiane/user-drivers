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

import com.google.android.things.pio.PeripheralManager;

import java.util.List;

/**
 * Weather Board Userland Driver it composed of BME280 and Si1132.
 * The Device connected with I2c bus.
 * You can get Temperature, Humidity, Pressure and Altitude value.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class WeatherBoard implements AutoCloseable {
    /**
     *  UV, Visible, IR
     */
    private SI1132 si1132;
    /**
     *  Temperature, Humidity, Pressure, Altitude
     */
    private BME280 bme280;

    /**
     * Create Weather board instance.
     * @param i2c used android things i2c bus name.
     */
    public WeatherBoard(String i2c) {
        // get Peripheral Manager for managing the i2c.
        PeripheralManager manager = PeripheralManager.getInstance();

        /*
          get available i2c pin list.
          i2c name format - i2c-#, and n2/c4 have I2C-1 and I2C-2.
          If given i2c name is in list, use it.
         */
        List<String> i2cList = manager.getI2cBusList();

        try {
            String i2cBusName;

            if (i2cList.contains(i2c))
                i2cBusName = i2c;
            else
                i2cBusName = i2cList.get(0);

            si1132 = new SI1132(i2cBusName);
            bme280 = new BME280(i2cBusName,
                    BME280.POWER_MODE.NORMAL,
                    BME280.OVERSAMPLING.X2, BME280.OVERSAMPLING.X2, BME280.OVERSAMPLING.X2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Ultra violet index value.
     * @return Ultra violet index value.
     * @throws Exception IOException and InterruptException.
     */
    public double readUV() throws Exception {
        return si1132.readUV();
    }

    /**
     * Get visible light ambient value.
     * @return visible light ambient value.
     * @throws Exception IOException and InterruptException.
     */
    public double readVisible() throws Exception {
        return si1132.readVisible();
    }

    /**
     * Get infrared light ambient value.
     * @return infrared light ambient value.
     * @throws Exception IOException and InterruptException.
     */
    public double readIR() throws Exception {
        return si1132.readIR();
    }

    /**
     * Get humidity
     * @return humidity value.
     * @throws Exception IOException.
     */
    public double readHumidity() throws Exception {
        return bme280.readHumidity();
    }

    /**
     * Get temperature, celsius.
     * @return temperature value, celsius.
     * @throws Exception IOException.
     */
    public double readTemperatureC() throws Exception {
        return bme280.readTemperatureC();
    }

    /**
     * Get temperature, fahrenheit.
     * @return temperature value, fahrenheit.
     * @throws Exception IOException.
     */
    public double readTemperatureF() throws Exception {
        return bme280.readTemperatureF();
    }

    /**
     * Get pressure.
     * @return pressure value, hPa.
     * @throws Exception IOException.
     */
    public double readPressure() throws Exception {
        return bme280.readPressure();
    }

    /**
     * Calculate altitude from pressure and sea level.
     * @param pressure captured by readPressure method value.
     * @param seaLevel sea level to calculate altitude.
     * @return altitude, Meter.
     */
    public double readAltitude(double pressure, double seaLevel) {
        return 44330.0 * (1.0 - Math.pow(pressure/seaLevel, 0.1903));
    }

    /**
     * Close Si1132 and BME280.
     * @throws Exception IOException
     */
    @Override
    public void close() throws Exception {
        si1132.close();
        bme280.close();
    }
}
