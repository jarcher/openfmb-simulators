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

public class SolarModel {

    public static SolarInverter buildSolarDescription(DeviceId id) {
        final SolarInverter description = new SolarInverter();
        description.setMRID(id.getmRid());
        description.setName(id.getName());
        description.setDescription(id.getDescription());
        return description;
    }

    public static SolarReadingProfile buildSolarRead(DeviceId id, double power) throws Exception {

        final long now = System.currentTimeMillis();

        final SolarReadingProfile profile = new SolarReadingProfile();

        profile.setLogicalDeviceID(id.getLogicalDeviceId());
        profile.setTimestamp(ModelCommon.xmlTimeFor(now));
        profile.setSolarInverter(buildSolarDescription(id));

        final Reading reading = ModelCommon.buildReading(
                power,
                now,
                UnitSymbolKind.W,
                UnitMultiplierKind.KILO,
                FlowDirectionKind.TOTAL,
                PhaseCodeKind.ABCN);

        profile.getReadings().add(reading);

        return profile;
    }

    public static SolarEventProfile buildSolarEvent(DeviceId id) throws Exception {

        final long now = System.currentTimeMillis();

        final SolarEventProfile profile = new SolarEventProfile();

        profile.setLogicalDeviceID(id.getLogicalDeviceId());
        profile.setTimestamp(ModelCommon.xmlTimeFor(now));
        profile.setSolarInverter(buildSolarDescription(id));

        final SolarInverterStatus status = new SolarInverterStatus();
        status.setIsConnected(true);
        status.setValue("");
        status.setTimestamp(ModelCommon.xmlTimeFor(now));
        status.setQualityFlag(new byte[]{0, 0});

        profile.setSolarInverterStatus(status);

        return profile;
    }
}
