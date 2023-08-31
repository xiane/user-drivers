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

import java.io.IOException;

/**
 * Basic Driver for I2c based Shtc3.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Shtc3 extends Shtc1 {
    protected final short SHTC3_ID_MASK = 0x83F;
    protected final short SHTC3_ID_BIT = 0x807;

    public Shtc3(String i2cBus) throws IOException {
        super(i2cBus);

        setIdMask(SHTC3_ID_MASK, SHTC3_ID_BIT);
    }

    @Override
    protected int getWaitTime() {
        switch (precision) {
            case Low:
                return 1;
            case High:
                return 12;
            default:
                return 0;
        }
    }
}
