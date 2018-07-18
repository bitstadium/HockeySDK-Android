package net.hockeyapp.android.utils;

import java.io.IOException;
import java.io.Writer;

public class BoundedPrintWriter extends Writer {

    private final int maxLength;
    private final Writer out;
    private int currentLength = 0;

    public BoundedPrintWriter(Writer out, int maxLength) {
        super(out);
        this.out = out;
        this.maxLength = maxLength;
    }

    @Override
    public void write(char[] buf, int off, int len) {
        try {
            if (currentLength + len < maxLength) {
                out.write(buf, off, len);
                currentLength += len;
            } else {
                out.write(buf, off, maxLength - currentLength);
                currentLength = maxLength;
            }
        }
        catch (IOException ignored) { }
    }

    @Override
    public void close() {
        try {
            if (out == null) {
                return;
            }
            out.close();
        }
        catch (IOException ignored) {}
    }

    @Override
    public void flush() {
        try {
            out.flush();
        }
        catch (IOException ignored) {}
    }
}
