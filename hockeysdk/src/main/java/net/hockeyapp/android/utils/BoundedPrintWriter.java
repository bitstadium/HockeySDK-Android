package net.hockeyapp.android.utils;

import java.io.PrintWriter;
import java.io.Writer;

public class BoundedPrintWriter extends PrintWriter {

    private final int maxLength;
    private final String lineSeparator;

    private int currentLength = 0;

    public BoundedPrintWriter(Writer out, int maxLength) {
        super(out);
        this.maxLength = maxLength;
        this.lineSeparator = System.getProperty("line.separator");
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

    @Override
    public void write(String s, int off, int len) {
        char [] buffer = s.toCharArray();
        write(buffer, 0, buffer.length);
    }

    @Override
    public void println() {
        write(lineSeparator);
    }
}
