package net.hockeyapp.android.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.*;
import java.util.Random;

/**
 * To avoid external apache library "httpmime" this is a simple implementation for a MultipartEntity.
 * Please note that first all key value pairs have to be written and then at least one file part has to be added.
 * Otherwise the boundaries are not written correctly.
 *
 * Based on:
 * http://derivedcode.wordpress.com/2014/11/04/android-uploading-files-to-server-over-http-using-multipart-entity
 *
 * @author Patrick Eschenbach
 */
public class SimpleMultipartEntity implements HttpEntity {

  private final static char[] BOUNDARY_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  private boolean isSetLast;

  private boolean isSetFirst;

  private ByteArrayOutputStream out;

  private String boundary;

  public SimpleMultipartEntity() {
    this.isSetFirst = false;
    this.isSetLast  = false;
    this.out = new ByteArrayOutputStream();

    /** Create boundary String */
    final StringBuffer buffer = new StringBuffer();
    final Random rand = new Random();

    for (int i = 0; i < 30; i++) {
      buffer.append(BOUNDARY_CHARS[rand.nextInt(BOUNDARY_CHARS.length)]);
    }
    this.boundary = buffer.toString();
  }

  public String getBoundary() {
    return boundary;
  }

  public void writeFirstBoundaryIfNeeds() throws IOException {
    if (!isSetFirst) {
      out.write(("--" + boundary + "\r\n").getBytes());
    }
    isSetFirst = true;
  }

  public void writeLastBoundaryIfNeeds() {
    if (isSetLast) {
      return;
    }
    try {
      out.write(("\r\n--" + boundary + "--\r\n").getBytes());

    } catch (final IOException e) {
      e.printStackTrace();
    }
    isSetLast = true;
  }

  public void addPart(final String key, final String value) throws IOException {
    writeFirstBoundaryIfNeeds();

    out.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes());
    out.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes());
    out.write("Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes());
    out.write(value.getBytes());
    out.write(("\r\n--" + boundary + "\r\n").getBytes());
  }

  public void addPart(final String key, final File value, boolean lastFile) throws IOException {
    addPart(key, value.getName(), new FileInputStream(value), lastFile);
  }

  public void addPart(final String key, final String fileName, final InputStream fin, boolean lastFile) throws IOException {
    addPart(key, fileName, fin, "application/octet-stream", lastFile);
  }

  public void addPart(final String key, final String fileName, final InputStream fin, String type, boolean lastFile) throws IOException {
    writeFirstBoundaryIfNeeds();
    try {
      type = "Content-Type: " + type + "\r\n";
      out.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
      out.write(type.getBytes());
      out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

      final byte[] tmp = new byte[4096];
      int l = 0;
      while ((l = fin.read(tmp)) != -1) {
        out.write(tmp, 0, l);
      }
      out.flush();

      if (lastFile) {
        /** This is the last file: write last boundary. */
        writeLastBoundaryIfNeeds();

      } else {
        /** Another file will follow: write normal boundary. */
        out.write(("\r\n--" + boundary + "\r\n").getBytes());
      }

    } finally {
      try {
        fin.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public long getContentLength() {
    writeLastBoundaryIfNeeds();
    return out.toByteArray().length;
  }

  @Override
  public Header getContentType() {
    return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + getBoundary());
  }

  @Override
  public boolean isChunked() {
    return false;
  }

  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public boolean isStreaming() {
    return false;
  }

  @Override
  public void writeTo(final OutputStream outstream) throws IOException {
    outstream.write(out.toByteArray());
  }

  @Override
  public Header getContentEncoding() {
    return null;
  }

  @Override
  public void consumeContent() throws IOException, UnsupportedOperationException {
    if (isStreaming()) {
      throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
    }
  }

  @Override
  public InputStream getContent() throws IOException, UnsupportedOperationException {
    return new ByteArrayInputStream(out.toByteArray());
  }
}
