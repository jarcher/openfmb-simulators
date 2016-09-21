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
package com.greenenergycorp.openfmb.simulator.xml.balance;

import com.greenenergycorp.openfmb.mapping.adapter.MessageObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.simulator.balance.BatteryControlIssuer;
import com.greenenergycorp.openfmb.simulator.battery.BatteryMachine;
import com.greenenergycorp.openfmb.simulator.xml.battery.BatteryModel;
import com.greenenergycorp.openfmb.xml.BatteryControlProfile;

import javax.xml.bind.JAXBException;

public class BatteryControlPublisher implements BatteryControlIssuer {

    private final MessageObserver messageObserver;
    private final DeviceId deviceId;
    private final OpenFmbXmlMarshaller marshaller;
    private final String batteryControlTopic;

    public BatteryControlPublisher(MessageObserver messageObserver, DeviceId deviceId, OpenFmbXmlMarshaller marshaller, String batteryControlTopic) {
        this.messageObserver = messageObserver;
        this.deviceId = deviceId;
        this.marshaller = marshaller;
        this.batteryControlTopic = batteryControlTopic;
    }

    public void setIslanded() throws Exception {
        final BatteryControlProfile profile = BatteryModel.buildBatteryControlIsIslanded(deviceId);

        publish(profile);
    }

    public void setPowerSetpoint(final double power) throws Exception {
        final BatteryControlProfile profile = BatteryModel.buildBatteryControlPowerSetpoint(deviceId, power);

        publish(profile);
    }

    public void leaveIslanded() throws Exception {
        final BatteryControlProfile profile = BatteryModel.buildBatteryControlModeSetpoint(deviceId, BatteryMachine.BatteryMode.LEAVING_ISLANDED.getNumber());

        publish(profile);
    }

    private void publish(final BatteryControlProfile profile) throws JAXBException {
        final byte[] payloadBytes = marshaller.marshal(profile);
        messageObserver.publish(payloadBytes, batteryControlTopic, deviceId.getLogicalDeviceId());
    }
}
