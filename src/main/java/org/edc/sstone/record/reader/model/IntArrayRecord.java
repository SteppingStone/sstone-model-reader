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
package org.edc.sstone.record.reader.model;

import java.io.IOException;

import org.edc.sstone.record.reader.RecordInputStream;

/**
 * @author Greg Orlowski
 */
public class IntArrayRecord extends AbstractRecord {

    public static final int CLASS_UID = 100;

    public int[] intValues;

    public void read(RecordInputStream in) throws IOException {
        super.read(in);

        // length to the end of the record / sizeof(int)
        int len = (getHeader().getRecordDataLen()
                - (int) (in.getPos() - getHeader().getRecordDataStartPos())) / 4;

        // length to the end of the record / sizeof(int)
        // int len = getHeader().getRecordDataLen() / 4;

        intValues = new int[len];
        for (int i = 0; i < len; i++) {
            intValues[i] = in.readInt();
        }
    }

    // b/c we do not read the subtype byte, consider 0 to be null
    public boolean isNull() {
        return getHeader().getRecordDataLen() == 0;
    }

    public short getClassUID() {
        return CLASS_UID;
    }

    public int length() {
        return intValues.length;
    }

    public int getInt(int i) {
        return intValues[i];
    }

}
