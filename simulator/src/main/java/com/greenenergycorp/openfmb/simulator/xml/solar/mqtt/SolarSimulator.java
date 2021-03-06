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
package com.greenenergycorp.openfmb.simulator.xml.solar.mqtt;

import com.greenenergycorp.openfmb.mapping.adapter.MessageObserver;
import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.mapping.mqtt.*;
import com.greenenergycorp.openfmb.simulator.DailyInterpolatedData;
import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.simulator.LineValueDataLoader;
import com.greenenergycorp.openfmb.simulator.PropertyUtil;
import com.greenenergycorp.openfmb.simulator.solar.SolarObserver;
import com.greenenergycorp.openfmb.simulator.solar.SolarSimLoop;
import com.greenenergycorp.openfmb.simulator.xml.solar.SolarPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SolarSimulator {

    private final static Logger logger = LoggerFactory.getLogger(SolarSimulator.class);

    public static void main(String[] args) throws Exception {

        final String simConfigPath = System.getProperty("config.sim.path", "solarsim.properties");

        final Properties simProps = PropertyUtil.optionallyLoad(simConfigPath, System.getProperties());

        final String logicalDeviceId = PropertyUtil.propOrThrow(simProps, "device.logicalDeviceID");
        final String mRid = PropertyUtil.propOrThrow(simProps, "device.mRID");
        final String name = PropertyUtil.propOrThrow(simProps, "device.name");
        final String description = PropertyUtil.propOrThrow(simProps, "device.description");

        final double scale = PropertyUtil.propDoubleOrThrow(simProps, "value.scale");
        final double offset = PropertyUtil.propDoubleOrThrow(simProps, "value.offset");
        final double jitterChance = PropertyUtil.propDoubleOrThrow(simProps, "value.jitterChance");
        final double jitterPercent = PropertyUtil.propDoubleOrThrow(simProps, "value.jitterPercent");

        final long intervalMs = PropertyUtil.propLongOrThrow(simProps, "config.intervalMs");

        final String solarReadTopic = PropertyUtil.propOrThrow(simProps, "topic.SolarReadingProfile");
        final String solarEventTopic = PropertyUtil.propOrThrow(simProps, "topic.SolarEventProfile");

        final String dataFilename = PropertyUtil.propOrThrow(simProps, "data.file");

        final DeviceId deviceId = new DeviceId(logicalDeviceId, mRid, name, description);

        final double[] dayData = LineValueDataLoader.load(dataFilename);

        final DailyInterpolatedData dataSource = new DailyInterpolatedData(dayData);

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

        mqttThread.start();

        final MessageObserver messageObserver = new MessageObserverAdapter(mqttObserver, new SimpleTopicMapping());

        final SolarObserver solarPublisher = new SolarPublisher(messageObserver, deviceId, openFmbXmlMarshaller, solarReadTopic, solarEventTopic);

        SolarSimLoop.loop(solarPublisher, dataSource, intervalMs, scale, offset, jitterChance, jitterPercent);
    }

}
