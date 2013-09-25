/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.buffer;

import org.jboss.netty.util.internal.DetectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A virtual buffer which shows multiple buffers as a single merged buffer.  It
 * is recommended to use {@link ChannelBuffers#wrappedBuffer(ChannelBuffer...)}
 * instead of calling the constructor explicitly.
 */
public class CompositeChannelBuffer{

    //components保存所有内部ChannelBuffer
    private ChannelBuffer[] components;
    //indices记录在整个CompositeChannelBuffer中，每个components的起始位置
    private int[] indices;
    //缓存上一次读写的componentId
    private int lastAccessedComponentId;

    public byte getByte(int index) {
        //通过indices中记录的位置索引到对应第几个子Buffer
        int componentId = componentId(index);
        return components[componentId].getByte(index - indices[componentId]);
    }

}
