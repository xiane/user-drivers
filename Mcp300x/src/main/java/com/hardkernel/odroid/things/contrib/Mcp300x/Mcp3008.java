package com.hardkernel.odroid.things.contrib.Mcp300x;

import java.io.IOException;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Mcp3008 extends Mcp300x {
    /**
     * Create Mcp300x instance and initialize.
     * the Default frequency is based on 2.7 V Vdd.
     *
     * @param spiBusName android things spi target bus name.
     * @throws IOException          caused from i2c procedure.
     */
    public Mcp3008(String spiBusName) throws IOException {
        super(spiBusName);
    }

    /**
     * Read single channel ADC from mcp3004.
     * Channel must be 0 to 7.
     * @param channel target channel number.
     * @param adcMode target adc mode. single-end or differential.
     * @return result value. 0 to 1024.
     * @throws IOException caused from spi procedure.
     */
    @Override
    public int readADC(int channel, boolean adcMode) throws IOException, IllegalArgumentException {
        if (channel < 0 || channel > 7) {
            throw new IllegalAccessError("channel " + channel + " is out of target.");
        }
        return super.readADC(channel, adcMode);
    }
}
