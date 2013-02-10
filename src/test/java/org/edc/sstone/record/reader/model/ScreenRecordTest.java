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

import junit.framework.TestCase;

/**
 * @author Greg Orlowski
 */
public class ScreenRecordTest extends TestCase {

    public void testAutoAdvance() {
        ScreenRecord r = new ScreenRecord();
        assertEquals(0, r.getAutoAdvanceDelayIntervals());
        assertEquals(false, r.isAutoAdvance());

        r.setAutoAdvanceDelayIntervals(32);

        assertEquals(false, r.isAutoAdvance());
        r.setAutoAdvance(true);

        assertEquals(32, r.getAutoAdvanceDelayIntervals());
        assertEquals(true, r.isAutoAdvance());
        
        assertEquals(8000l, r.getAutoAdvanceDelayMs());
    }

}
