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
public class TextAreaComponentRecord extends ComponentRecord {

    public static final short CLASS_UID = 4;

    public static final byte SUBTYPE_TEXT_AREA = 1;
    public static final byte SUBTYPE_LETTER_READER = 2;
    public static final byte SUBTYPE_WORD_READER = 3;
    public static final byte SUBTYPE_SYLLABLE_READER = 4;

    public static final byte SUPPRESS_AUDIO = (byte) (1 << 7);
    public static final byte READ_NON_LETTERS = (byte) (1 << 6);

    public byte readControlByte;

    /**
     * This is fine. In the DAT, we force the user to use one of a predefined set of separator chars
     * (-, |, :, ^, _, . and / is a good set)
     */
    public byte syllableSeparator = '-';

    public String text = null;

    public short getClassUID() {
        return CLASS_UID;
    }

    public void read(RecordInputStream in) throws IOException {
        super.read(in);
        readControlByte = in.readByte();

        byte ch = in.readByte();
        syllableSeparator = ch;
        text = nullIfEmpty(in.readUTF());
    }

    public boolean isReadNonLetters() {
        return (readControlByte & READ_NON_LETTERS) == READ_NON_LETTERS;
    }

    public boolean isSuppressAudio() {
        return (readControlByte & SUPPRESS_AUDIO) == SUPPRESS_AUDIO;
    }
}
