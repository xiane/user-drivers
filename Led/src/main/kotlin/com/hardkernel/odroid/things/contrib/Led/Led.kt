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

package com.hardkernel.odroid.things.contrib.Led

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager

import java.io.IOException

/**
 * Simple Led control user Driver over the GPIO.
 */
@Suppress("unused", "WeakerAccess")
class Led
/**
Get a gpio instance and set the direction.
@param pinName target gpio pin name to use Led.
 */
@Throws(IOException::class) constructor(pinName: String) : AutoCloseable {
    private var pin: Gpio? = null

    init {
        pin = PeripheralManager.getInstance()
            .openGpio(pinName)
        pin?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
    }

    /**
     Turn on when true, and turn off when false.
     @param on On/Off state. true is on.
     */
    @Throws(Exception::class)
    fun turn(on: Boolean) {
        pin?.value = on
    }

    /**
     auto close the gpio pin.
     */
    @Throws(IOException::class)
    override fun close() {
        pin?.close()
    }
}