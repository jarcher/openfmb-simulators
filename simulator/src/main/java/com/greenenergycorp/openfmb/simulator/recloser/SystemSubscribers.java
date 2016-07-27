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
package com.greenenergycorp.openfmb.simulator.recloser;

import com.greenenergycorp.openfmb.mapping.adapter.PayloadObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemSubscribers {


    public static class RecloserControlSubscriber implements PayloadObserver {
        private final static Logger logger = LoggerFactory.getLogger(RecloserControlSubscriber.class);

        private final RecloserMachine machine;
        private final OpenFmbXmlMarshaller marshaller;
        private final String logicalDeviceId;

        public RecloserControlSubscriber(RecloserMachine machine, OpenFmbXmlMarshaller marshaller, String logicalDeviceId) {
            this.machine = machine;
            this.marshaller = marshaller;
            this.logicalDeviceId = logicalDeviceId;
        }


        public void handle(byte[] bytes) {
            try {

                final Object object = marshaller.unmarshal(bytes);
                if (object instanceof RecloserControlProfile) {
                    final RecloserControlProfile controlProfile = (RecloserControlProfile) object;

                    if (controlProfile.getLogicalDeviceID().equals(logicalDeviceId)) {

                        if (controlProfile.getRecloserControl() != null && controlProfile.getRecloserControl().getEndDeviceControlType() != null) {

                            final EndDeviceControlType control = controlProfile.getRecloserControl().getEndDeviceControlType();
                            if (control.getAction().trim().equals("trip")) {
                                machine.handleOpen();
                            } else if (control.getAction().trim().equals("close")) {
                                machine.handleClose();
                            }
                        }

                    }
                }
            } catch (Throwable ex) {
                logger.warn("Error handling setpoint: " + ex);
            }
        }
    }

    public static class BatteryReadSubscriber implements PayloadObserver {
        private final static Logger logger = LoggerFactory.getLogger(BatteryReadSubscriber.class);

        private final SystemPowerObserver machine;
        private final OpenFmbXmlMarshaller marshaller;

        public BatteryReadSubscriber(SystemPowerObserver machine, OpenFmbXmlMarshaller marshaller) {
            this.machine = machine;
            this.marshaller = marshaller;
        }


        public void handle(byte[] bytes) {
            try {

                final Object object = marshaller.unmarshal(bytes);
                if (object instanceof BatteryReadingProfile) {
                    final BatteryReadingProfile profile = (BatteryReadingProfile) object;

                    final String logicalDeviceId = profile.getLogicalDeviceID();

                    for (final Reading r: profile.getReadings()) {
                        if (r.getReadingType().getUnit() == UnitSymbolKind.W) {
                            final double value = (double) r.getValue();
                            machine.updateBatteryPower(logicalDeviceId, value);
                        }
                    }
                }
            } catch (Throwable ex) {
                logger.warn("Error handling reading: " + ex);
            }
        }
    }

    public static class SolarReadSubscriber implements PayloadObserver {
        private final static Logger logger = LoggerFactory.getLogger(SolarReadSubscriber.class);

        private final SystemPowerObserver machine;
        private final OpenFmbXmlMarshaller marshaller;

        public SolarReadSubscriber(SystemPowerObserver machine, OpenFmbXmlMarshaller marshaller) {
            this.machine = machine;
            this.marshaller = marshaller;
        }


        public void handle(byte[] bytes) {
            try {

                final Object object = marshaller.unmarshal(bytes);
                if (object instanceof SolarReadingProfile) {
                    final SolarReadingProfile profile = (SolarReadingProfile) object;

                    final String logicalDeviceId = profile.getLogicalDeviceID();

                    for (final Reading r: profile.getReadings()) {
                        if (r.getReadingType().getUnit() == UnitSymbolKind.W) {
                            final double value = (double) r.getValue();
                            machine.updateSolarPower(logicalDeviceId, value);
                        }
                    }
                }
            } catch (Throwable ex) {
                logger.warn("Error handling reading: " + ex);
            }
        }
    }

    public static class ResourceReadSubscriber implements PayloadObserver {
        private final static Logger logger = LoggerFactory.getLogger(ResourceReadSubscriber.class);

        private final SystemPowerObserver machine;
        private final OpenFmbXmlMarshaller marshaller;

        public ResourceReadSubscriber(SystemPowerObserver machine, OpenFmbXmlMarshaller marshaller) {
            this.machine = machine;
            this.marshaller = marshaller;
        }


        public void handle(byte[] bytes) {
            try {

                final Object object = marshaller.unmarshal(bytes);
                if (object instanceof ResourceReadingProfile) {
                    final ResourceReadingProfile profile = (ResourceReadingProfile) object;

                    final String logicalDeviceId = profile.getLogicalDeviceID();

                    for (final Reading r: profile.getReadings()) {
                        if (r.getReadingType().getUnit() == UnitSymbolKind.W) {
                            final double value = (double) r.getValue();
                            machine.updateLoadPower(logicalDeviceId, value);
                        }
                    }
                }
            } catch (Throwable ex) {
                logger.warn("Error handling reading: " + ex);
            }
        }
    }
}
