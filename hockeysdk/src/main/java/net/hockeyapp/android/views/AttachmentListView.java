package net.hockeyapp.android.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import net.hockeyapp.android.utils.HockeyLog;

import java.util.ArrayList;

/**
 * <h3>Description</h3>
 *
 * A multi-row layout doing a line break when content doesn't fit into current row.
 *
 */
public class AttachmentListView extends ViewGroup {

    private static final String TAG = "AttachmentListView";

    private int mLineHeight;

    public AttachmentListView(Context context) {
        super(context);
    }

    public AttachmentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Returns a list of Uris of its AttachmentView children.
     *
     * @return ArrayList of Uri
     */
    public ArrayList<Uri> getAttachments() {
        ArrayList<Uri> attachments = new ArrayList<Uri>();

        for (int i = 0; i < getChildCount(); i++) {
            AttachmentView attachmentView = (AttachmentView) getChildAt(i);
            attachments.add(attachmentView.getAttachmentUri());
        }
        return attachments;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if ((MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED)) {
            HockeyLog.debug(TAG, "Width is unspecified");
            //throw new AssertionError();
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int count = getChildCount();
        int height = 0;
        int line_height = 0;

        int xPos = getPaddingLeft();
        int yPos = getPaddingTop();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            AttachmentView attachmentView = (AttachmentView) child;
            height = attachmentView.getEffectiveMaxHeight() + attachmentView.getPaddingTop();

            if (child.getVisibility() != GONE) {
                final LayoutParams lp = child.getLayoutParams();
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));

                final int childWidth = child.getMeasuredWidth();
                line_height = Math.max(line_height, child.getMeasuredHeight() + lp.height);

                if (xPos + childWidth > width) {
                    xPos = getPaddingLeft();
                    yPos += line_height;
                }
                xPos += childWidth + lp.width;
            }
        }
        this.mLineHeight = line_height;

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = yPos + line_height + getPaddingBottom();

        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (yPos + line_height + getPaddingBottom() < height) {
                height = yPos + line_height + getPaddingBottom();
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1); // default of 1px spacing
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return (p instanceof LayoutParams);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int width = r - l;
        int xPos = getPaddingLeft();
        int yPos = getPaddingTop();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                child.invalidate();
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                final LayoutParams lp = child.getLayoutParams();
                if (xPos + childWidth > width) {
                    xPos = getPaddingLeft();
                    yPos += mLineHeight;
                }
                child.layout(xPos, yPos, xPos + childWidth, yPos + childHeight);
                xPos += childWidth + lp.width + ((AttachmentView) child).getGap();
            }
        }
    }
}
