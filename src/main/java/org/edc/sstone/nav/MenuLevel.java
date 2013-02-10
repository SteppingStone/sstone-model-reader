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
package org.edc.sstone.nav;

import org.edc.sstone.record.reader.model.ScreenRecord;
import org.edc.sstone.record.reader.model.ScreenSeriesRecord;
import org.edc.sstone.record.reader.model.StyleRecord;

/**
 * @author Greg Orlowski
 */
public class MenuLevel {

    /*
     * Do we use up too much heap by keeping ScreenRecord instances in memory?
     */
    ScreenRecord currScreenRecord;

    StyleRecord branchStyle;
    String branchTitle;

    private int currScreenIdx = -1;
    private int prevScreenIdx = -1;

    private long firstScreenRecordPos = -1l;

    int[] screenRecordOffsets;

    protected static MenuLevel newMenuScreenLevel(ScreenRecord menuScreenRecord) {
        MenuLevel ret = new MenuLevel();
        ret.currScreenRecord = menuScreenRecord;
        return ret;
    }

    protected static MenuLevel newScreenSeriesLevel(ScreenSeriesRecord rec) {
        MenuLevel ret = new MenuLevel();
        //
        ret.firstScreenRecordPos = rec.getFirstScreenRecordPos();
        ret.screenRecordOffsets = rec.getScreenRecordOffsets();

        ret.currScreenIdx = 0;
        ret.prevScreenIdx = -1;

        return ret;
    }

    protected long getCurrentScreenPos() {
        return getScreenRecordPos(currScreenIdx);
    }

    // At the last screen in a series, "advance" should up one level
    protected boolean hasNext() {
        return currScreenIdx < screenRecordOffsets.length - 1;
    }

    protected boolean hasPrevious() {
        return prevScreenIdx != -1;
    }

    protected void setToNextScreen() {
        prevScreenIdx = currScreenIdx++;
    }

    protected void setToPreviousScreen() {
        currScreenIdx = prevScreenIdx--;
    }

    private long getScreenRecordPos(int recordNum) {
        return firstScreenRecordPos + screenRecordOffsets[recordNum];
    }

    protected boolean isScreenSeries() {
        return screenRecordOffsets != null;
    }
}
