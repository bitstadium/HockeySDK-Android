package net.hockeyapp.android.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.R;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.utils.ImageUtils;
import net.hockeyapp.android.utils.Util;

import java.io.File;

/**
 * <h3>Description</h3>
 *
 * The view for an attachment.
 */
@SuppressLint("ViewConstructor")
public class AttachmentView extends FrameLayout {

    private final static int IMAGES_PER_ROW_PORTRAIT = 3;

    private final static int IMAGES_PER_ROW_LANDSCAPE = 2;

    private final Context mContext;

    private final ViewGroup mParent;

    private final FeedbackAttachment mAttachment;

    private final Uri mAttachmentUri;

    private final String mFilename;

    private ImageView mImageView;

    private TextView mTextView;

    private int mWidthPortrait;

    private int mMaxHeightPortrait;

    private int mWidthLandscape;

    private int mMaxHeightLandscape;

    private int mGap;

    private int mOrientation;

    public AttachmentView(Context context, ViewGroup parent, Uri attachmentUri, boolean removable) {
        super(context);

        this.mContext = context;
        this.mParent = parent;
        this.mAttachment = null;
        this.mAttachmentUri = attachmentUri;
        this.mFilename = attachmentUri.getLastPathSegment();

        calculateDimensions(20);
        initializeView(context, removable);

        mTextView.setText(mFilename);
        mTextView.setContentDescription(mTextView.getText());
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... args) {
                return loadImageThumbnail();
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    configureViewForThumbnail(bitmap, false);
                } else {
                    configureViewForPlaceholder(false);
                }
            }
        }.execute();
    }

    public AttachmentView(Context context, ViewGroup parent, FeedbackAttachment attachment, boolean
            removable) {
        super(context);

        this.mContext = context;
        this.mParent = parent;
        this.mAttachment = attachment;
        this.mAttachmentUri = Uri.fromFile(new File(Constants.getHockeyAppStorageDir(), attachment
                .getCacheId()));
        this.mFilename = attachment.getFilename();

        calculateDimensions(30);
        initializeView(context, removable);

        mOrientation = ImageUtils.ORIENTATION_PORTRAIT;
        mTextView.setText(R.string.hockeyapp_feedback_attachment_loading);
        mTextView.setContentDescription(mTextView.getText());
        configureViewForPlaceholder(false);
    }

    public FeedbackAttachment getAttachment() {
        return mAttachment;
    }

    public Uri getAttachmentUri() {
        return mAttachmentUri;
    }

    public int getWidthPortrait() {
        return mWidthPortrait;
    }

    public int getMaxHeightPortrait() {
        return mMaxHeightPortrait;
    }

    public int getWidthLandscape() {
        return mWidthLandscape;
    }

    public int getMaxHeightLandscape() {
        return mMaxHeightLandscape;
    }

    public int getGap() {
        return mGap;
    }

    public int getEffectiveMaxHeight() {
        return mOrientation == ImageUtils.ORIENTATION_LANDSCAPE ? mMaxHeightLandscape : mMaxHeightPortrait;
    }

    public void remove() {
        Util.announceForAccessibility(mParent, mContext.getString(R.string.hockeyapp_feedback_attachment_removed));
        mParent.removeView(this);
    }

    public void setImage(Bitmap bitmap, int orientation) {
        mTextView.setText(mFilename);
        mTextView.setContentDescription(mTextView.getText());
        mOrientation = orientation;

        if (bitmap == null) {
            configureViewForPlaceholder(true);

        } else {
            configureViewForThumbnail(bitmap, true);
        }
    }

    public void signalImageLoadingError() {
        mTextView.setText(R.string.hockeyapp_feedback_attachment_error);
        mTextView.setContentDescription(mTextView.getText());
    }

    private void calculateDimensions(int marginDip) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        this.mGap = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, metrics));

        int layoutMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                marginDip, metrics));
        int displayWidth = metrics.widthPixels;

        int parentWidthPortrait = displayWidth - (2 * layoutMargin) - ((IMAGES_PER_ROW_PORTRAIT -
                1) * this.mGap);
        // (IMAGES_PER_ROW_LANDSCAPE - 1) * this.gap == 1, so just using this.gap
        int parentWidthLandscape = displayWidth - (2 * layoutMargin) - this.mGap;

        this.mWidthPortrait = parentWidthPortrait / IMAGES_PER_ROW_PORTRAIT;
        this.mWidthLandscape = parentWidthLandscape / IMAGES_PER_ROW_LANDSCAPE;

        this.mMaxHeightPortrait = mWidthPortrait * 2;
        //noinspection SuspiciousNameCombination
        this.mMaxHeightLandscape = mWidthLandscape;
    }

    @SuppressWarnings("deprecation")
    private void initializeView(Context context, boolean removable) {
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM));
        setPadding(0, mGap, 0, 0);

        Util.announceForAccessibility(mParent, mContext.getString(R.string.hockeyapp_feedback_attachment_added));

        // ImageView
        mImageView = new ImageView(context);

        // LinearLayout
        LinearLayout bottomView = new LinearLayout(context);
        bottomView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams
                .WRAP_CONTENT, Gravity.BOTTOM));
        bottomView.setGravity(Gravity.START);
        bottomView.setOrientation(LinearLayout.VERTICAL);
        bottomView.setBackgroundColor(Color.parseColor("#80262626"));

        // TextView
        mTextView = new TextView(context);
        mTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams
                .WRAP_CONTENT, Gravity.CENTER));
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextColor(context.getResources().getColor(R.color.hockeyapp_text_white));
        mTextView.setSingleLine();
        mTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);

        // Remove Button
        if (removable) {
            ImageButton imageButton = new ImageButton(context);
            imageButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams
                    .WRAP_CONTENT, Gravity.BOTTOM));
            imageButton.setAdjustViewBounds(true);
            imageButton.setImageDrawable(getSystemIcon("ic_menu_delete"));
            imageButton.setBackgroundResource(0);
            imageButton.setContentDescription(context.getString(R.string.hockeyapp_feedback_attachment_remove_description));
            imageButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AttachmentView.this.remove();
                }
            });
            imageButton.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Util.announceForAccessibility(mTextView, mTextView.getText());
                    }
                }
            });

            bottomView.addView(imageButton);
        }

        bottomView.addView(mTextView);
        addView(mImageView);
        addView(bottomView);
    }

    private void configureViewForThumbnail(Bitmap bitmap, final boolean openOnClick) {
        int width = mOrientation == ImageUtils.ORIENTATION_LANDSCAPE ? mWidthLandscape : mWidthPortrait;
        int height = mOrientation == ImageUtils.ORIENTATION_LANDSCAPE ? mMaxHeightLandscape :
                mMaxHeightPortrait;

        mTextView.setMaxWidth(width);
        mTextView.setMinWidth(width);

        mImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
                .WRAP_CONTENT));
        mImageView.setAdjustViewBounds(true);
        mImageView.setMinimumWidth(width);
        mImageView.setMaxWidth(width);
        mImageView.setMaxHeight(height);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mImageView.setImageBitmap(bitmap);
        mImageView.setContentDescription(mTextView.getText());
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!openOnClick) {
                    return;
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(mAttachmentUri, "image/*");
                mContext.startActivity(intent);
            }
        });
    }

    private void configureViewForPlaceholder(final boolean openOnClick) {
        mTextView.setMaxWidth(mWidthPortrait);
        mTextView.setMinWidth(mWidthPortrait);

        mImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
                .WRAP_CONTENT));
        mImageView.setAdjustViewBounds(false);
        mImageView.setBackgroundColor(Color.parseColor("#eeeeee"));
        mImageView.setMinimumHeight((int) (mWidthPortrait * 1.2f));
        mImageView.setMinimumWidth(mWidthPortrait);
        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageView.setImageDrawable(getSystemIcon("ic_menu_attachment"));
        mImageView.setContentDescription(mTextView.getText());
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!openOnClick) {
                    return;
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(mAttachmentUri, "*/*");
                mContext.startActivity(intent);
            }
        });
    }

    private Bitmap loadImageThumbnail() {
        try {
            mOrientation = ImageUtils.determineOrientation(mContext, mAttachmentUri);
            int width = mOrientation == ImageUtils.ORIENTATION_LANDSCAPE ? mWidthLandscape : mWidthPortrait;
            int height = mOrientation == ImageUtils.ORIENTATION_LANDSCAPE ? mMaxHeightLandscape :
                    mMaxHeightPortrait;

            return ImageUtils.decodeSampledBitmap(mContext, mAttachmentUri, width, height);
        } catch (Throwable t) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private Drawable getSystemIcon(String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(getResources().getIdentifier(name, "drawable", "android")
                    , mContext.getTheme());
        } else {
            return getResources().getDrawable(getResources().getIdentifier(name, "drawable", "android"));
        }
    }
}
