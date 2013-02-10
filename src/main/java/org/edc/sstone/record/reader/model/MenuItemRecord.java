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
public class MenuItemRecord extends TitledComponentRecord {

    public static final short CLASS_UID = 1;

    /**
     * This bit mask will be set when the menu item is the root node of a module.
     */
    public static final byte MODULE_HEADER = 1;

    /**
     * This bit mask will be set when the menu item points to a {@link ScreenRecord}.
     */
    public static final byte SCREEN_POINTER = 2;

    /**
     * This bit mask will be set when the menu item points to a {@link ScreenSeriesRecord}.
     */
    public static final byte SCREEN_SERIES_POINTER = 4;

    // public static final byte AUDIO_SERIES_POINTER = 100;
    // public static final byte SLIDE_SERIES_POINTER = 101;

    public String iconImagePath = null;

    /**
     * We need branchStyleRecord if we want a default style that applies to all screens below the
     * level of the menuItem so we do not have to specify styles for every individual screen.
     * 
     * When the MenuItemRecord is the root node ({@link MenuItemRecord#MODULE_HEADER} is set), the
     * {@link StyleRecord} will be the default for the entire module.
     */
    public StyleRecord branchStyleRecord = null;
    public String branchTitle = null;

    /**
     * This represents the index of this MenuItem in its enclosing menu {@link ScreenRecord}
     */
    public transient short idx;

    /**
     * The root URL of the module. We only need this when we are using the MenuItemRecord on a
     * module selection screen.
     */
    public transient String rootUrl;

    public short getClassUID() {
        return CLASS_UID;
    }

    public void read(RecordInputStream in) throws IOException {
        super.read(in);
        this.iconImagePath = nullIfEmpty(in.readUTF());
        StyleRecord styleRecord = (StyleRecord) in.readRecord();
        if (!styleRecord.isNull()) {
            this.branchStyleRecord = styleRecord;
        }
        this.branchTitle = in.readUTF();

        // Do not seek here... we want to seek only when the next record is a screenrecord, but,
        // in that case, seek in the screen record. For a screen series record, we read a screen
        // record, and the screen record's seek should position the stream offset at the next
        // screen record.
        //
        // in.seek(getRecordHeader().getRecordDataStartPos() +
        // getRecordHeader().getRecordDataLen());
    }
}
