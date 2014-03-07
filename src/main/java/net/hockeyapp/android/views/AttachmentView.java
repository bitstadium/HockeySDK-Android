package net.hockeyapp.android.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.tasks.AttachmentDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The view for an attachment.
 *
 * @author Patrick Eschenbach
 */
public class AttachmentView extends FrameLayout {

  private final static int IMAGES_PER_ROW = 3;

  private final Context context;

  private final ViewGroup parent;

  private final FeedbackAttachment attachment;

  private final Uri attachmentUri;

  private final String filename;

  private ImageView imageView;

  private TextView textView;

  private int horizontalGap;

  private int width;

  private int height;

  private boolean available;

  public AttachmentView(Context context, ViewGroup parent, Uri attachmentUri, boolean removable) {
    super(context);

    this.context = context;
    this.parent = parent;
    this.attachment = null;
    this.attachmentUri = attachmentUri;
    this.filename = attachmentUri.getLastPathSegment();
    this.available = true;

    calculateDimensions(20);
    configureView(context, removable);
  }

  public AttachmentView(Context context, ViewGroup parent, FeedbackAttachment attachment, boolean removable) {
    super(context);

    this.context = context;
    this.parent = parent;
    this.attachment = attachment;
    this.attachmentUri = Uri.fromFile(new File(AttachmentDownloader.getAttachmentStorageDir(), attachment.getCacheId()));
    this.filename = attachment.getFilename();
    this.available = false;

    calculateDimensions(30);
    configureView(context, removable);
  }

  public FeedbackAttachment getAttachment() {
    return attachment;
  }

  public Uri getAttachmentUri() { return attachmentUri; }

  public int getThumbnailWidth() { return width; }

  public int getThumbnailHeight() { return height; }

  public void remove(boolean deleteFromFileSystem) {
    parent.removeView(this);

    /* Re-adjust paddings of views */
    for (int i = 0; i < parent.getChildCount(); i++) {
      View view = parent.getChildAt(i);

      if ((i % IMAGES_PER_ROW) != 0) {
        view.setPadding(horizontalGap, horizontalGap, 0, 0);

      } else {
        view.setPadding(0, horizontalGap, 0, 0);
      }
    }

    if (deleteFromFileSystem) {
      //boolean success = new File(filePath).delete();
    }
  }

  public void setImage(Bitmap bitmap) {
    textView.setText(filename);
    available = true;

    if (bitmap == null) {
      configureImageViewForPlaceholder(available);

    } else {
      configureImageViewForThumbnail(bitmap, available);
    }
  }

  public void signalImageLoadingError() {
    textView.setText("Error");
    available = false;
  }

  private void calculateDimensions(int marginDip) {
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    int layoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDip, metrics);
    int imageGap     = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, metrics);
    int displayWidth = metrics.widthPixels;
    int parentWidth = displayWidth - (2 * layoutMargin) - (2 * imageGap);

    this.width =  parentWidth / IMAGES_PER_ROW;
    this.height = (int)(width * 1.2f);
  }

  private void configureView(final Context context, boolean removable) {
    setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    horizontalGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    if ((parent.getChildCount() % IMAGES_PER_ROW) != 0) {
      /** This is not the first child in the row, therefore it needs a padding to is left neighbour. */
      setPadding(horizontalGap, horizontalGap, 0, 0);

    } else {
      setPadding(0, horizontalGap, 0, 0);
    }

    LinearLayout bottomView = new LinearLayout(context);
    bottomView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
    bottomView.setGravity(Gravity.LEFT);
    bottomView.setOrientation(LinearLayout.VERTICAL);
    bottomView.setBackgroundColor(Color.parseColor("#80262626"));

    /* Configure TextView */
    textView = new TextView(context);
    textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    textView.setGravity(Gravity.CENTER);
    textView.setTextColor(Color.parseColor("#FFFFFF"));
    textView.setSingleLine();
    textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
    textView.setMaxWidth(width);
    textView.setMinWidth(width);

    /* Configure ImageView */
    imageView = new ImageView(context);
    if (available) {
      textView.setText(filename);
      Bitmap bitmap = loadImageThumbnail();
      if (bitmap != null) {
        configureImageViewForThumbnail(bitmap, false);

      } else {
        configureImageViewForPlaceholder(false);
      }

    } else {
      textView.setText("Loading...");
      configureImageViewForPlaceholder(available);
    }

    /* Configure remove button. */
    if (removable) {
      ImageButton imageButton = new ImageButton(context);
      imageButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
      imageButton.setAdjustViewBounds(true);
      imageButton.setImageDrawable(getSystemIcon("ic_menu_delete"));
      imageButton.setBackgroundResource(0);
      imageButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          AttachmentView.this.remove(false);
        }
      });

      bottomView.addView(imageButton);
    }

    bottomView.addView(textView);
    addView(imageView);
    addView(bottomView);
  }

  private Bitmap loadImageThumbnail() {
    try {
      InputStream input = context.getContentResolver().openInputStream(attachmentUri);
      Bitmap bitmap = BitmapFactory.decodeStream(input);
      return bitmap == null ? null : Bitmap.createScaledBitmap(bitmap, width, height, false);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void configureImageViewForThumbnail(Bitmap bitmap, final boolean available) {
    imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    imageView.setAdjustViewBounds(true);
    imageView.setMinimumHeight(height);
    imageView.setMinimumWidth(width);
    //imageView.setMaxHeight(height);
    //imageView.setMaxWidth(width);
    imageView.setImageBitmap(bitmap);
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!available) {
          return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(attachmentUri, "image/*");
        context.startActivity(intent);
      }
    });
  }

  private void configureImageViewForPlaceholder(final boolean available) {
    imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    imageView.setAdjustViewBounds(false);
    imageView.setBackgroundColor(Color.parseColor("#eeeeee"));
    imageView.setMinimumHeight(height);
    imageView.setMinimumWidth(width);
    //imageView.setMaxHeight(height);
    //imageView.setMaxWidth(width);
    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    imageView.setImageDrawable(getSystemIcon("ic_menu_attachment"));
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!available) {
          return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(attachmentUri, "*/*");
        context.startActivity(intent);
      }
    });
  }

  private Drawable getSystemIcon(String name) {
    return getResources().getDrawable(getResources().getIdentifier(name, "drawable", "android"));
  }
}
