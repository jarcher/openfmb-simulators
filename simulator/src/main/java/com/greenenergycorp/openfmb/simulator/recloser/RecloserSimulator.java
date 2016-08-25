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

import com.greenenergycorp.openfmb.mapping.adapter.MessageObserver;
import com.greenenergycorp.openfmb.mapping.adapter.PayloadObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.mapping.mqtt.*;
import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.simulator.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RecloserSimulator {

    private final static Logger logger = LoggerFactory.getLogger(RecloserSimulator.class);

    public static void main(final String[] args) throws Exception {

        final String simConfigPath = System.getProperty("config.sim.path", "reclosersim.properties");

        final Properties simProps = PropertyUtil.optionallyLoad(simConfigPath, System.getProperties());

        final String logicalDeviceId = PropertyUtil.propOrThrow(simProps, "device.logicalDeviceID");
        final String mRid = PropertyUtil.propOrThrow(simProps, "device.mRID");
        final String name = PropertyUtil.propOrThrow(simProps, "device.name");
        final String description = PropertyUtil.propOrThrow(simProps, "device.description");

        final DeviceId deviceId = new DeviceId(logicalDeviceId, mRid, name, description);

        final String recloserEventTopic = PropertyUtil.propOrThrow(simProps, "topic.RecloserEventProfile");
        final String recloserReadTopic = PropertyUtil.propOrThrow(simProps, "topic.RecloserReadingProfile");
        final String recloserControlTopic = PropertyUtil.propOrThrow(simProps, "topic.RecloserControlProfile");

        final String batteryReadTopic = PropertyUtil.propOrThrow(simProps, "topic.BatteryReadingProfile");
        final String resourceReadTopic = PropertyUtil.propOrThrow(simProps, "topic.ResourceReadingProfile");
        final String solarReadTopic = PropertyUtil.propOrThrow(simProps, "topic.SolarReadingProfile");

        final double voltage = PropertyUtil.propDoubleOrThrow(simProps, "recloser.voltage");
        final double hertz = PropertyUtil.propDoubleOrThrow(simProps, "recloser.hertz");

        final long intervalMs = PropertyUtil.propLongOrThrow(simProps, "config.intervalMs");

        final OpenFmbXmlMarshaller openFmbXmlMarshaller = new OpenFmbXmlMarshaller();

        final String mqttConfigPath = System.getProperty("config.mqtt.path", "mqtt.properties");

        final MqttConfiguration mqttConfiguration = MqttConfiguration.fromFile(mqttConfigPath);

        final MqttAdapterManager mqttAdapterManager = new MqttAdapterManager(mqttConfiguration, 0);

        final MqttObserver mqttObserver = mqttAdapterManager.getMessageObserver();

        final Thread mqttThread = new Thread(new Runnable() {
            public void run() {
                mqttAdapterManager.run();
            }
        }, "mqtt publisher");

        final MessageObserver messageObserver = new MessageObserverAdapter(mqttObserver, new SimpleTopicMapping());

        final RecloserPublisher recloserPublisher = new RecloserPublisher(messageObserver, deviceId, openFmbXmlMarshaller, recloserReadTopic, recloserEventTopic);

        final RecloserMachine machine = new RecloserMachine(recloserPublisher, voltage, hertz, 0.0);

        final Map<String, PayloadObserver> controlHandlerMap = new HashMap<String, PayloadObserver>();
        controlHandlerMap.put(recloserControlTopic + "/" + deviceId.getLogicalDeviceId(), new SystemSubscribers.RecloserControlSubscriber(machine, openFmbXmlMarshaller, logicalDeviceId));
        controlHandlerMap.put(batteryReadTopic + "/#", new SystemSubscribers.BatteryReadSubscriber(machine, openFmbXmlMarshaller));
        controlHandlerMap.put(solarReadTopic + "/#", new SystemSubscribers.SolarReadSubscriber(machine, openFmbXmlMarshaller));
        controlHandlerMap.put(resourceReadTopic + "/#", new SystemSubscribers.ResourceReadSubscriber(machine, openFmbXmlMarshaller));

        mqttAdapterManager.subscribe(controlHandlerMap);

        mqttThread.start();

        logger.info("Pushing updates every " + intervalMs + " ms");
        while (true) {
            machine.push();
            Thread.sleep(intervalMs);
        }

    }
}
