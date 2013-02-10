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
// extends TitledComponentRecord 
/**
 * @author Greg Orlowski
 */
public class ScreenSeriesRecord extends AbstractRecord {

    public static final short CLASS_UID = 6;

    /**
     * each element in the {@link IntArrayRecord} represents the total length in bytes of the
     * corresponding element in the enclosed list of {@link ScreenRecord} records.
     */
    public IntArrayRecord screenRecordLengths;

    /*
     * NOTE: this will ONLY be used during writing. During reading, we do not want to reference all
     * screens in memory at the same time.
     */
    // public Vector screens;

    public short getClassUID() {
        return CLASS_UID;
    }

    public void read(RecordInputStream in) throws IOException {
        super.read(in);
        screenRecordLengths = (IntArrayRecord) in.readRecord();
    }

    public long getFirstScreenRecordPos() {
        return readEndPos;
    }

    public int[] getScreenRecordOffsets() {
        int[] ret = new int[screenRecordLengths.length()];
        ret[0] = 0;
        for (int i = 1; i < ret.length; i++) {
            ret[i] = ret[i - 1] + screenRecordLengths.getInt(i - 1);
        }
        return ret;
    }

}
