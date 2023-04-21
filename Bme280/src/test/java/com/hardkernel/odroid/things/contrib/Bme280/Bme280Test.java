package com.hardkernel.odroid.things.contrib.Bme280;

import com.google.android.things.pio.I2cDevice;

import org.junit.Test;

import org.mockito.Mock;

import java.io.IOException;

public class Bme280Test {
    @Mock
    I2cDevice i2c;

    @Test
    public void correctInit() throws IOException, IllegalAccessException, InterruptedException {
        Bme280 bme280 = new Bme280(i2c);
        bme280.readTemperatureC();
    }
}
