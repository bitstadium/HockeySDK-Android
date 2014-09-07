package net.hockeyapp.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <h4>Description</h4>
 * 
 * Various functions related to image loading and bitmap scaling.
 * 
 * <h4>License</h4>
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
public class ImageUtils {
  public static final int ORIENTATION_PORTRAIT  = 0;
  public static final int ORIENTATION_LANDSCAPE = 1;

  /**
   * Determines the orientation of the image based on its ratio.
   *
   * @return The image orientation, either ORIENTATION_PORTRAIT or ORIENTATION_LANDSCAPE.
   */
  public static int determineOrientation(File file) throws IOException {
    InputStream input = null;
    try {
      input = new FileInputStream(file);
      return determineOrientation(input);
    }
    finally {
      input.close();
    }
  }

  /**
   * Determines the orientation of the image based on its ratio.
   *
   * @return The image orientation, either ORIENTATION_PORTRAIT or ORIENTATION_LANDSCAPE.
   */
  public static int determineOrientation(Context context, Uri uri) throws IOException {
    InputStream input = null;
    try {
      input = context.getContentResolver().openInputStream(uri);
      return determineOrientation(input);
    }
    finally {
      input.close();
    }
  }

  /**
   * Determines the orientation of the image based on its ratio.
   *
   * @return The image orientation, either ORIENTATION_PORTRAIT or ORIENTATION_LANDSCAPE.
   */
  public static int determineOrientation(InputStream input) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    BitmapFactory.decodeStream(input, null, options);
    if (options.outWidth == -1 || options.outHeight == -1) {
      return ORIENTATION_PORTRAIT;
    }

    // Choose orientation based on image ratio.
    float ratio = ((float) options.outWidth) / ((float) options.outHeight);
    return ratio > 1 ? ORIENTATION_LANDSCAPE : ORIENTATION_PORTRAIT;
  }

  /**
   * Decodes the image as a bitmap with a size as small as possible but with a minimum size of given reqWidth
   * and reqHeight.
   *
   * Based on: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
   */
  public static Bitmap decodeSampledBitmap(File file, int reqWidth, int reqHeight) throws IOException {
    // First decode with inJustDecodeBounds=true to check dimensions
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    BitmapFactory.decodeFile(file.getAbsolutePath(), options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

    return bitmap;
  }

  /**
   * Decodes the image as a bitmap with a size as small as possible but with a minimum size of given reqWidth
   * and reqHeight.
   *
   * Based on: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
   */
  public static Bitmap decodeSampledBitmap(Context context, Uri imageUri, int reqWidth, int reqHeight) throws IOException {
    // First decode with inJustDecodeBounds=true to check dimensions
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    InputStream inputBounds = context.getContentResolver().openInputStream(imageUri);
    BitmapFactory.decodeStream(inputBounds, null, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    InputStream inputBitmap = context.getContentResolver().openInputStream(imageUri);
    Bitmap bitmap = BitmapFactory.decodeStream(inputBitmap, null, options);

    return bitmap;
  }

  /**
   * Calculates the scale factor to scale down the image as much as possible while preserving a minimum size
   * defined by the given reqWidth and reqHeight.
   *
   * See: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
   */
  private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }
}
