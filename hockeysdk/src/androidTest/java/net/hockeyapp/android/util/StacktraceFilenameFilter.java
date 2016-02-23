package net.hockeyapp.android.util;

import java.io.File;
import java.io.FilenameFilter;

public class StacktraceFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String filename) {
        return filename.endsWith(".stacktrace");
    }
}
