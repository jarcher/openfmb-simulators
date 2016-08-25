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

public class BatterySimulator {

    private final static Logger logger = LoggerFactory.getLogger(BatterySimulator.class);

    public static void main(String[] args) throws Exception {

        final String simConfigPath = System.getProperty("config.sim.path", "batterysim.properties");

        final Properties simProps = PropertyUtil.optionallyLoad(simConfigPath, System.getProperties());

        final String logicalDeviceId = PropertyUtil.propOrThrow(simProps, "device.logicalDeviceID");
        final String mRid = PropertyUtil.propOrThrow(simProps, "device.mRID");
        final String name = PropertyUtil.propOrThrow(simProps, "device.name");
        final String description = PropertyUtil.propOrThrow(simProps, "device.description");

        final DeviceId deviceId = new DeviceId(logicalDeviceId, mRid, name, description);

        final String batteryReadTopic = PropertyUtil.propOrThrow(simProps, "topic.BatteryReadingProfile");
        final String batteryEventTopic = PropertyUtil.propOrThrow(simProps, "topic.BatteryEventProfile");
        final String batteryControlTopic = PropertyUtil.propOrThrow(simProps, "topic.BatteryControlProfile");

        final double batteryMaxChargeRatekW = PropertyUtil.propDoubleOrThrow(simProps, "battery.maxChargeRatekW");
        final double batteryMaxDischargeRatekW = PropertyUtil.propDoubleOrThrow(simProps, "battery.maxDischargeRatekW");
        final double batteryEnergyRatingkWh = PropertyUtil.propDoubleOrThrow(simProps, "battery.energyRatingkWh");
        final double batteryEnergyMaxkWh = PropertyUtil.propDoubleOrThrow(simProps, "battery.energyMaxkWh");
        final double batteryEnergyMinkWh = PropertyUtil.propDoubleOrThrow(simProps, "battery.energyMinkWh");
        final double batteryEfficiencyRatio = PropertyUtil.propDoubleOrThrow(simProps, "battery.efficiencyRatio");
        final double batteryVoltage = PropertyUtil.propDoubleOrThrow(simProps, "battery.voltage");
        final double batteryHertz = PropertyUtil.propDoubleOrThrow(simProps, "battery.hertz");

        final long intervalMs = PropertyUtil.propLongOrThrow(simProps, "config.intervalMs");

        final BatteryMachine.BatterySpec batterySpec = new BatteryMachine.BatterySpec(
                batteryMaxChargeRatekW,
                batteryMaxDischargeRatekW,
                batteryEnergyRatingkWh,
                batteryEnergyMaxkWh,
                batteryEnergyMinkWh,
                batteryEfficiencyRatio,
                batteryVoltage,
                batteryHertz);

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

        final BatteryPublisher batteryPublisher = new BatteryPublisher(messageObserver, deviceId, openFmbXmlMarshaller, batteryReadTopic, batteryEventTopic);

        final BatteryMachine batterySimulator = new BatteryMachine(batteryPublisher, intervalMs, batterySpec);

        final PayloadObserver controlObserver = new BatterySimPayloadObserver(openFmbXmlMarshaller, logicalDeviceId, batterySimulator);

        final Map<String, PayloadObserver> controlHandlerMap = new HashMap<String, PayloadObserver>();
        controlHandlerMap.put(batteryControlTopic + "/" + logicalDeviceId, controlObserver);

        mqttAdapterManager.subscribe(controlHandlerMap);

        mqttThread.start();
        batterySimulator.run();
    }


}
