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

import junit.framework.TestCase;

import org.edc.sstone.Constants;
import org.edc.sstone.io.ClasspathInputStreamProvider;
import org.edc.sstone.record.reader.RecordFactory;
import org.edc.sstone.record.reader.model.IntArrayRecord;
import org.edc.sstone.record.reader.model.MenuItemRecord;
import org.edc.sstone.record.reader.model.Record;
import org.edc.sstone.record.reader.model.ResourceComponentRecord;
import org.edc.sstone.record.reader.model.ScreenRecord;
import org.edc.sstone.record.reader.model.ScreenSeriesRecord;
import org.edc.sstone.record.reader.model.StyleRecord;
import org.edc.sstone.record.reader.model.TextAreaComponentRecord;
import org.edc.sstone.text.Alphabet;

/**
 * @author Greg Orlowski
 */
public class ModuleManagerTest extends TestCase {

    private static final int MAIN_MENU_IMAGE_IDX = 0;
    private static final int MAIN_MENU_READING_MI_IDX = 1;
    private static final int MAIN_MENU_MATH_MI_IDX = 2;
    private static final int MAIN_MENU_AUDIO_MI_IDX = 3;

    private static final int MATH_SCREEN_COUNT = 2;

    public void testGetAlphabet() throws Exception {
        ModuleManager nav = getNavigator();
        Alphabet alphabet = nav.getAlphabet();

        assertEquals("abc", alphabet.toLowerCase("ABC"));
    }

    public void testFirstScreen() throws Exception {
        ModuleManager nav = getNavigator();
        ScreenRecord firstScreen = nav.firstScreen();
        assertTrue(isStreamClosed(nav));

        assertEquals("Stepping Stone", firstScreen.title);

        // assertEquals("module", nav.moduleRecord.title);
        // assertEquals("module_logo", nav.moduleRecord.iconImagePath);

        // first screen
        assertEquals(4, firstScreen.componentRecords.size());

        MenuItemRecord mir = (MenuItemRecord) firstScreen.componentRecords.elementAt(MAIN_MENU_READING_MI_IDX);
        assertEquals("Reading and Writing", mir.title);

        ResourceComponentRecord image = (ResourceComponentRecord) firstScreen.componentRecords
                .elementAt(MAIN_MENU_IMAGE_IDX);
        assertEquals("ss_logo", image.resourcePath);
    }

    /*
     * NOTE: we test that the stream is closed after each navigation pass b/c, since we're targeting
     * devices with low memory, we want to make sure we free up resources each time we will load and
     * display a screen.
     * 
     * TODO: break up the parts of this into methods like:
     * descendAndEnsureEndOfSeriesReturnsBackUp...
     * 
     * Since there is a lot of boilerplate code, we do not want/need tests that repeat navigating to
     * the first record, etc. So keep this as 1 test but then have the test execute methods with
     * self-documenting names.
     */
    public void testDescendNextPreviousAndUp() throws Exception {
        ModuleManager nav = getNavigator();
        ScreenRecord mainMenu = nav.firstScreen();

        /*
         * TODO: add more assertions for properties of next, previous, etc. screens. I also need to
         * figure out where to handle defaulting the title + style.
         */
        MenuItemRecord mir = (MenuItemRecord) mainMenu.componentRecords.elementAt(MAIN_MENU_READING_MI_IDX);
        ScreenRecord screen1 = nav.descend(mir);
        assertTrue(isStreamClosed(nav));

        assertEquals("screen1", screen1.title);
        assertFalse(nav.hasPrevious());

        // navigate to next screen
        ScreenRecord screen2 = nav.next();
        assertTrue(isStreamClosed(nav));

        assertEquals("screen2", screen2.title);
        assertTrue(nav.hasPrevious());

        // navigate to previous screen
        ScreenRecord previousScreen = nav.previous();
        assertTrue(isStreamClosed(nav));

        assertEquals(screen1.title, previousScreen.title);
        assertEquals(screen1.getHeader().getRecordDataStartPos(),
                previousScreen.getHeader().getRecordDataStartPos());

        // navigate to next again and then up.
        assertEquals(nav.next().getHeader().getRecordDataStartPos(),
                screen2.getHeader().getRecordDataStartPos());

        ScreenRecord backToMainMenu = nav.up();
        assertTrue(isStreamClosed(nav));

        assertEquals(mainMenu.title, backToMainMenu.title);

        descendAndEnsureEndOfSeriesReturnsBackUp(nav, mainMenu);
        testAudioSeriesScreen(nav, mainMenu);

        testBranchDefaults(nav, mainMenu);
    }

