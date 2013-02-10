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
public abstract class ComponentRecord extends AbstractRecord {

    public StyleRecord styleRecord;

    /**
     * Several component types can categorized with a "sub-type". Since we're targeting J2ME and
     * want to minimize the class count, we implement this with a simple byte classifier where
     * possible rather than with polymorphism.
     */

    public void read(RecordInputStream in) throws IOException {
        super.read(in);
        Record _styleRecord = in.readRecord();
        this.styleRecord = _styleRecord.isNull() ? null : (StyleRecord) _styleRecord;
    }

    /**
     * Seek past the record contents to the end of the record. Use this if we only need a
     * "sparse record" that does not reconstitute all nested components.
     * 
     * @param in
     *            the record input stream
     * @throws IOException
     */
    // protected void seekToEndOfRecord(RecordInputStream in) throws IOException {
    // long dataStartPos = getRecordHeader().getRecordDataStartPos();
    // in.skip(getRecordHeader().getRecordDataLen() - (in.getPos() - dataStartPos));
    // }

}
