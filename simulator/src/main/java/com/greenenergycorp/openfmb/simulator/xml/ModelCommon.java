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

import com.greenenergycorp.openfmb.xml.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ModelCommon {

    public static XMLGregorianCalendar xmlTimeFor(long time) throws DatatypeConfigurationException {
        final GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

    public static Reading buildReading(double v, long time, UnitSymbolKind unit, UnitMultiplierKind unitMultiplier, FlowDirectionKind flow, PhaseCodeKind phase) throws DatatypeConfigurationException {

        final ReadingType readingType = new ReadingType();
        readingType.setUnit(unit);
        readingType.setMultiplier(unitMultiplier);
        readingType.setFlowDirection(flow);
        readingType.setPhases(phase);

        final DateTimeInterval dateTimeInterval = new DateTimeInterval();
        dateTimeInterval.setStart(xmlTimeFor(time));
        dateTimeInterval.setEnd(xmlTimeFor(time));

        final Reading reading = new Reading();
        reading.setReadingType(readingType);
        reading.setSource("");
        reading.setQualityFlag(new byte[] {0, 0});
        reading.setTimePeriod(dateTimeInterval);
        reading.setValue((float)v);

        return reading;
    }
}
