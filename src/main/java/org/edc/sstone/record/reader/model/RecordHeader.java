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

import org.edc.sstone.record.reader.RecordInputStream;

/**
 * @author Greg Orlowski
 */
public final class RecordHeader {

    private transient final long recordHeaderStartPos;
    private final int recordDataLen;

    public RecordHeader(long recordHeaderStartPos, int recordDataLen) {
        this.recordHeaderStartPos = recordHeaderStartPos;
        this.recordDataLen = recordDataLen;
    }

    public static int getHeaderSize() {
        // first 2 bytes (short) is type. Next 2 bytes is recordDataLen
        // type: sizeof(short) + length: sizeof(int)
        return 2 + 4;
    }

    /**
     * @return the offset of the {@link RecordInputStream} where the record data starts (after the
     *         header metadata)
     */
    // public long getRecordHeaderStartPos() {
    // return recordHeaderStartPos;
    // }

    /**
     * @return the offset of the {@link RecordInputStream} where the record data starts (after the
     *         header metadata)
     */
    public long getRecordDataStartPos() {
        return recordHeaderStartPos + getHeaderSize();
    }

    /**
     * @return the length of the data portion of the record (minus the header size)
     */
    public int getRecordDataLen() {
        return recordDataLen;
    }

    /**
     * @return the length of the data portion of the record + the header size
     */
     public int getRecordTotalLen() {
     return recordDataLen + getHeaderSize();
     }

}
