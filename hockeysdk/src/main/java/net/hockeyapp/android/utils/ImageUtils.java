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
 * <h3>Description</h3>
 *
 * Various functions related to image loading and bitmap scaling.
 */
public class ImageUtils {
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    /**
     * Determines the orientation of the image based on its ratio.
     *
     * @param file the file handle of the image
     * @return The image orientation, either ORIENTATION_PORTRAIT or ORIENTATION_LANDSCAPE.
     * @throws IOException if the file couldn't be processed
     */
    public static int determineOrientation(File file) throws IOException {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            return determineOrientation(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * Determines the orientation of the image based on its ratio.
     *
     * @param context the context to use
     * @param uri     the URI of the image
     * @return The image orientation, either ORIENTATION_PORTRAIT or ORIENTATION_LANDSCAPE.
     * @throws IOException if the URI couldn't be processed
     */
    public static int determineOrientation(Context context, Uri uri) throws IOException {
        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);
            return determineOrientation(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * Determines the orientation of the image based on its ratio.
     *
     * @param input the input stream of the image
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
     *
     * @param file      the file handle of the image
     * @param reqWidth  required width
     * @param reqHeight required height
     * @return decoded the decoded bitmap
     * @throws IOException if the URI couldn't be processed
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
     *
     * @param context   the context to use
     * @param imageUri  the URI of the image
     * @param reqWidth  required width
     * @param reqHeight required height
     * @return decoded the decoded bitmap
     * @throws IOException if the URI couldn't be processed
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
     *
     * @param options   options that describe the image
     * @param reqWidth  required height
     * @param reqHeight required width
     * @return the scale factor
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
