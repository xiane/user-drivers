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

package com.hardkernel.odroid.things.contrib.Mcp2515;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Commands {
    public static final byte RESET = (byte)0xC0;

    public static final byte WRITE = (byte)0x02;
    public static final byte READ = (byte)0x03;

    public static final byte BIT_MODIFY = 0x05;

    public static final byte READ_STATUS = (byte)0xA0;
    public static final byte READ_RX_STATUS = (byte)0xB0;

    public static final byte READ_RX_0_ID = (byte)0x90;
    public static final byte READ_RX_0_DATA = (byte)0x92;
    public static final byte READ_RX_1_ID = (byte)0x94;
    public static final byte READ_RX_1_DATA = (byte)0x96;

    public static final byte LOAD_TX_0_ID = (byte)0x40;
    public static final byte LOAD_TX_0_DATA = (byte)0x41;
    public static final byte LOAD_TX_1_ID = (byte)0x42;
    public static final byte LOAD_TX_1_DATA = (byte)0x43;
    public static final byte LOAD_TX_2_ID = (byte)0x44;
    public static final byte LOAD_TX_2_DATA = (byte)0x45;

    public static final byte RTS = (byte)0x80;
    public static final byte RTS_BUFFER_0 = (byte)0x81;
    public static final byte RTS_BUFFER_1 = (byte)0x82;
    public static final byte RTS_BUFFER_2 = (byte)0x84;

}
