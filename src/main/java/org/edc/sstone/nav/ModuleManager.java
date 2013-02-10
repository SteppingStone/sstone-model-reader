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

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.edc.sstone.CheckedException;
import org.edc.sstone.Constants;
import org.edc.sstone.io.InputStreamProvider;
import org.edc.sstone.record.reader.RecordFactory;
import org.edc.sstone.record.reader.RecordInputStream;
import org.edc.sstone.record.reader.model.MenuItemRecord;
import org.edc.sstone.record.reader.model.Record;
import org.edc.sstone.record.reader.model.ScreenRecord;
import org.edc.sstone.record.reader.model.ScreenSeriesRecord;
import org.edc.sstone.record.reader.model.StyleRecord;
import org.edc.sstone.res.ResourceProvider;
import org.edc.sstone.text.Alphabet;

import de.enough.polish.util.Properties;

/**
 * @author Greg Orlowski
 */
public class ModuleManager implements ResourceProvider {

    private Stack menuLevelIndex = new Stack();
    private InputStreamProvider streamProvider;
    private RecordFactory recordFactory;

    private String audioFileType = "wav";
    private String imageFileType = "png";

    private Alphabet alphabet;

    RecordInputStream ris;

    public final String modulePath;

    public ModuleManager(RecordFactory recordFactory, String modulePath, InputStreamProvider streamProvider) {
        this(recordFactory, modulePath, streamProvider, null);
    }

    public ModuleManager(RecordFactory recordFactory, String modulePath, InputStreamProvider streamProvider,
            String audioType) {
        this.recordFactory = recordFactory;
        this.streamProvider = streamProvider;
        this.modulePath = modulePath.endsWith("/")
                ? modulePath.substring(0, modulePath.length() - 1)
                : modulePath;
        this.audioFileType = audioType;
    }

    protected RecordInputStream getStream() throws IOException, CheckedException {
        if (ris == null || ris.isClosed()) {
            String indexModPath = modulePath + "/" + Constants.MODULE_INDEX_FILENAME;
            InputStream in = streamProvider.getInputStream(indexModPath);
            if (in == null) {
                throw new CheckedException("Null InputStream when attempting to load index.mod from: "
                        + indexModPath, CheckedException.MODULE_READ_ERROR);
            }
            ris = new RecordInputStream(in, recordFactory);
        }
        return ris;
    }

    // TODO: make this better... maybe translate the exception?
    private void handleIOException(IOException e) throws CheckedException {
        closeStream();
        throw new CheckedException("IOException reading module from modulePath: [" + modulePath + "]", e,
                CheckedException.MODULE_READ_ERROR);
    }

    public ScreenRecord firstScreen() throws CheckedException {
        return descend(getModuleHeaderRecord(false));
    }

    protected MenuItemRecord getModuleHeaderRecord(boolean closeAfterRead) throws CheckedException {
        MenuItemRecord moduleHeaderRecord = null;
        try {
            moduleHeaderRecord = (MenuItemRecord) getStream().readRecord();
            if (closeAfterRead) {
                closeStream();
            }
        } catch (IOException e) {
            handleIOException(e);
        }
        moduleHeaderRecord.rootUrl = this.modulePath;
        return moduleHeaderRecord;
    }

    public MenuItemRecord getModuleHeaderRecord() throws CheckedException {
        return getModuleHeaderRecord(true);
    }

    private ScreenRecord readSeriesCurrentScreen() throws CheckedException, IOException {
        MenuLevel ml = peek();
        seek(ml.getCurrentScreenPos());
        ScreenRecord sr = (ScreenRecord) getStream().readRecord();
        ml.currScreenRecord = sr;
        closeStream();
        return mergeDefaults(ml.currScreenRecord);
    }

    private RecordInputStream seek(long pos) throws CheckedException, IOException {
        RecordInputStream ris = getStream();
        ris.seek(pos);
        return ris;
    }

    protected StyleRecord getBranchStyle() {
        if (menuLevelIndex.isEmpty())
            return null;
        StyleRecord ret = new StyleRecord();
        // ret.hasAtLeastOneFieldSet = Boolean.FALSE;

        for (int i = (menuLevelIndex.size() - 1); i >= 0; i--) {
            ret.setDefaults(((MenuLevel) menuLevelIndex.elementAt(i)).branchStyle);
        }
        return ret.isNull() ? null : ret;
    }

    protected String getBranchTitle() {
        String title = null;
        for (int i = (menuLevelIndex.size() - 1); i >= 0; i--) {
            title = ((MenuLevel) menuLevelIndex.elementAt(i)).branchTitle;
            if (title != null)
                break;
        }
        return title;
    }

    protected ScreenRecord mergeDefaults(ScreenRecord currentScreen) {
        if (currentScreen.styleRecord == null) {
            currentScreen.styleRecord = getBranchStyle();
        } else {
            currentScreen.styleRecord.setDefaults(getBranchStyle());
        }
        if (currentScreen.title == null) {
            currentScreen.title = getBranchTitle();
        }

        // if(currentScreen.componentRecords != null) {
        // for(int i=0; i<componentRecords )
        // }

        return currentScreen;
    }

    public ScreenRecord currentScreen() {
        if (!menuLevelIndex.isEmpty()) {
            return peek().currScreenRecord;
        }
        return null;
    }

