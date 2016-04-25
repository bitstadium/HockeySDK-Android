package net.hockeyapp.android.views;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.widget.ImageView;

import net.hockeyapp.android.utils.HockeyLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/**
 * <h3>Description</h3>
 *
 * The PaintView for showing the image and drawing on it.
 *
 */
@SuppressLint("ViewConstructor")
public class PaintView extends ImageView {

    private static final float TOUCH_TOLERANCE = 4;

    /**
     * Determines the orientation of the image based on its ratio and returns the orientation the activity
     * should have.
     *
     * @param resolver a content resolver
     * @param imageUri the URI for the image
     * @return the desired activity orientation
     */
    public static int determineOrientation(ContentResolver resolver, Uri imageUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {
            InputStream in = resolver.openInputStream(imageUri);
            BitmapFactory.decodeStream(in, null, options);

      /* Choose orientation based on image ratio. */
            float ratio = ((float) options.outWidth) / ((float) options.outHeight);
            return ratio > 1 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        } catch (IOException e) {
            HockeyLog.error("Unable to determine necessary screen orientation.", e);
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
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
    /* Raw height and width of image */
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

      /* Calculate the largest inSampleSize value that is a power of 2 and keeps both
         height and width larger than the requested height and width */
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Decodes the image as a bitmap with a size as small as possible but with a minimum size of given reqWidth
     * and reqHeight.
     *
     * Based on: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     *
     * @param resolver  a content resolver
     * @param imageUri  the URI for the image
     * @param reqWidth  required height
     * @param reqHeight required width
     * @return the decoded bitmap
     */
    private static Bitmap decodeSampledBitmapFromResource(ContentResolver resolver, Uri imageUri, int reqWidth, int reqHeight) throws IOException {
    /* First decode with inJustDecodeBounds=true to check dimensions */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        InputStream inputBounds = resolver.openInputStream(imageUri);
        BitmapFactory.decodeStream(inputBounds, null, options);

    /* Calculate inSampleSize */
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    /* Decode bitmap with inSampleSize set */
        options.inJustDecodeBounds = false;
        InputStream inputBitmap = resolver.openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputBitmap, null, options);

        return bitmap;
    }

    private Path path;
    private Stack<Path> paths;
    private Paint paint;
    private float mX, mY;

    public PaintView(Context context, Uri imageUri, int displayWidth, int displayHeight) {
        super(context);

        path = new Path();
        paths = new Stack<Path>();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);

        new AsyncTask<Object, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
        /* This is essential to make the image view to wrap exactly the displayed image and avoiding any
        empty space around it that would be drawable but doesn't belong to the image. */
                setAdjustViewBounds(true);
            }

            @Override
            protected Bitmap doInBackground(java.lang.Object... args) {
                Context context = (Context) args[0];
                Uri imageUri = (Uri) args[1];
                Integer displayWidth = (Integer) args[2];
                Integer displayHeight = (Integer) args[3];
                try {
                    Bitmap bm = decodeSampledBitmapFromResource(context.getContentResolver(), imageUri, displayWidth, displayHeight);
                    return bm;
                } catch (IOException e) {
                    HockeyLog.error("Could not load image into ImageView.", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bm) {
                if (bm == null)
                    return;
                setImageBitmap(bm);
            }
        }.execute(context, imageUri, displayWidth, displayHeight);
    }

    public void clearImage() {
        paths.clear();
        invalidate();
    }

    public void undo() {
        if (!paths.empty()) {
            paths.pop();
            invalidate();
        }
    }

    public boolean isClear() {
        return paths.empty();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    /* Draw existing paths. */
        for (Path path : paths) {
            canvas.drawPath(path, paint);
        }

    /* Draw current path. */
        canvas.drawPath(path, paint);
    }

    private void touchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        path.lineTo(mX, mY);
        paths.push(path);
        path = new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}
