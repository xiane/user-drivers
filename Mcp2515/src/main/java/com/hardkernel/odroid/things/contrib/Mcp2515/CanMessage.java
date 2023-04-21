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

import java.nio.ByteBuffer;

/**
 * CAN message structure.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CanMessage {
    private long id;
    private ByteBuffer data;
    private boolean isRtr;

    /**
     * Create CAN message.
     * @param id
     * @param isRtr
     * @param data
     */
    public CanMessage(long id, boolean isRtr, ByteBuffer data){
        this.data = data;
        this.id = id;
        this.isRtr = isRtr;
    }

    public boolean isRemoteTransmitRequest(){
        return isRtr;
    }

    public long getId(){
        return id;
    }

    public ByteBuffer getData() {
        return data;
    }

    public int getDataLength(){
        return data.capacity();
    }
}
