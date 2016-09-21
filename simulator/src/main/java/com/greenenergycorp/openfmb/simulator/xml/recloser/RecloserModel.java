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
package com.greenenergycorp.openfmb.simulator.xml.recloser;

import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.simulator.xml.ModelCommon;
import com.greenenergycorp.openfmb.xml.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class RecloserModel {


    public static Recloser buildRecloserDescription(final DeviceId id) {
        final Recloser description = new Recloser();
        description.setMRID(id.getmRid());
        description.setName(id.getName());
        description.setDescription(id.getDescription());
        return description;
    }

    public static RecloserReadingProfile buildRecloserRead(final DeviceId id, final List<Reading> readings) throws Exception {
        final long now = System.currentTimeMillis();

        final RecloserReadingProfile profile = new RecloserReadingProfile();

        profile.setLogicalDeviceID(id.getLogicalDeviceId());
        profile.setTimestamp(ModelCommon.xmlTimeFor(now));
        profile.setRecloser(buildRecloserDescription(id));

        for (Reading r: readings) {
            profile.getReadings().add(r);
        }

        return profile;
    }

    public static RecloserEventProfile buildRecloserEvent(
            final DeviceId id,
            final boolean isClosed,
            final boolean isBlocked) throws DatatypeConfigurationException {

        final long now = System.currentTimeMillis();
        final XMLGregorianCalendar xmlTime = ModelCommon.xmlTimeFor(now);

        final RecloserEventProfile profile = new RecloserEventProfile();

        profile.setLogicalDeviceID(id.getLogicalDeviceId());
        profile.setTimestamp(xmlTime);
        profile.setRecloser(buildRecloserDescription(id));

        final RecloserStatus recloserStatus = new RecloserStatus();
        recloserStatus.setIsBlocked(isBlocked);
        recloserStatus.setSwitchStatus(isClosed ? SwitchStatusKind.CLOSED : SwitchStatusKind.OPEN);

        recloserStatus.setValue("");
        recloserStatus.setTimestamp(xmlTime);

        recloserStatus.setQualityFlag(new byte[] {0, 0});

        profile.setRecloserStatus(recloserStatus);

        return profile;
    }
}
