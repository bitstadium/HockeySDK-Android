package net.hockeyapp.android.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.utils.ImageUtils;

import java.io.File;
import java.io.IOException;

/**
 * <h3>Description</h3>
 * 
 * The view for an attachment.
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
public class AttachmentView extends FrameLayout {

  private final static int IMAGES_PER_ROW_PORTRAIT = 3;

  private final static int IMAGES_PER_ROW_LANDSCAPE = 2;

  private final Context context;

  private final ViewGroup parent;

  private final FeedbackAttachment attachment;

  private final Uri attachmentUri;

  private final String filename;

  private ImageView imageView;

  private TextView textView;

  private int widthPortrait;

  private int maxHeightPortrait;

  private int widthLandscape;

  private int maxHeightLandscape;

  private int gap;

  private int orientation;

  public AttachmentView(Context context, ViewGroup parent, Uri attachmentUri, boolean removable) {
    super(context);

    this.context = context;
    this.parent = parent;
    this.attachment = null;
    this.attachmentUri = attachmentUri;
    this.filename = attachmentUri.getLastPathSegment();

    calculateDimensions(20);
    initializeView(context, removable);

    textView.setText(filename);
    Bitmap bitmap = loadImageThumbnail();
    if (bitmap != null) {
      configureViewForThumbnail(bitmap, false);

    } else {
      configureViewForPlaceholder(false);
    }
  }

  public AttachmentView(Context context, ViewGroup parent, FeedbackAttachment attachment, boolean removable) {
    super(context);

    this.context = context;
    this.parent = parent;
    this.attachment = attachment;
    this.attachmentUri = Uri.fromFile(new File(Constants.getHockeyAppStorageDir(), attachment.getCacheId()));
    this.filename = attachment.getFilename();

    calculateDimensions(30);
    initializeView(context, removable);

    orientation = ImageUtils.ORIENTATION_PORTRAIT;
    textView.setText("Loading...");
    configureViewForPlaceholder(false);
  }

  public FeedbackAttachment getAttachment() {
    return attachment;
  }

  public Uri getAttachmentUri() { return attachmentUri; }

  public int getWidthPortrait() { return widthPortrait; }

  public int getMaxHeightPortrait() { return maxHeightPortrait; }

  public int getWidthLandscape() { return widthLandscape; }

  public int getMaxHeightLandscape() { return maxHeightLandscape; }

  public int getGap() { return gap; }

  public int getEffectiveMaxHeight() {
    return orientation == ImageUtils.ORIENTATION_LANDSCAPE ? maxHeightLandscape : maxHeightPortrait;
  }

  public void remove() {
    parent.removeView(this);
  }

  public void setImage(Bitmap bitmap, int orientation) {
    this.textView.setText(filename);
    this.orientation = orientation;

    if (bitmap == null) {
      configureViewForPlaceholder(true);

    } else {
      configureViewForThumbnail(bitmap, true);
    }
  }

  public void signalImageLoadingError() {
    textView.setText("Error");
  }

  private void calculateDimensions(int marginDip) {
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    this.gap = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, metrics));

    int layoutMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDip, metrics));
    int displayWidth = metrics.widthPixels;

    int parentWidthPortrait  = displayWidth - (2 * layoutMargin) - ((IMAGES_PER_ROW_PORTRAIT  - 1) * gap);
    int parentWidthLandscape = displayWidth - (2 * layoutMargin) - ((IMAGES_PER_ROW_LANDSCAPE - 1) * gap);

    this.widthPortrait  = parentWidthPortrait / IMAGES_PER_ROW_PORTRAIT;
    this.widthLandscape = parentWidthLandscape / IMAGES_PER_ROW_LANDSCAPE;

    this.maxHeightPortrait  = widthPortrait * 2;
    this.maxHeightLandscape = widthLandscape;
  }

  private void initializeView(Context context, boolean removable) {
    setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
    setPadding(0, gap, 0, 0);

    // ImageView
    imageView = new ImageView(context);

    // LinearLayout
    LinearLayout bottomView = new LinearLayout(context);
    bottomView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
    bottomView.setGravity(Gravity.LEFT);
    bottomView.setOrientation(LinearLayout.VERTICAL);
    bottomView.setBackgroundColor(Color.parseColor("#80262626"));

    // TextView
    textView = new TextView(context);
    textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    textView.setGravity(Gravity.CENTER);
    textView.setTextColor(Color.parseColor("#FFFFFF"));
    textView.setSingleLine();
    textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);

    // Remove Button
    if (removable) {
      ImageButton imageButton = new ImageButton(context);
      imageButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
      imageButton.setAdjustViewBounds(true);
      imageButton.setImageDrawable(getSystemIcon("ic_menu_delete"));
      imageButton.setBackgroundResource(0);
      imageButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          AttachmentView.this.remove();
        }
      });

      bottomView.addView(imageButton);
    }

    bottomView.addView(textView);
    addView(imageView);
    addView(bottomView);
  }

  private void configureViewForThumbnail(Bitmap bitmap, final boolean openOnClick) {
    int width  = orientation == ImageUtils.ORIENTATION_LANDSCAPE ? widthLandscape : widthPortrait;
    int height = orientation == ImageUtils.ORIENTATION_LANDSCAPE ? maxHeightLandscape : maxHeightPortrait;

    textView.setMaxWidth(width);
    textView.setMinWidth(width);

    imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    imageView.setAdjustViewBounds(true);
    imageView.setMinimumWidth(width);
    imageView.setMaxWidth(width);
    imageView.setMaxHeight(height);
    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    imageView.setImageBitmap(bitmap);
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!openOnClick) {
          return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(attachmentUri, "image/*");
        context.startActivity(intent);
      }
    });
  }

  private void configureViewForPlaceholder(final boolean openOnClick) {
    textView.setMaxWidth(widthPortrait);
    textView.setMinWidth(widthPortrait);

    imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    imageView.setAdjustViewBounds(false);
    imageView.setBackgroundColor(Color.parseColor("#eeeeee"));
    imageView.setMinimumHeight((int)(widthPortrait * 1.2f));
    imageView.setMinimumWidth(widthPortrait);
    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    imageView.setImageDrawable(getSystemIcon("ic_menu_attachment"));
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!openOnClick) {
          return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(attachmentUri, "*/*");
        context.startActivity(intent);
      }
    });
  }

  private Bitmap loadImageThumbnail() {
    try {
      orientation = ImageUtils.determineOrientation(context, attachmentUri);
      int width  = orientation == ImageUtils.ORIENTATION_LANDSCAPE ? widthLandscape : widthPortrait;
      int height = orientation == ImageUtils.ORIENTATION_LANDSCAPE ? maxHeightLandscape : maxHeightPortrait;

      return ImageUtils.decodeSampledBitmap(context, attachmentUri, width, height);
    }
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private Drawable getSystemIcon(String name) {
    return getResources().getDrawable(getResources().getIdentifier(name, "drawable", "android"));
  }
}
