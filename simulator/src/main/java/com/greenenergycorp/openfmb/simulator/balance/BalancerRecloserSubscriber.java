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
package com.greenenergycorp.openfmb.simulator.balance;

import com.greenenergycorp.openfmb.mapping.adapter.PayloadObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.xml.RecloserEventProfile;
import com.greenenergycorp.openfmb.xml.SwitchStatusKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalancerRecloserSubscriber implements PayloadObserver {
    private final static Logger logger = LoggerFactory.getLogger(BalancerRecloserSubscriber.class);

    private final RecloserStatusObserver observer;
    private final OpenFmbXmlMarshaller marshaller;
    private final String recloserId;

    public BalancerRecloserSubscriber(RecloserStatusObserver observer, OpenFmbXmlMarshaller marshaller, String recloserId) {
        this.observer = observer;
        this.marshaller = marshaller;
        this.recloserId = recloserId;
    }

    public void handle(byte[] bytes) {
        try {

            final Object object = marshaller.unmarshal(bytes);
            if (object instanceof RecloserEventProfile) {
                final RecloserEventProfile profile = (RecloserEventProfile) object;

                final String logicalDeviceId = profile.getLogicalDeviceID();

                if (logicalDeviceId.equals(recloserId) && profile.getRecloserStatus() != null) {
                    observer.updateRecloserStatus(profile.getRecloserStatus().getSwitchStatus() == SwitchStatusKind.CLOSED);
                }
            }
        } catch (Throwable ex) {
            logger.warn("Error handling reading: " + ex);
        }
    }
}
