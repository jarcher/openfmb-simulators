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
package com.greenenergycorp.openfmb.simulator.xml.battery;

import com.greenenergycorp.openfmb.mapping.adapter.MessageObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.simulator.battery.BatteryObserver;
import com.greenenergycorp.openfmb.simulator.xml.ModelCommon;
import com.greenenergycorp.openfmb.xml.*;

import java.util.Arrays;

public class BatteryPublisher implements BatteryObserver {

    private final MessageObserver messageObserver;
    private final DeviceId deviceId;
    private final OpenFmbXmlMarshaller marshaller;
    private final String readTopic;
    private final String eventTopic;

    public BatteryPublisher(MessageObserver messageObserver, DeviceId deviceId, OpenFmbXmlMarshaller marshaller, String readTopic, String eventTopic) {
        this.messageObserver = messageObserver;
        this.deviceId = deviceId;
        this.marshaller = marshaller;
        this.readTopic = readTopic;
        this.eventTopic = eventTopic;
    }

    public void batteryReadUpdate(double power, double volts, double freq) throws Exception {
        final long now = System.currentTimeMillis();

        final BatteryReadingProfile readProfile = BatteryModel.buildBatteryRead(deviceId, Arrays.asList(
                ModelCommon.buildReading(power, now, UnitSymbolKind.W, UnitMultiplierKind.KILO, FlowDirectionKind.TOTAL, PhaseCodeKind.ABCN),
                ModelCommon.buildReading(volts, now, UnitSymbolKind.V, UnitMultiplierKind.NO_MULTIPLIER, FlowDirectionKind.TOTAL, PhaseCodeKind.ABCN),
                ModelCommon.buildReading(freq, now, UnitSymbolKind.HZ, UnitMultiplierKind.NO_MULTIPLIER, FlowDirectionKind.TOTAL, PhaseCodeKind.ABCN)
        ));

        final byte[] payloadBytes = marshaller.marshal(readProfile);
        messageObserver.publish(payloadBytes, readTopic, deviceId.getLogicalDeviceId());
    }

    public void batteryEventUpdate(boolean isConnected, boolean isCharging, String mode, double stateOfCharge) throws Exception {
        final long now = System.currentTimeMillis();

        final BatteryEventProfile eventProfile = BatteryModel.buildBatteryEvent(deviceId, isConnected, isCharging, mode, stateOfCharge);

        final byte[] payloadBytes = marshaller.marshal(eventProfile);
        messageObserver.publish(payloadBytes, eventTopic, deviceId.getLogicalDeviceId());
    }
}
