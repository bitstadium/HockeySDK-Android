/*package net.hockeyapp.android.internal;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.InputType;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

*//**
 * <h4>Description</h4>
 * 
 * Internal helper class to draw a custom {@link Button}
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2012 Codenauts UG
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
 * @author Bogdan Nistor
 **//*
public class CustomImageButton extends View {
	private final static int WIDTH_PADDING = 8;
	private final static int HEIGHT_PADDING = 10;
	private final String label;
	private final InternalListener listenerAdapter = new InternalListener();

	*//**
	 * Constructor.
	 * 
	 * @param context
	 *        Activity context in which the button view is being placed for.
	 * 
	 * @param resImage
	 *        Image to put on the button. This image should have been placed
	 *        in the drawable resources directory.
	 * 
	 * @param label
	 *        The text label to display for the custom button.
	 *//*
	public CustomImageButton(Context context, String label) {
		super(context);
		
		this.label = label;

		setFocusable(true);
		setBackgroundColor(Color.BLACK);

		setOnClickListener(listenerAdapter);
		setClickable(true);
	}

	*//**
	 * The method that is called when the focus is changed to or from this
	 * view.
	 *//*
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		if (gainFocus == true) {
			this.setBackgroundColor(Color.DKGRAY);
		} else {
			this.setBackgroundColor(Color.BLACK);
		}
	}

	*//**
	 * Method called on to render the view.
	 *//*
	protected void onDraw(Canvas canvas) {
		Paint textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		//canvas.drawBitmap(image, WIDTH_PADDING / 2, HEIGHT_PADDING / 2, null);
		//canvas.drawText(label, WIDTH_PADDING / 2, (HEIGHT_PADDING / 2) + image.getHeight() + 8, textPaint);
		canvas.drawText(label, getWidth() - label.get, y, paint)
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec),
				measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int measureSpec) {
		int preferred = image.getWidth() * 2;
		return getMeasurement(measureSpec, preferred);
	}

	private int measureHeight(int measureSpec) {
		int preferred = image.getHeight() * 2;
		return getMeasurement(measureSpec, preferred);
	}

	private int getMeasurement(int measureSpec, int preferred) {
		int specSize = MeasureSpec.getSize(measureSpec);
		int measurement = 0;

		switch(MeasureSpec.getMode(measureSpec)) {
			case MeasureSpec.EXACTLY:
				// This means the width of this view has been given.
				measurement = specSize;
				break;
			case MeasureSpec.AT_MOST:
				// Take the minimum of the preferred size and what
				// we were told to be.
				measurement = Math.min(preferred, specSize);
				break;
			default:
				measurement = preferred;
				break;
		}

		return measurement;
	}

	*//**
	 * Sets the listener object that is triggered when the view is clicked.
	 * 
	 * @param newListener
	 *        The instance of the listener to trigger.
	 *//*
	public void setOnClickListener(OnClickListener newListener) {
		listenerAdapter.setListener(newListener);
	}

	*//**
	 * Returns the label of the button.
	 *//*
	public String getLabel() {
		return label;
	}

	*//**
	 * Returns the resource id of the image.
	 *//*
	public int getImageResId() {
		return imageResId;
	}

	*//**
	 * Internal click listener class. Translates a view’s click listener to
	 * one that is more appropriate for the custom image button class.
	 * 
	 * @author Kah
	 *//*
	private class InternalListener implements View.OnClickListener {
		private OnClickListener listener = null;

		*//**
		 * Changes the listener to the given listener.
		 * 
		 * @param newListener
		 *        The listener to change to.
		 *//*
		public void setListener(OnClickListener newListener) {
			listener = newListener;
		}

		@Override
		public void onClick(View v) {
			if (listener != null) {
				listener.onClick(CustomImageButton.this);
			}
		}
	}
}*/