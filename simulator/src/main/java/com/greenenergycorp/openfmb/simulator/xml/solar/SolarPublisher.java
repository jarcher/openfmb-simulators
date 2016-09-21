/**
 * Copyright 2016 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. Green Energy
 * Corp licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.greenenergycorp.openfmb.simulator.xml.solar;

import com.greenenergycorp.openfmb.mapping.adapter.MessageObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.simulator.solar.SolarObserver;
import com.greenenergycorp.openfmb.xml.SolarEventProfile;
import com.greenenergycorp.openfmb.xml.SolarReadingProfile;

public class SolarPublisher implements SolarObserver {
    private final MessageObserver messageObserver;
    private final DeviceId deviceId;
    private final OpenFmbXmlMarshaller marshaller;
    private final String readTopic;
    private final String eventTopic;

    public SolarPublisher(MessageObserver messageObserver, DeviceId deviceId, OpenFmbXmlMarshaller marshaller, String readTopic, String eventTopic) {
        this.messageObserver = messageObserver;
        this.deviceId = deviceId;
        this.marshaller = marshaller;
        this.readTopic = readTopic;
        this.eventTopic = eventTopic;
    }

    public void solarReadUpdate(final double outputPower) throws Exception {
        final SolarReadingProfile read = SolarModel.buildSolarRead(deviceId, outputPower);
        final byte[] readBytes = marshaller.marshal(read);
        messageObserver.publish(readBytes, readTopic, deviceId.getLogicalDeviceId());
    }

    public void solarEventUpdate() throws Exception {
        final SolarEventProfile event = SolarModel.buildSolarEvent(deviceId);
        final byte[] eventBytes = marshaller.marshal(event);
        messageObserver.publish(eventBytes, eventTopic, deviceId.getLogicalDeviceId());
    }

}
