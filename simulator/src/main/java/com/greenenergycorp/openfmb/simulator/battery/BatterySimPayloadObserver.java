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
package com.greenenergycorp.openfmb.simulator.battery;

import com.greenenergycorp.openfmb.mapping.adapter.PayloadObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.xml.BatteryControlProfile;
import com.greenenergycorp.openfmb.xml.SetPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatterySimPayloadObserver implements PayloadObserver {
    private final static Logger logger = LoggerFactory.getLogger(BatterySimPayloadObserver.class);

    private final OpenFmbXmlMarshaller openFmbXmlMarshaller;
    private final String logicalDeviceId;
    private final BatteryMachine batterySimulator;

    public BatterySimPayloadObserver(OpenFmbXmlMarshaller openFmbXmlMarshaller, String logicalDeviceId, BatteryMachine batterySimulator) {
        this.openFmbXmlMarshaller = openFmbXmlMarshaller;
        this.logicalDeviceId = logicalDeviceId;
        this.batterySimulator = batterySimulator;
    }

    public void handle(byte[] bytes) {
        try {

            final Object object = openFmbXmlMarshaller.unmarshal(bytes);
            if (object instanceof BatteryControlProfile) {
                final BatteryControlProfile controlProfile = (BatteryControlProfile) object;

                if (controlProfile.getLogicalDeviceID().equals(logicalDeviceId)) {
                    if (controlProfile.getBatterySystemControl().isIsIslanded()) {
                        batterySimulator.setModeControl(BatteryMachine.BatteryMode.ISLANDED);
                    }

                    for (final SetPoint sp : controlProfile.getBatterySystemControl().getSetPoints()) {
                        if (sp.getControlType() != null) {

                            if (sp.getControlType().equals("SetMode")) {
                                final int modeInt = sp.getValue().intValue();
                                final BatteryMachine.BatteryMode batteryMode = BatteryMachine.BatteryMode.fromInt(modeInt);
                                if (batteryMode != null) {
                                    batterySimulator.setModeControl(batteryMode);
                                }
                            }

                            if (sp.getControlType().equals("SetRealPower")) {
                                batterySimulator.setPowerSetpoint(sp.getValue().doubleValue());
                            }
                        }
                    }

                }


            }

        } catch (Throwable ex) {
            logger.warn("Error handling setpoint: " + ex);
        }
    }
}