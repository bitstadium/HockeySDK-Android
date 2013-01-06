package net.hockeyapp.android.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * <h4>Description</h4>
 * 
 * Internal helper class to draw the content view of a Feedback message row
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2011-2013 Bit Stadium GmbH
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
 **/
public class FeedbackMessageView extends LinearLayout {
  public final static int AUTHOR_TEXT_VIEW_ID = 0x3001;
  public final static int DATE_TEXT_VIEW_ID = 0x3002;
  public final static int MESSAGE_TEXT_VIEW_ID = 0x3003;
  
  private TextView authorTextView;
  private TextView dateTextView;
  private TextView messageTextView;
  
  @SuppressWarnings("unused")
  private boolean ownMessage;

  public FeedbackMessageView(Context context) {
    this(context, true);
  }

  public FeedbackMessageView(Context context, boolean ownMessage) {
    super(context);
    
    this.ownMessage = ownMessage;
    
    loadLayoutParams(context);
    loadAuthorLabel(context);
    loadDateLabel(context);
    loadMessageLabel(context);
  }

  private void loadLayoutParams(Context context) {
    setOrientation(LinearLayout.VERTICAL);
    setGravity(Gravity.LEFT);
    setBackgroundColor(Color.LTGRAY);
  }

  private void loadAuthorLabel(Context context) {
    authorTextView = new TextView(context);
    authorTextView.setId(AUTHOR_TEXT_VIEW_ID);

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, 
        getResources().getDisplayMetrics());
    
    params.setMargins(margin, margin, margin, 0);
    authorTextView.setLayoutParams(params);
    authorTextView.setShadowLayer(1, 0, 1, Color.WHITE);
    authorTextView.setSingleLine(true);
    authorTextView.setTextColor(Color.GRAY);
    authorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    authorTextView.setTypeface(null, Typeface.NORMAL);
    
    addView(authorTextView);
  }
  
  /**
   * Sets the author name for the Author {@link TextView}
   * @param name  Author name string
   */
  public void setAuthorLabelText(String name) {
    if (authorTextView != null && name != null) {
      authorTextView.setText(name);
    }
  }
  
  private void setAuthorLaberColor(int color) {
    if (authorTextView != null) {
      authorTextView.setTextColor(color);
    }
  }
  
  private void loadDateLabel(Context context) {
    dateTextView = new TextView(context);
    dateTextView.setId(DATE_TEXT_VIEW_ID);

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, 
        getResources().getDisplayMetrics());
    
    params.setMargins(margin, 0, margin, 0);
    dateTextView.setLayoutParams(params);
    dateTextView.setShadowLayer(1, 0, 1, Color.WHITE);
    dateTextView.setSingleLine(true);
    dateTextView.setTextColor(Color.GRAY);
    dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    dateTextView.setTypeface(null, Typeface.ITALIC);
    
    addView(dateTextView);
  }
  
  /**
   * Sets the date text for the Date {@link TextView}
   * @param text  Date string
   */
  public void setDateLabelText(String text) {
    if (dateTextView != null && text != null) {
      dateTextView.setText(text);
    }
  }

  private void setDateLaberColor(int color) {
    if (dateTextView != null) {
      dateTextView.setTextColor(color);
    }
  }

  private void loadMessageLabel(Context context) {
    messageTextView = new TextView(context);
    messageTextView.setId(MESSAGE_TEXT_VIEW_ID);

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, 
        getResources().getDisplayMetrics());
    
    params.setMargins(margin, 0, margin, margin);
    messageTextView.setLayoutParams(params);
    messageTextView.setShadowLayer(1, 0, 1, Color.WHITE);
    messageTextView.setSingleLine(false);
    messageTextView.setTextColor(Color.BLACK);
    messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    messageTextView.setTypeface(null, Typeface.NORMAL);
    
    addView(messageTextView);
  }
  
  public void setMessageLabelText(String text) {
    if (messageTextView != null && text != null) {
      messageTextView.setText(text);
    }
  }

  private void setMessageLaberColor(int color) {
    if (messageTextView != null) {
      messageTextView.setTextColor(color);
    }
  }
  
  /**
   * Sets the background for the entire {@link FeedbackMessageView} and for the text colors used
   * @param decisionValue   Integer value (0 or 1). If value is 0, then the background will be dark
   *              and text color light. If the value is 1, the background will be light and the 
   *              text color dark
   */
  public void setFeedbackMessageViewBgAndTextColor(int decisionValue) {
    if (decisionValue == 0) {
      setBackgroundColor(Color.LTGRAY);
      setAuthorLaberColor(Color.WHITE);
      setDateLaberColor(Color.WHITE);
    } else if (decisionValue == 1) {
      setBackgroundColor(Color.WHITE);
      setAuthorLaberColor(Color.LTGRAY);
      setDateLaberColor(Color.LTGRAY);
    }
    
    setMessageLaberColor(Color.BLACK);
  }
}