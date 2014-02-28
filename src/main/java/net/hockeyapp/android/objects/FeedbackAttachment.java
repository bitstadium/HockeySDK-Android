package net.hockeyapp.android.objects;

import net.hockeyapp.android.tasks.AttachmentDownloader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;

/**
 * Entity Class for Attachments.
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
    File folder = AttachmentDownloader.getAttachmentStorageDir();
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
