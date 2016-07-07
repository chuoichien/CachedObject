/**
 * Constants.java
 * <p/>
 * Purpose              :
 * <p/>
 * Optional info        :
 *
 * @author : Huy Duong Tu
 * @date : 26 Sep 2012
 * @lastChangedRevision :
 * @lastChangedDate :
 */
package com.banana.cachedutils;


import android.os.Environment;

import java.io.File;

/**
 * @author Thanhlnh
 */
public class Constants {

    public static final String DIR = "CachedUtils";
    public static final String DIR_CACHED = "Cached";
    public static final String FULL_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DIR;
    public static final String CACHED_DIRECTORY = FULL_DIR + File.separator + DIR_CACHED;
}
