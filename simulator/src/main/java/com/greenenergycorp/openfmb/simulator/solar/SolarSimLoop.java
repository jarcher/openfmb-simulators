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

import com.greenenergycorp.openfmb.simulator.DailyInterpolatedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SolarSimLoop {
    private final static Logger logger = LoggerFactory.getLogger(SolarSimulator.class);

    public static void loop(
            final SolarPublisher publisher,
            final DailyInterpolatedData dataSource,
            final long intervalMs,
            final double scale,
            final double offset,
            final double jitterChance,
            final double jitterPercent) throws InterruptedException {

        final Random random = new Random(System.currentTimeMillis());

        while (true) {
            final long now = System.currentTimeMillis();

            try {
                final double inputValue = dataSource.atTime(now);
                final double scaledValue = -1 * inputValue * scale + offset;

                final double jitteredValue;
                if (random.nextDouble() <= jitterChance) {
                    final double jitterRange = jitterPercent * scaledValue;
                    jitteredValue = scaledValue + ((jitterRange * random.nextDouble()) - (jitterRange / 2));
                } else {
                    jitteredValue = scaledValue;
                }

                publisher.solarReadUpdate(jitteredValue);

                publisher.solarEventUpdate();

            } catch (Exception ex) {
                logger.error("Error publishing data: " + ex);
            }

            Thread.sleep(intervalMs);
        }

    }
}
