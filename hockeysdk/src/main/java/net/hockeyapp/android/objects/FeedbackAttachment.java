package net.hockeyapp.android.objects;

import net.hockeyapp.android.Constants;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;

/**
 * <h3>Description</h3>
 * <p/>
 * Model for feedback attachments.
 * <p/>
 * <h3>License</h3>
 * <p/>
 * <pre>
 * Copyright (c) 2011-2014 Bit Stadium GmbH
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * </pre>
 *
 * @author Patrick Eschenbach
 */
public class FeedbackAttachment implements Serializable {

    private static final long serialVersionUID = 5059651319640956830L;

    private int mId;
    private int mMessageId;
    private String mFilename;
    private String mUrl;
    private String mCreatedAt;
    private String mUpdatedAt;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getMessageId() {
        return mMessageId;
    }

    public void setMessageId(int messageId) {
        this.mMessageId = messageId;
    }

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String filename) {
        this.mFilename = filename;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.mCreatedAt = createdAt;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.mUpdatedAt = updatedAt;
    }

    /**
     * Returns the attachment's filename that identifies it in the cache.
     *
     * @return the filename in the cache.
     */
    public String getCacheId() {
        return "" + mMessageId + mId;
    }

    /**
     * Checks if attachment has already been downloaded.
     *
     * @return true if available, false if not.
     */
    public boolean isAvailableInCache() {
        File folder = Constants.getHockeyAppStorageDir();
        if (folder.exists() && folder.isDirectory()) {
            File[] match = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.equals(getCacheId());
                }
            });

            return match != null && match.length == 1;

        } else return false;
    }

    @Override
    public String toString() {
        return "\n" + FeedbackAttachment.class.getSimpleName()
                + "\n" + "id         " + mId
                + "\n" + "message id " + mMessageId
                + "\n" + "filename   " + mFilename
                + "\n" + "url        " + mUrl
                + "\n" + "createdAt  " + mCreatedAt
                + "\n" + "updatedAt  " + mUpdatedAt
                ;
    }
}
