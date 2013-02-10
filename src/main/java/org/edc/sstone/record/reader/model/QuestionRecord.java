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
public class QuestionRecord extends ComponentRecord {

    public static final short CLASS_UID = 7;

    public byte answerInfo;
    public String question;
    public String[] answers;

    public short getClassUID() {
        return CLASS_UID;
    }

    public int getAnswerCount() {
        return answerInfo & 0x0F;
    }

    public int getCorrectAnswerIndex() {
        return answerInfo >>> 4;
    }

    public void read(RecordInputStream in) throws IOException {
        super.read(in);
        answerInfo = in.readByte();
        String s = nullIfEmpty(in.readUTF());
        question = s == null ? "[null]" : s;

        int answerCount = getAnswerCount();
        answers = new String[answerCount];
        for (int i = 0; i < answerCount; i++) {
            s = nullIfEmpty(in.readUTF());
            answers[i] = s == null ? "[null]" : s;
        }
    }

}