    private void testBranchDefaults(ModuleManager nav, ScreenRecord mainMenu) throws Exception {
        ScreenRecord mathScreen1 = nav.descend((MenuItemRecord) mainMenu.componentRecords
                .elementAt(MAIN_MENU_MATH_MI_IDX));
        assertEquals("math_screen1", mathScreen1.title);

        /*
         * Now test style defaults
         */
        assertEquals(8, mathScreen1.styleRecord.margin.getTop());
        assertEquals(Constants.FONT_FACE_MONOSPACE, mathScreen1.styleRecord.fontStyle.getFace());
        assertEquals(Constants.FONT_SIZE_MEDIUM, mathScreen1.styleRecord.fontStyle.getSize());
        assertTrue(mathScreen1.styleRecord.fontStyle.isStrikeThrough());

        ScreenRecord mathScreen2 = nav.next();

        // The style should be the closest MenuItemRecord#branchStyleRecord up the tree b/c
        // we do not define a style
        assertEquals((short) 2, mathScreen2.styleRecord.padding);
        assertEquals(Constants.FONT_FACE_PROPORTIONAL, mathScreen2.styleRecord.fontStyle.getFace());
        assertFalse(mathScreen2.styleRecord.fontStyle.isStrikeThrough());
        assertTrue(mathScreen2.styleRecord.fontStyle.isEnableMagnification());

        // The title should inherit from the closest MenuItemRecord branchTitle up the tree
        // because the 2nd screen does not define a title
        assertEquals("Math Series", mathScreen2.title);

        nav.up(); // return to main menu
    }

    public void testGetBranchStyle() throws Exception {
            ModuleManager nav = getNavigator();
            ScreenRecord firstScreen = nav.firstScreen();
            assertTrue(isStreamClosed(nav));
            assertNotNull(firstScreen);
    
            // We can unit test this...
            StyleRecord branchStyle = nav.getBranchStyle();
            assertEquals((short) 2, branchStyle.padding);
            assertEquals(0xFFEE00, branchStyle.highlightColor);
            assertEquals((short) 8, branchStyle.animationStartDelay);
            assertEquals((short) 11, branchStyle.animationPeriod);
    
            assertEquals(Constants.FONT_FACE_PROPORTIONAL, branchStyle.fontStyle.getFace());
        }

    protected void descendAndEnsureEndOfSeriesReturnsBackUp(ModuleManager nav, ScreenRecord mainMenu) throws Exception {
        ScreenRecord mathScreen1 = nav.descend((MenuItemRecord) mainMenu.componentRecords
                .elementAt(MAIN_MENU_MATH_MI_IDX));
        assertEquals("math_screen1", mathScreen1.title);
        assertFalse(nav.hasPrevious());

        // We want to call next MATH_SCREEN_COUNT (not MATH_SCREEN_COUNT-1) times because
        // when we call next() while positioned on the last screen in the series, we should
        // return to the menu above (in this case the main menu).
        ScreenRecord backToMain = null;
        for (int i = 0; i < MATH_SCREEN_COUNT; i++) {
            backToMain = nav.next();
        }

        assertEquals(mainMenu.title, backToMain.title);
        assertEquals(mainMenu.getHeader().getRecordDataStartPos(),
                backToMain.getHeader().getRecordDataStartPos());
    }

    protected void testAudioSeriesScreen(ModuleManager nav, ScreenRecord mainMenu) throws Exception {
        ScreenRecord audioMenuScreen = nav.descend((MenuItemRecord) mainMenu.componentRecords
                .elementAt(MAIN_MENU_AUDIO_MI_IDX));
        assertEquals("audio_series_menu_screen", audioMenuScreen.title);
        assertFalse(nav.hasPrevious());

        // The 2nd component
        ScreenRecord audioSlide1 = nav.descend((MenuItemRecord) audioMenuScreen.componentRecords.elementAt(1));
        // assertEquals("null", nav.getAudioResourceUrl(audioSlide1.resourcePath));
        String audioPath = nav.getAudioResourcePath(audioSlide1.resourcePath);
        assertEquals("clip1.mp3", audioPath.substring(audioPath.lastIndexOf('/') + 1));

        // back to audioMenuScreen
        nav.up();

        // We have to navigate "up" not next b/c we are not in a series.
        ScreenRecord backToMain = nav.up();
        assertEquals(mainMenu.title, backToMain.title);
        assertEquals(mainMenu.getHeader().getRecordDataStartPos(),
                backToMain.getHeader().getRecordDataStartPos());
    }

    protected boolean isStreamClosed(ModuleManager nav) {
        return nav.ris == null;
    }

    static RecordFactory getFactory() {
        RecordFactory rf = new RecordFactory();

        Record[] recordObjects = new Record[] {
                new StyleRecord(),
                new TextAreaComponentRecord(),
                new ScreenRecord(),
                new ScreenSeriesRecord(),
                new MenuItemRecord(),
                new IntArrayRecord(),
                new ResourceComponentRecord()
        };

        for (int i = 0; i < recordObjects.length; i++)
            rf.registerType(recordObjects[i]);

        return rf;
    }

    /**
     * Just make sure mod3, which uses a greater variety of components and attributes, loads.
     */
    public void testMod3Load() throws Exception {
        getNavigator("/modules/mod3").firstScreen();
    }

    static ModuleManager getNavigator() throws Exception {
        return getNavigator("/modules/mod1");
    }

    static ModuleManager getNavigator(String modPath) throws Exception {
        RecordFactory rf = getFactory();
        ModuleManager ret = new ModuleManager(rf, modPath, new ClasspathInputStreamProvider());
        ret.initProperties();
        return ret;
    }

    public void testFixSubPath() {
        String path = "f.png";
        assertEquals(path, ModuleManager.fixSubPath(path, "png", "png"));
        assertEquals("f.gif", ModuleManager.fixSubPath(path, "gif", "png"));

        assertEquals("f.gif", ModuleManager.fixSubPath("f", "gif", "png"));
    }

}
