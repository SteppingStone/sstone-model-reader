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
import java.util.Vector;

import org.edc.sstone.record.reader.RecordInputStream;

/**
 * @author Greg Orlowski
 */
public class ScreenRecord extends TitledComponentRecord {

    public static final short CLASS_UID = 3;

    public static final byte SUBTYPE_MENU_SCREEN = 1;
    public static final byte SUBTYPE_CONTENT_SCREEN = 2;
    public static final byte SUBTYPE_ANIMATED_SCREEN = 3;

    /**
     * Use this subtype to designate a screen with an audio player interface
     */
    public static final byte SUBTYPE_AUDIO_SCREEN = 4;

    /**
     * Question screens will need special behavior that suppresses the next-screen event until the
     * all questions are answered.
     */
    public static final byte SUBTYPE_QUESTION_SCREEN = 5;

    /**
     * This screen should present the user with configurable preferences.
     */
    public static final byte SUBTYPE_USER_PREFERENCES_SCREEN = 6;

    protected static final byte AUTO_ADVANCE = (byte) (1 << 7);

    /**
     * The number of possible delay intervals within a second. So the value of 4 means that the
     * delay can be set in increments of 250ms. We want to fit this within 6 bits, which means that
     * we will allow a maximum delay of (64 / 4) = 16 seconds.
     */
    public static final byte ADVANCE_DELAY_SECOND_INTERVAL = 4;

    public byte navControlByte = 0;

    /**
     * audio screens use this for an audio resource.
     */
    public String resourcePath;
    public Vector componentRecords = new Vector();
    public transient short currentComponentIdx = 0;
    public transient boolean shouldReadComponentRecords = true;

    public short getClassUID() {
        return CLASS_UID;
    }

    public void read(RecordInputStream in) throws IOException {
        super.read(in);
        navControlByte = in.readByte();
        resourcePath = nullIfEmpty(in.readUTF());
        readComponentRecords(in);
    }

    public void readComponentRecords(RecordInputStream in) throws IOException {
        if (shouldReadComponentRecords) {
            for (short i = 0; in.hasMoreRecordBytes(); i++) {
                Record rec = in.readRecord();
                if (rec instanceof MenuItemRecord) {
                    ((MenuItemRecord) rec).idx = i;
                    in.seek(rec.getHeader().getRecordDataStartPos()
                            + rec.getHeader().getRecordDataLen());
                }
                componentRecords.addElement(rec);
            }
        }
    }

    public boolean isAutoAdvance() {
        return (navControlByte & AUTO_ADVANCE) == AUTO_ADVANCE;
    }

    public void setAutoAdvance(boolean val) {
        navControlByte = (byte) ((navControlByte & (~AUTO_ADVANCE)) | (val ? AUTO_ADVANCE : 0));
    }

    /**
     * @return the number of intervals (I will set an interval duration to 250ms) to delay before
     *         auto-advancing
     */
    public int getAutoAdvanceDelayIntervals() {
        return (navControlByte & (~AUTO_ADVANCE));
    }

    public void setAutoAdvanceDelayIntervals(int count) {
        byte val = (byte) count;
        navControlByte = (byte) ((navControlByte & AUTO_ADVANCE) | val);
    }

    public long getAutoAdvanceDelayMs() {
        return (getAutoAdvanceDelayIntervals() / ADVANCE_DELAY_SECOND_INTERVAL) * 1000l;
    }
}
