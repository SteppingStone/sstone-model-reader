/*
 * Copyright (c) 2012 EDC
 * 
 * This file is part of Stepping Stone.
 * 
 * Stepping Stone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Stepping Stone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Stepping Stone.  If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */
package org.edc.sstone.record.reader;

import org.apache.commons.lang.IntHashMap;
import org.edc.sstone.record.reader.model.Record;

/**
 * @author Greg Orlowski
 */
public class RecordFactory {

    private IntHashMap recordTypeMap = new IntHashMap();

    public void registerType(Record obj) {
        try {
            obj.getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Serializable cannot be instantiated. UID: " + obj.getClassUID());
        }
        recordTypeMap.put(obj.getClassUID(), obj.getClass());
    }

    protected Record newInstance(short classUID) {
        Object o = recordTypeMap.get(classUID);
        if (o == null) {
            throw new IllegalArgumentException("Unsupported Record class. UID: " + classUID);
        }
        try {
            return (Record) ((Class) o).newInstance();
        } catch (Exception e) {
            // this should be unreachable
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate class with UID: " + classUID);
        }
    }
}
