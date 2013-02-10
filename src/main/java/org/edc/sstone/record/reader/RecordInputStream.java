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
 *
 * ---------------------------------------------------------------------------
 * NOTE: the readUTF, readUnsignedShort, convertFromUTF and
 * convertToUnsignedShort methods are modified from methods with corresponding
 * names in GNU classpath's DataInputStream class. The original license for
 * that class appears below:
 * ---------------------------------------------------------------------------
 *
 * DataInputStream.java -- FilteredInputStream that implements DataInput
 * Copyright (C) 1998, 1999, 2000, 2001, 2003, 2005, 2008
 * Free Software Foundation
 *
 * This file is part of GNU Classpath.
 *
 * GNU Classpath is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * GNU Classpath is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GNU Classpath; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version. 
 */

package org.edc.sstone.record.reader;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.util.Stack;

import org.edc.sstone.record.reader.model.AbstractRecord;
import org.edc.sstone.record.reader.model.Record;
import org.edc.sstone.record.reader.model.RecordHeader;

/**
 * An {@link InputStream} from which we can read {@link AbstractRecord} objects, Strings, and java
 * primitives.
 * 
 * NOTE: I do not implement DataInput b/c it requires the deprecated readLine() method, which we do
 * not need. The readLine() method is not present in the Sun WTK DataInput interface class.
 * 
 * @author Greg Orlowski
 */
public class RecordInputStream extends InputStream {

    private final RecordFactory recordFactory;
    protected DataInputStream in;
    private byte[] buf = new byte[8];
    protected long pos = 0;

    protected Stack marks = new Stack();

    public RecordInputStream(InputStream in, RecordFactory recordFactory) {
        this.in = wrap(in);
        this.recordFactory = recordFactory;
    }

    public Record readRecord() throws IOException {
        if (isRecordMarkSet()) {
            Mark mark = getMark();
            if (pos >= mark.recordMarkPos + mark.recordLen)
                throw new EOFException();
        }

        long recordStartOffset = pos;

        short classUID = readShort();
        int recordDataLen = readInt();

        Record record = recordFactory.newInstance(classUID);
        record.setRecordHeader(new RecordHeader(recordStartOffset, recordDataLen));

        markRecord(recordDataLen);
        record.read(this);

        // Yes, this is kind-of ugly. We only care about readEndPos when we are reading a segment
        // of the stream, which happens when we are dealing with descendents of AbstractRecord not
        // a RecordWriter. I could subclass RecordInputStream instead, but this is quick and
        // convenient
        if (record instanceof AbstractRecord) {
            ((AbstractRecord) record).readEndPos = getPos();
        }

        reset();

        return record;
    }

    /**
     * Seek to absolute position pos in the stream
     * 
     * @param pos
     *            the desired offset position, starting from the absolute start of the stream, to
     *            which we want to seek.
     * @return the position after the operation, which may not be the requested position if the
     *         stream was unable to seek to that position.
     * @throws IOException
     */
    public long seek(long pos) throws IOException {
        if (pos > this.pos) {
            skip(pos - this.pos);
        }
        return this.pos;
    }

    public long skip(long n) throws IOException {
        long numSkipped = in.skip(n);
        pos += numSkipped;
        return numSkipped;
    }

    public long getPos() {
        return pos;
    }

    /**
     * Does nothing
     */
    public void mark(int readlimit) {
        // DO NOTHING
    }

    void markRecord(int recordLen) {
        marks.push(new Mark(pos, recordLen));
    }

    /**
     * Marking, per the contract specified by {@link InputStream}, is not supported because this
     * stream is not buffered. We support marking record boundaries, but that is just so records
     * know how much more they can read ahead until the end of the record.
     */
    public boolean markSupported() {
        return false;
    }

    public void reset() throws IOException {
        // ??? why did I previously reset pos?
        // pos = getMark().recordMarkPos;
        marks.pop();
    }

    public boolean isClosed() {
        return pos == -1;
    }

    public void close() throws IOException {
        // J2ME's Stack does not have clear()
        // marks.clear();
        while (marks.size() > 0)
            marks.pop();
        pos = -1;
        in.close();
        in = null;
    }

    public int available() throws IOException {
        return in.available();
    }

    public boolean hasMoreRecordBytes() {
        if (isRecordMarkSet()) {
            Mark mark = getMark();
            return (mark.recordLen - (int) (pos - mark.recordMarkPos)) > 0;
        }
        return true;
    }

