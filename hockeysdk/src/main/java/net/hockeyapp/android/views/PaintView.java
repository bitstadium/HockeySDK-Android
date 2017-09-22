package net.hockeyapp.android.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.widget.ImageView;

import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.ImageUtils;

import java.io.IOException;
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

    private Path path;
    private Stack<Path> paths;
    private Paint paint;
    private float mX, mY;

    @SuppressLint("StaticFieldLeak")
    public PaintView(Context context, Uri imageUri, int displayWidth, int displayHeight) {
        super(context);

        path = new Path();
        paths = new Stack<>();
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
                    return ImageUtils.decodeSampledBitmap(context, imageUri, displayWidth, displayHeight);
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
