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

import org.edc.sstone.Constants;
import org.edc.sstone.record.reader.RecordInputStream;
import org.edc.sstone.ui.model.FixedSpacing;
import org.edc.sstone.ui.model.FontStyle;
import org.edc.sstone.ui.model.Spacing;
import org.edc.sstone.util.StdLib;

/**
 * TODO: I do not want this to depend on j2me/lcdui classes, but IFont currently depends on
 * {@link javax.microedition.lcdui.Graphics}. In order to not depend on it, we'd have to create an
 * adapter layer on top of {@link javax.microedition.lcdui.Graphics} or provide some factory to look
 * up fonts by size, but the latter solution would make the APIs convoluted. It's nice to just get
 * an IFont rather than having to get the SMALL, MED, LARGE size and look it up somewhere... (?)
 * 
 * Maybe {Registry}
 * 
 * @author Greg Orlowski
 */
public class StyleRecord extends AbstractRecord {

    public static final short CLASS_UID = 2;

    /**
     * This is used this only with {@link StyleRecord} objects that are not read from a
     * {@link RecordInputStream}. If we read the record from a stream, we'll use the header length
     * to tell us whether or not the object is null. If we instantiate a transient instance from the
     * ctor that is not read from a stream, use this field to determine whether the style has any
     * fields set.
     */
    private transient Boolean hasAtLeastOneFieldSet = Boolean.FALSE;

    /*
     * These are all public b/c accessors add to the class size. This is a simple value object class
     * that only exists to support deserialzation. It's unlikely that we'd write code to modify the
     * unencapsulated fields.
     */
    public int backgroundColor = Constants.NUMBER_NOT_SET;
    public int fontColor = Constants.NUMBER_NOT_SET;
    public int highlightColor = Constants.NUMBER_NOT_SET;

    public FontStyle fontStyle = null;

    // public byte fontFace = Constants.NUMBER_NOT_SET;
    // public byte fontStyle = Constants.NUMBER_NOT_SET;
    // public byte fontSize = Constants.NUMBER_NOT_SET;

    public byte lineHeight = Constants.NUMBER_NOT_SET;
    public Spacing margin = null;
    public short padding = Constants.NUMBER_NOT_SET;

    public byte textAnchor = Constants.NUMBER_NOT_SET;
    public byte componentAnchor = Constants.NUMBER_NOT_SET;

    /**
     * The amount of time to pause, persisted as a short of deciseconds, before an animation starts
     */
    public short animationStartDelay = Constants.NUMBER_NOT_SET;

    /**
     * The amount of time to pause, persisted as a short of deciseconds, between each frame of an
     * animation sequence.
     */
    public short animationPeriod = Constants.NUMBER_NOT_SET;

    public void read(RecordInputStream in) throws IOException {

        hasAtLeastOneFieldSet = null;

        if (!isNull()) {
            /*
             * NOTE: we only read the super fields (subtype + control block) if the record data
             * block length is > 0
             */
            super.read(in);
            backgroundColor = in.readInt();
            fontColor = in.readInt();
            highlightColor = in.readInt();

            byte fontFace = in.readByte();
            byte fontStyleBt = in.readByte();
            byte fontSize = in.readByte();

            if (fontFace != Constants.NUMBER_NOT_SET
                    || fontStyleBt != Constants.NUMBER_NOT_SET
                    || fontSize != Constants.NUMBER_NOT_SET) {
                fontStyle = new FontStyle(fontFace, fontStyleBt, fontSize);
            }

            lineHeight = in.readByte();

            short[] margins = new short[4];
            for (int i = 0; i < margins.length; i++)
                margins[i] = in.readShort();

            // TODO: note that when we serialize, either we set all to unset or we need to populate
            // all
            // values
            boolean marginSet = margins[0] != -1;
            margin = marginSet ? new FixedSpacing(margins[0], margins[1], margins[2], margins[3]) : null;
            padding = in.readShort();

            textAnchor = in.readByte();
            componentAnchor = in.readByte();

            animationStartDelay = in.readShort();
            animationPeriod = in.readShort();
        }
    }

    public short getClassUID() {
        return CLASS_UID;
    }

    /**
     * @param parentStyle
     *            a {@link StyleRecord} from which to inherit style defaults.
     */
    public void setDefaults(StyleRecord parentStyle) {
        if (parentStyle != null && !parentStyle.isNull()) {

            // If we know that we are setting defaults to a parent
            // that is not null then we know that at least one field
            // will get set
            hasAtLeastOneFieldSet = Boolean.TRUE;

            if (!StdLib.isSet(backgroundColor))
                backgroundColor = parentStyle.backgroundColor;

            if (!StdLib.isSet(fontColor))
                fontColor = parentStyle.fontColor;

            if (!StdLib.isSet(highlightColor))
                highlightColor = parentStyle.highlightColor;

            this.fontStyle = this.fontStyle == null
                    ? parentStyle.fontStyle
                    : this.fontStyle.withDefaults(parentStyle.fontStyle);

            if (!StdLib.isSet(lineHeight))
                lineHeight = parentStyle.lineHeight;

            if (margin == null) {
                margin = parentStyle.margin;
            }

            if (!StdLib.isSet(padding))
                padding = parentStyle.padding;

            if (!StdLib.isSet(textAnchor))
                textAnchor = parentStyle.textAnchor;

            if (!StdLib.isSet(componentAnchor))
                componentAnchor = parentStyle.componentAnchor;

            if (!StdLib.isSet(animationStartDelay))
                animationStartDelay = parentStyle.animationStartDelay;

            if (!StdLib.isSet(animationPeriod))
                animationPeriod = parentStyle.animationPeriod;
        }
    }

    public boolean isNull() {
        if (hasAtLeastOneFieldSet != null)
            return !(hasAtLeastOneFieldSet.booleanValue());
        return super.isNull();
    }

}