    protected Mark getMark() {
        return (Mark) marks.peek();
    }

    private boolean isRecordMarkSet() {
        return marks.size() > 0;
    }

    /*
     * InputStream
     */
    public int read() throws IOException {
        int ret = in.read();
        pos++;
        return ret;
    }

    protected static DataInputStream wrap(InputStream in) {
        // return new DataInputStream(in.markSupported() ? in : new BufferedInputStream(in));
        return new DataInputStream(in);
    }

    /*
     * implement DataInput
     */
    public boolean readBoolean() throws IOException {
        boolean ret = in.readBoolean();
        pos++;
        return ret;
    }

    public byte readByte() throws IOException {
        byte ret = in.readByte();
        pos++;
        return ret;
    }

    public char readChar() throws IOException {
        char ret = in.readChar();
        pos += 2;
        return ret;
    }

    public double readDouble() throws IOException {
        double ret = in.readDouble();
        pos += 8;
        return ret;
    }

    public float readFloat() throws IOException {
        float ret = in.readFloat();
        pos += 4;
        return ret;
    }

    public void readFully(byte[] buf) throws IOException {
        readFully(buf, 0, buf.length);
    }

    public void readFully(byte[] buf, int offset, int len) throws IOException {
        in.readFully(buf, offset, len);
        pos += len;
    }

    public int readInt() throws IOException {
        int ret = in.readInt();
        pos += 4;
        return ret;
    }

    public long readLong() throws IOException {
        long ret = in.readLong();
        pos += 8;
        return ret;
    }

    public short readShort() throws IOException {
        short ret = in.readShort();
        pos += 2;
        return ret;
    }

    public int readUnsignedByte() throws IOException {
        return readByte() & 0xFF;
    }

    public int readUnsignedShort() throws IOException {
        readFully(buf, 0, 2);
        return convertToUnsignedShort(buf);
    }

    public int skipBytes(int num) throws IOException {
        int numSkipped = in.skipBytes(num);
        pos += numSkipped;
        return numSkipped;
    }

    /*
     * All methods below this point are derived from java.io.DataInputStream in GNU Classpath
     */
    public String readUTF() throws IOException {
        // I do not need to update pos b/c readFully and readUnsignedShort update it
        final int len = readUnsignedShort();
        byte[] buff = new byte[len];
        readFully(buff);
        return convertFromUTF(buff);
    }

    static String convertFromUTF(byte[] buf) throws EOFException, UTFDataFormatException {
        // Give StringBuffer an initial estimated size to avoid
        // enlarge buffer frequently
        StringBuffer sb = new StringBuffer((buf.length / 2) + 2);

        for (int i = 0; i < buf.length;) {
            if ((buf[i] & 0x80) == 0) // bit pattern 0xxxxxxx
                sb.append((char) (buf[i++] & 0xFF));
            else if ((buf[i] & 0xE0) == 0xC0) // bit pattern 110xxxxx
            {
                if (i + 1 >= buf.length
                        || (buf[i + 1] & 0xC0) != 0x80)
                    throw new UTFDataFormatException();

                sb.append((char) (((buf[i++] & 0x1F) << 6)
                        | (buf[i++] & 0x3F)));
            }
            else if ((buf[i] & 0xF0) == 0xE0) // bit pattern 1110xxxx
            {
                if (i + 2 >= buf.length
                        || (buf[i + 1] & 0xC0) != 0x80
                        || (buf[i + 2] & 0xC0) != 0x80)
                    throw new UTFDataFormatException();

                sb.append((char) (((buf[i++] & 0x0F) << 12)
                        | ((buf[i++] & 0x3F) << 6)
                        | (buf[i++] & 0x3F)));
            }
            else
                // must be ((buf [i] & 0xF0) == 0xF0 || (buf [i] & 0xC0) == 0x80)
                throw new UTFDataFormatException(); // bit patterns 1111xxxx or
                                                    // 10xxxxxx
        }
        return sb.toString();
    }

    static int convertToUnsignedShort(byte[] buf) {
        return (((buf[0] & 0xff) << 8) | (buf[1] & 0xff));
    }

    protected static class Mark {
        protected final long recordMarkPos;
        protected final int recordLen;

        public Mark(long recordMarkPos, int recordLen) {
            this.recordMarkPos = recordMarkPos;
            this.recordLen = recordLen;
        }
    }

}
