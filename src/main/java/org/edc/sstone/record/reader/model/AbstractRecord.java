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
public abstract class AbstractRecord implements Record {

    private RecordHeader recordHeader;

    /**
     * We can pack additional record classification information into this byte, which gets
     * initialized to zero.
     */
    public byte subType = 0;

    /**
     * The controlData is intended to give us 4 additional bytes to use any way we want to in any
     * given record type. This is a hedge. It allows us to add simple data fields (bit masks and
     * numeric fields) down the road with the possibility that we will not need to change the file
     * format.
     */
    public int controlData = 0;

    public transient long readEndPos;

    /*
     * (non-Javadoc)
     * 
     * @see org.edc.sstone.record.reader.model.Record#getHeader()
     */
    public RecordHeader getHeader() {
        return recordHeader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.edc.sstone.record.reader.model.Record#read(org.edc.sstone.record.reader.RecordInputStream
     * )
     */
    public void read(RecordInputStream in) throws IOException {
        // if (!isNull()) {
        this.subType = in.readByte();
        this.controlData = in.readInt();
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.edc.sstone.record.reader.model.Record#setRecordHeader(org.edc.sstone.record.reader.model
     * .RecordHeader)
     */
    public void setRecordHeader(RecordHeader recordHeader) {
        this.recordHeader = recordHeader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.edc.sstone.record.reader.model.Record#isNull()
     */
    public boolean isNull() {
        // if (subType == Constants.NUMBER_NOT_SET)
        // return true;
        // RecordHeader rh = getRecordHeader();
        // if (rh != null && rh.getRecordDataLen() == 0)
        // return true;
        // return false;

        // OK, we're going to use == 1 b/c we will ALWAYS want to
        // read subType (I think). Override if that will be different
        return getHeader().getRecordDataLen() == 0;
    }

    // public void setNull() {
    // subType = Constants.NUMBER_NOT_SET;
    // }

    /*
     * TODO: I should just set nullIfEmpty as the default readUTF behavior in recordInputStream
     */
    public static String nullIfEmpty(String str) {
        return (str == null || str.length() == 0) ? null : str;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.edc.sstone.record.reader.model.Record#getClassUID()
     */
    public abstract short getClassUID();

}
