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
package org.edc.sstone.record.reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.edc.sstone.record.reader.RecordInputStream;

import junit.framework.TestCase;

/**
 * @author Greg Orlowski
 */
public class RecordInputStreamTest extends TestCase {

    static char[] chars = new char[] { 'X', 'ɲ', ' ' };

    static byte[] getStreamBytes() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(12);
        dos.writeInt(60);
        dos.writeLong(0l);
        dos.writeFloat(3.14f);
        dos.writeDouble(3.14);

        for (int i = 0; i < chars.length; i++)
            dos.writeChar(chars[i]);

        return bos.toByteArray();
    }

    public void testRecord() throws Exception {
        byte[] arr = getStreamBytes();
        RecordInputStream ois = new RecordInputStream(new ByteArrayInputStream(arr), null);

        int bytesRead = 0;

        short sh = ois.readShort();
        bytesRead += 2;

        assertEquals(sh, 12);
        assertEquals(bytesRead, ois.getPos());

        int i = ois.readInt();
        bytesRead += 4;

        assertEquals(i, 60);
        assertEquals(bytesRead, ois.getPos());

        long l = ois.readLong();
        bytesRead += 8;
        assertEquals(l, 0l);
        assertEquals(bytesRead, ois.getPos());

        float f = ois.readFloat();
        bytesRead += 4;
        assertEquals(314, (int) (f * 100));
        assertEquals(bytesRead, ois.getPos());

        double d = ois.readDouble();
        bytesRead += 8;
        assertEquals(314, (int) (d * 100));
        assertEquals(bytesRead, ois.getPos());

        // chars
        for (i = 0; i < chars.length; i++) {
            char ch = ois.readChar();
            assertEquals(ch, chars[i]);
            bytesRead += 2;
            assertEquals(bytesRead, ois.getPos());
        }

        assertEquals(arr.length, ois.getPos());
    }

    /**
     * Test various UTF8 reading scenarious
     */
    public void testReadUTF() throws Exception {
        String s = ""; // DataOutputStream throws NPE if we write null
        ByteArrayOutputStream bos;
        DataOutputStream dos;

        byte[] b = null;

        // Now verify our APIs
        String[] testStrings = {
                "", "Greg", "   ", "abc", "ɲ ɛ ɔ ŋ ñ é á ó ú í"
        };

        for (int i = 0; i < testStrings.length; i++) {
            bos = new ByteArrayOutputStream();
            dos = new DataOutputStream(bos);
            dos.writeUTF(testStrings[i]);
            b = bos.toByteArray();
            RecordInputStream ois = new RecordInputStream(new ByteArrayInputStream(b), null);

            s = ois.readUTF();
            assertEquals(b.length, ois.getPos());
            assertEquals(s, testStrings[i]);
        }
    }

    public void testSeek() throws Exception {
        byte[] arr = getStreamBytes();
        RecordInputStream ois = new RecordInputStream(new ByteArrayInputStream(arr), null);

        long desiredPos = 8;

        // Test seek when starting from zero
        ois.seek(desiredPos);
        assertEquals(desiredPos, ois.getPos());

        // Test seek when not starting from zero
        ois = new RecordInputStream(new ByteArrayInputStream(arr), null);
        ois.skipBytes(4);
        ois.seek(desiredPos);
        assertEquals(desiredPos, ois.getPos());
    }

}
