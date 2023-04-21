package com.hardkernel.odroid.things.contrib.Mcp2515;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.SpiDevice;

import org.junit.Test;

import org.mockito.Mock;

import java.io.IOException;

public class Mcp2515Test {
    @Mock
    SpiDevice spi;

    @Mock
    Gpio gpio;

    @Test
    public void correctInit() throws IOException, IllegalAccessException, InterruptedException {
        Mcp2515 mcp2515 = new Mcp2515(spi, gpio);
    }
}
