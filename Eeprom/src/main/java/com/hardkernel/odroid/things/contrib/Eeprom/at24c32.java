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

package com.hardkernel.odroid.things.contrib.Eeprom;

import java.io.IOException;

/**
 * Driver for at24c32. it based on the at24c driver.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class at24c32 extends at24c {
    /**
     * create new at24c32 driver with i2c bus name and at24c32's address.
     * @param i2cBus set the android thing i2c bus name to use it.
     * @param address address of at24c32, it must include A_PREFIX value(0x1010000).
     * @throws IOException exception on android things sequence of opening a i2c.
     * @throws IllegalArgumentException address value is not start with A_PREFIX and not fit format.
     */
    public at24c32(String i2cBus, int address)
            throws IOException, IllegalArgumentException {
        super(i2cBus, address, 32768/8);
        if ((address & (A_PREFIX | A111)) == 0) {
            close();
            throw new IllegalArgumentException("Wrong address");
        }
        wr_buffer_size = 32;
        wait_time = 25;
    }

    public at24c32(String i2cBus, String[] addressGpios, int address)
            throws IOException, IllegalArgumentException {
        super(i2cBus, addressGpios, address, 32768/8);
        if (addressGpios.length != 3) {
            close();
            throw new IllegalArgumentException("Address GPIOs must be three");
        }

        wr_buffer_size = 32;
        wait_time = 25;
    }

    @Override
    protected byte[] _read(int offset, int size)
        throws InterruptedException, IOException {
        return super._read(addr_16 | offset, size);
    }

    @Override
    protected void _write(int offset, byte[] val, int size)
            throws InterruptedException, IOException {
        super._write(addr_16 | offset, val, size);
    }
}