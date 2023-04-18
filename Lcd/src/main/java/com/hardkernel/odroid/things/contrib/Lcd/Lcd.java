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

package com.hardkernel.odroid.things.contrib.Lcd;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.List;

/**
 * Lcd Character Display module Driver via I2C bus.
 * basic target device is 2004A LCD Display.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Lcd implements AutoCloseable{
    private final I2cDevice device;

    /**
     * max_line
     */
    private int max_line;
    private int max_length;

    final private static int I2C_ADDRESS = 0x27;
    final private static byte LCD_CHR = 1;
    final private static byte LCD_CMD = 0;
    final private static byte ENABLE = 0b00000100;

    /**
     * Lcd commands
     */
    final private static int LCD_CLEAR_DISPLAY = 0x01;
    final private static int LCD_RETURN_HOME = 0x02;
    final private static int LCD_ENTRY_MODE_SET = 0x04;
    final private static int LCD_DISPLAY_CONTROL = 0x08;
    final private static int LCD_CURSOR_SHIFT = 0x10;
    final private static int LCD_FUNCTION_SET = 0x20;
    final private static int LCD_SET_CGRAM_ADDRESS = 0x40;
    final private static int LCD_SET_DDRAM_ADDRESS = 0x80;
    /**
     * flags for display entry mode
     */
    final private static int LCD_ENTRY_RIGHT = 0x00;
    final private static int LCD_ENTRY_LEFT = 0x02;
    final private static int LCD_ENTRY_SHIFT_INCREMENT = 0x01;
    final private static int LCD_ENTRY_SHIFT_DECREMENT = 0x00;

    /**
     * flags for display on/off control
     */
    final private static int LCD_DISPLAY_ON = 0x04;
    final private static int LCD_DISPLAY_OFF = 0x00;
    final private static int LCD_CURSOR_ON = 0x02;
    final private static int LCD_CURSOR_OFF = 0x00;
    final private static int LCD_BLINK_ON = 0x01;
    final private static int LCD_BLINK_OFF = 0x00;

    /**
     * flags for display/cursor shift
     */
    final private static int LCD_DISPLAY_MOVE = 0x08;
    final private static int LCD_CURSOR_MOVE = 0x00;
    final private static int LCD_MOVE_RIGHT = 0x04;
    final private static int LCD_MOVE_LEFT = 0x00;

    /**
     * flags for function set
     */
    final private static int LCD_8BIT_MODE = 0x10;
    final private static int LCD_4BIT_MODE = 0x00;
    final private static int LCD_2LINE = 0x08;
    final private static int LCD_1LINE = 0x00;
    final private static int LCD_5x10DOTS = 0x04;
    final private static int LCD_5x8DOTS = 0x00;

    /**
     * flags for backlight control
     */
    final private static int LCD_BACKLIGHT = 0x08;
    final private static int LCD_NO_BACKLIGHT = 0x00;

    final private static byte LINE_1 = (byte) 0x80;
    final private static byte LINE_2 = (byte) 0xC0;
    final private static byte LINE_3 = (byte) 0x94;
    final private static byte LINE_4 = (byte) 0xD4;

    protected static byte[] LINE_TARGET = {0, LINE_1, LINE_2, LINE_3, LINE_4};

    /**
     * Create Lcd instance to use LCD Character Display device.
     * @param i2cBus connected i2c bus to use LCD.
     * @param col targeting display's max length in line. (max 20)
     * @param row targeting display's max line. (max 4)
     * @throws IOException I2C command exception.
     * @throws InterruptedException Exception when sleep.
     * @throws IndexOutOfBoundsException Exception about wrong row value.
     */
    public Lcd(String i2cBus, int col, int row)
            throws IOException, InterruptedException, IndexOutOfBoundsException {
        // get Peripheral Manager for managing the i2c device.
        PeripheralManager manager = PeripheralManager.getInstance();

        List<String> i2cBusList = manager.getI2cBusList();

        /*
          get available i2c pin list.
          i2c name format - I2C-#, and n2/c4 have I2C-1 and I2C-2.
          In this case use given bus. if given bus is not in list, use default one.
         */
        if (i2cBusList.contains(i2cBus))
            device = manager.openI2cDevice(i2cBus, I2C_ADDRESS);
        else
            device = manager.openI2cDevice(i2cBusList.get(0), I2C_ADDRESS);

        init(col, row);
    }

    private void init(int col, int row)
            throws IOException, InterruptedException, IndexOutOfBoundsException {
        if (row > 4)
            throw new IndexOutOfBoundsException("Out of support, 4 line is max.");
        max_line = row;
        max_length = col;

        write_cmd(0x03);
        write_cmd(0x03);
        write_cmd(0x03);
        write_cmd(0x02);

        write_cmd(LCD_ENTRY_MODE_SET | LCD_ENTRY_LEFT);
        write_cmd(LCD_FUNCTION_SET | LCD_2LINE | LCD_5x8DOTS | LCD_4BIT_MODE);
        write_cmd(LCD_DISPLAY_CONTROL | LCD_DISPLAY_ON);
        write_cmd(LCD_CLEAR_DISPLAY);
        Thread.sleep(5);
    }

    /**
     * Write command to Lcd.
     * @param bits command instruction.
     * @throws IOException I2C command exception.
     */
    protected void write_cmd(int bits) throws IOException {
        lcd_byte(bits, LCD_CMD);
    }

    private void lcd_byte(int bits, byte mode) throws IOException {
        byte bits_high = (byte) (mode | (bits & 0xF0) | LCD_BACKLIGHT);
        byte bits_low = (byte) (mode | (bits << 4) & 0xF0 | LCD_BACKLIGHT);
        device.writeRegByte(0, bits_high);
        lcd_toogle_enable(bits_high);
        device.writeRegByte(0, bits_low);
        lcd_toogle_enable(bits_low);
    }

    private void lcd_toogle_enable(byte bits) throws IOException {
        device.writeRegByte(0, (byte) (bits | ENABLE));
        device.writeRegByte(0, (byte) (bits & ~ENABLE));
    }

    /**
     * print character with line number.
     * msg size should shorter then fisrt noticed col size. if it is over, msg will be cut.
     * @param msg print message.
     * @param line target print line. it must be in the row size.
     * @throws IOException line is bigger then row, or under the zero.
     */
    public void print(String msg, int line) throws IOException {
        if (line > max_line || line <= 0)
            throw new IOException("Out of line");

        write_cmd(LINE_TARGET[line]);

        if (msg.length() > max_length)
            msg = msg.substring(0, max_length);

        for(char ch: msg.toCharArray()) {
            lcd_byte(ch, LCD_CHR);
        }
    }

    /**
     * stop LCD.
     * @throws IOException I2C bus command exception.
     */
    @Override
    public void close() throws IOException {
        device.close();
    }
}
