package net.hockeyapp.android.objects;

import net.hockeyapp.android.Constants;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;

/**
 * <h3>Description</h3>
 * 
 * Model for feedback attachments.
 * 
 * <h3>License</h3>
 * 
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

  private int id;
  private int messageId;
  private String filename;
  private String url;
  private String createdAt;
  private String updatedAt;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getMessageId() { return messageId; }

  public void setMessageId(int messageId) { this.messageId = messageId; }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getUpdatedAt() { return updatedAt; }

  public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

  /**
   * Returns the attachment's filename that identifies it in the cache.
   *
   * @return the filename in the cache.
   */
  public String getCacheId() {
    return "" + messageId + id;
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
        + "\n" + "id         " + id
        + "\n" + "message id " + messageId
        + "\n" + "filename   " + filename
        + "\n" + "url        " + url
        + "\n" + "createdAt  " + createdAt
        + "\n" + "updatedAt  " + updatedAt
        ;
  }
}
