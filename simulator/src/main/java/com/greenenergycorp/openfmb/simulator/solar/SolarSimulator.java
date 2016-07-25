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
package com.greenenergycorp.openfmb.simulator.solar;

import com.greenenergycorp.openfmb.mapping.data.xml.OpenFmbXmlMarshaller;
import com.greenenergycorp.openfmb.mapping.mqtt.MqttAdapterManager;
import com.greenenergycorp.openfmb.mapping.mqtt.MqttConfiguration;
import com.greenenergycorp.openfmb.mapping.mqtt.MqttObserver;
import com.greenenergycorp.openfmb.simulator.DailyInterpolatedData;
import com.greenenergycorp.openfmb.simulator.DeviceId;
import com.greenenergycorp.openfmb.simulator.LineValueDataLoader;
import com.greenenergycorp.openfmb.simulator.PropertyUtil;
import com.greenenergycorp.openfmb.simulator.xml.SolarModel;
import com.greenenergycorp.openfmb.xml.SolarEventProfile;
import com.greenenergycorp.openfmb.xml.SolarReadingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;

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

        loop(mqttObserver, solarReadTopic + "/" + logicalDeviceId, solarEventTopic + "/" + logicalDeviceId, openFmbXmlMarshaller, dataSource, intervalMs, deviceId, scale, offset, jitterChance, jitterPercent);
    }

    private static void loop(
            final MqttObserver mqtt,
            final String solarReadTopic,
            final String solarEventTopic,
            final OpenFmbXmlMarshaller openFmbXmlMarshaller,
            final DailyInterpolatedData dataSource,
            final long intervalMs,
            final DeviceId deviceId,
            final double scale,
            final double offset,
            final double jitterChance,
            final double jitterPercent) throws InterruptedException {

        final Random random = new Random(System.currentTimeMillis());

        while (true) {
            final long now = System.currentTimeMillis();

            try {
                final double inputValue = dataSource.atTime(now);
                final double scaledValue = inputValue * scale + offset;

                final double jitteredValue;
                if (random.nextDouble() <= jitterChance) {
                    final double jitterRange = jitterPercent * scaledValue;
                    jitteredValue = scaledValue + ((jitterRange * random.nextDouble()) - (jitterRange / 2));
                } else {
                    jitteredValue = scaledValue;
                }

                final SolarReadingProfile read = SolarModel.buildSolarRead(deviceId, jitteredValue);
                final byte[] readBytes = openFmbXmlMarshaller.marshal(read);
                mqtt.publish(readBytes, solarReadTopic);

                final SolarEventProfile event = SolarModel.buildSolarEvent(deviceId);
                final byte[] eventBytes = openFmbXmlMarshaller.marshal(event);
                mqtt.publish(eventBytes, solarEventTopic);

            } catch (Exception ex) {
                logger.error("Error publishing data: " + ex);
            }

            Thread.sleep(intervalMs);
        }

    }
}
