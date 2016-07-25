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
package com.greenenergycorp.openfmb.simulator.xml;

import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.xml.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class BatteryModel {


    public static BatterySystem buildBatteryDescription(final DeviceId id) {
        final BatterySystem description = new BatterySystem();
        description.setMRID(id.getmRid());
        description.setName(id.getName());
        description.setDescription(id.getDescription());
        return description;
    }

    public static BatteryReadingProfile buildBatteryRead(final DeviceId id, final List<Reading> readings) throws Exception {
        final long now = System.currentTimeMillis();

        final BatteryReadingProfile profile = new BatteryReadingProfile();

        profile.setLogicalDeviceID(id.getLogicalDeviceId());
        profile.setTimestamp(ModelCommon.xmlTimeFor(now));
        profile.setBatterySystem(buildBatteryDescription(id));

        for (Reading r: readings) {
            profile.getReadings().add(r);
        }

        return profile;
    }

    public static BatteryEventProfile buildBatteryEvent(
            final DeviceId id,
            final boolean isConnected,
            final boolean isCharging,
            final String mode,
            final double stateOfCharge) throws DatatypeConfigurationException {

        final long now = System.currentTimeMillis();
        final XMLGregorianCalendar xmlTime = ModelCommon.xmlTimeFor(now);

        final BatteryEventProfile profile = new BatteryEventProfile();

        profile.setLogicalDeviceID(id.getLogicalDeviceId());
        profile.setTimestamp(xmlTime);
        profile.setBatterySystem(buildBatteryDescription(id));

        final BatteryStatus batteryStatus = new BatteryStatus();
        batteryStatus.setIsConnected(isConnected);
        batteryStatus.setIsCharging(isCharging);
        batteryStatus.setMode(mode);
        batteryStatus.setStateOfCharge((float) stateOfCharge);

        batteryStatus.setValue("");
        batteryStatus.setTimestamp(xmlTime);

        batteryStatus.setQualityFlag(new byte[] {0, 0});

        profile.setBatteryStatus(batteryStatus);

        return profile;
    }
}
