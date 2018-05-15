package net.hockeyapp.android.utils;

import java.io.PrintWriter;
import java.io.Writer;

public class BoundedPrintWriter extends PrintWriter {

    private final int maxLength;

    private int currentLength = 0;

    public BoundedPrintWriter(Writer out, int maxLength) {
        super(out);
        this.maxLength = maxLength;
    }

    @Override
    public void write(char[] buf, int off, int len) {
        if (currentLength + len < maxLength) {
            super.write(buf, off, len);
            currentLength += len;
        } else {
            super.write(buf, off, maxLength - currentLength);
            currentLength = maxLength;
        }
    }
}