    public ScreenRecord descend(MenuItemRecord menuItemRecord) throws CheckedException {
        ScreenRecord ret = null;
        if (!menuLevelIndex.isEmpty()) {
            peek().currScreenRecord.currentComponentIdx = menuItemRecord.idx;
        }
        try {
            // seekPos(pos);
            RecordInputStream ris = seek(menuItemRecord.readEndPos);
            Record record = ris.readRecord();
            if (record instanceof ScreenRecord) {
                ret = (ScreenRecord) record;
                menuLevelIndex.push(MenuLevel.newMenuScreenLevel(ret));
            } else if (record instanceof ScreenSeriesRecord) {
                // TODO: this will blow up if a series has 0 screens. We must not allow that.
                menuLevelIndex.push(MenuLevel.newScreenSeriesLevel((ScreenSeriesRecord) record));
                ret = readSeriesCurrentScreen();
            }
            closeStream();
        } catch (IOException e) {
            handleIOException(e);
        }
        peek().branchStyle = menuItemRecord.branchStyleRecord;
        peek().branchTitle = menuItemRecord.branchTitle;
        return mergeDefaults(ret);
    }

    private void closeStream() {
        // !keepStreamOpen &&
        if (ris != null && !ris.isClosed()) {
            try {
                ris.close();
            } catch (IOException ioe) {
                System.err.println("Error on closeStream(): " + ioe.getMessage());
            }
            ris = null;
        }
    }

    public ScreenRecord up() {
        /*
         * We do not need to close the stream here b/c we do not read the stream
         */
        // current screen is always on top. We want to pop the top then peek to get the record
        // that is now on top.
        menuLevelIndex.pop();
        return menuLevelIndex.size() == 0 ? null : mergeDefaults(peek().currScreenRecord);
    }

    public ScreenRecord next() throws CheckedException {
        MenuLevel screenSeriesLevel = peek();
        if (screenSeriesLevel.hasNext()) {
            screenSeriesLevel.setToNextScreen();
            try {
                return readSeriesCurrentScreen();
            } catch (IOException e) {
                handleIOException(e);
            }
        }
        return up();
    }

    public ScreenRecord previous() throws CheckedException {
        if (!hasPrevious())
            return up();

        peek().setToPreviousScreen();
        try {
            return readSeriesCurrentScreen();
        } catch (IOException e) {
            handleIOException(e);
        }
        return null;
    }

    public boolean hasPrevious() {
        return peek().hasPrevious();
    }

    public boolean hasNext() {
        return isCurrLevelScreenSeries();
    }

    public boolean isCurrLevelScreenSeries() {
        return peek().isScreenSeries();
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public synchronized void initProperties() throws CheckedException {
        Properties props = getProperties();

        // initialize the module alphabet
        String lowercaseChars = props.getProperty("alphabet.lowercase").trim();
        String uppercaseChars = props.getProperty("alphabet.uppercase").trim();
        alphabet = new Alphabet(lowercaseChars, uppercaseChars);

        if (audioFileType == null) {
            audioFileType = props.getProperty(Constants.AUDIO_FILETYPE);
            if (audioFileType == null || audioFileType.trim().length() == 0) {
                audioFileType = Constants.FILETYPE_ANY;
            }
        }

        imageFileType = props.getProperty(Constants.IMAGE_FILETYPE);
    }

    private MenuLevel peek() {
        return (MenuLevel) menuLevelIndex.peek();
    }

    protected Properties getProperties() throws CheckedException {
        Properties modProps = new Properties();
        String propsFilePath = modulePath + "/" + Constants.MODULE_PROPERTIES_FILENAME;
        InputStream in = null;
        try {
            in = streamProvider.getInputStream(propsFilePath);
            modProps.load(in, "UTF-8");
        } catch (IOException e) {
            CheckedException ce = new CheckedException(e, CheckedException.MODULE_PROPERTIES_READ_ERROR);
            ce.messageArgs = new String[] { propsFilePath };
            throw ce;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignoreCloseException) {
                }
            }
        }
        return modProps;
    }

    /*
     * Implement ResourceLoader iface
     */
    public InputStream loadAudio(String subpath) throws IOException {
        return streamProvider.getInputStream(subpath);
        // return streamProvider.getInputStream(getAudioResourcePath(subpath));
    }

    public InputStream loadImage(String subpath) throws IOException {
        subpath = fixSubPath(subpath, imageFileType, "png");
        return streamProvider.getInputStream(modulePath + "/images/" + subpath);
    }

    // public String getAudioMimeType() {
    // return audioMimeType;
    // }

    public String getAudioResourcePath(String subpath) {
        subpath = fixSubPath(subpath, audioFileType, "mp3");
        return modulePath + "/audio/" + subpath;
    }

    protected static String fixSubPath(String subpath, String fileType, String defaultType) {
        if (subpath.indexOf('.') == -1) {
            subpath = subpath + '.' + defaultType;
        }
        if (!Constants.FILETYPE_ANY.equals(fileType.trim().toLowerCase())) {
            subpath = subpath.substring(0, subpath.lastIndexOf('.')) + '.' + fileType;
        }
        return subpath;
    }

    public String getAudioResourceUrl(String subpath) {
        // return streamProvider.getUrl(subpath);
        return streamProvider.getUrl(getAudioResourcePath(subpath));
    }

}
