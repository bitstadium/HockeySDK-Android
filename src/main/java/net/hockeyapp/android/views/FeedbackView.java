package net.hockeyapp.android.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.InputType;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * <h4>Description</h4>
 * 
 * Internal helper class to draw the content view of FeedbackActivity
 * and FeedbackFragement. 
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
public class FeedbackView extends LinearLayout {
  public final static int LAST_UPDATED_TEXT_VIEW_ID = 0x2000;
  public final static int NAME_EDIT_TEXT_ID = 0x2002;
  public final static int EMAIL_EDIT_TEXT_ID = 0x2004;
  public final static int SUBJECT_EDIT_TEXT_ID = 0x2006;
  public final static int TEXT_EDIT_TEXT_ID = 0x2008;
  public final static int SEND_FEEDBACK_BUTTON_ID = 0x2009;
  public final static int ADD_RESPONSE_BUTTON_ID = 0x20010;
  public final static int REFRESH_BUTTON_ID = 0x20011;
  public final static int WRAPPER_BASE_ID = 0x20012;
  public final static int WRAPPER_LAYOUT_FEEDBACK_ID = 0x20013;
  public final static int WRAPPER_LAYOUT_BUTTONS_ID = 0x20014;
  public final static int WRAPPER_LAYOUT_FEEDBACK_AND_MESSAGES_ID = 0x20015;
  public final static int MESSAGES_LISTVIEW_ID = 0x20016;
  public final static int FEEDBACK_SCROLLVIEW_ID = 0x20017;
  
  /** Base wrapper {@link LinearLayout} */
  private LinearLayout wrapperBase;
  
  /** {@link ScrollView} that holds the {@link LinearLayout} with the actual feedback elements */
  private ScrollView feedbackScrollView;
  
  /** Wrapper {@link LinearLayout} for the input elements for sending feedback */
  private LinearLayout wrapperLayoutFeedback;
  
  /** Wrapper {@link LinearLayout} for last updated label, add response {@link Button} and list of discussions */
  private LinearLayout wrapperLayoutFeedbackAndMessages;
  
  /** Wrapper {@link LinearLayout} for Add a Response and Refresh {@link Button}s */
  private LinearLayout wrapperLayoutButtons;
  
  /** {@link ListView} for list of discussions */
  private ListView messagesListView;

  public FeedbackView(Context context) {
    super(context);
      
    loadLayoutParams(context);
    
    loadWrapperBase(context);
    loadFeedbackScrollView(context);
    loadWrapperLayoutFeedback(context);
    loadWrapperLayoutFeedbackAndMessages(context);
    
    loadNameInput(context);
    loadEmailInput(context);
    loadSubjectInput(context);
    loadTextInput(context);
    loadSendFeedbackButton(context);
    
    loadLastUpdatedLabel(context);
    
    loadWrapperLayoutButtons(context);
    loadAddResponseButton(context);
    loadRefreshButton(context);
    
    loadMessagesListView(context);
  }

  private void loadLayoutParams(Context context) {
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    setBackgroundColor(Color.WHITE);
    setLayoutParams(params);
  }

  private void loadWrapperBase(Context context) {
    wrapperBase = new LinearLayout(context);
    wrapperBase.setId(WRAPPER_BASE_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    params.gravity = Gravity.CENTER | Gravity.TOP;
    
    wrapperBase.setLayoutParams(params);
    wrapperBase.setPadding(0, padding, 0, padding);
    wrapperBase.setOrientation(LinearLayout.VERTICAL);
    
    addView(wrapperBase);
  }
  
  private void loadFeedbackScrollView(Context context) {
    feedbackScrollView = new ScrollView(context);
    feedbackScrollView.setId(FEEDBACK_SCROLLVIEW_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    params.gravity = Gravity.CENTER;
    
    feedbackScrollView.setLayoutParams(params);
    feedbackScrollView.setPadding(padding, padding, padding, padding);
    
    wrapperBase.addView(feedbackScrollView);
  }
  
  private void loadWrapperLayoutFeedback(Context context) {
    wrapperLayoutFeedback = new LinearLayout(context);
    wrapperLayoutFeedback.setId(WRAPPER_LAYOUT_FEEDBACK_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    params.gravity = Gravity.LEFT;
    
    wrapperLayoutFeedback.setLayoutParams(params);
    wrapperLayoutFeedback.setPadding(padding, padding, padding, padding);
    wrapperLayoutFeedback.setGravity(Gravity.TOP);
    wrapperLayoutFeedback.setOrientation(LinearLayout.VERTICAL);
    
    feedbackScrollView.addView(wrapperLayoutFeedback);
  }

  private void loadWrapperLayoutFeedbackAndMessages(Context context) {
    wrapperLayoutFeedbackAndMessages = new LinearLayout(context);
    wrapperLayoutFeedbackAndMessages.setId(WRAPPER_LAYOUT_FEEDBACK_AND_MESSAGES_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    params.gravity = Gravity.CENTER;
    
    wrapperLayoutFeedbackAndMessages.setLayoutParams(params);
    wrapperLayoutFeedbackAndMessages.setPadding(padding, padding, padding, padding);
    wrapperLayoutFeedbackAndMessages.setGravity(Gravity.TOP);
    wrapperLayoutFeedbackAndMessages.setOrientation(LinearLayout.VERTICAL);
    
    wrapperBase.addView(wrapperLayoutFeedbackAndMessages);
  }

  private void loadWrapperLayoutButtons(Context context) {
    wrapperLayoutButtons = new LinearLayout(context);
    wrapperLayoutButtons.setId(WRAPPER_LAYOUT_BUTTONS_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    params.gravity = Gravity.LEFT;
    
    wrapperLayoutButtons.setLayoutParams(params);
    wrapperLayoutButtons.setPadding(0, padding, 0, padding);
    wrapperLayoutButtons.setGravity(Gravity.TOP);
    wrapperLayoutButtons.setOrientation(LinearLayout.HORIZONTAL);
    
    wrapperLayoutFeedbackAndMessages.addView(wrapperLayoutButtons);
  }
  
  private void loadMessagesListView(Context context) {
    messagesListView = new ListView(context);
    messagesListView.setId(MESSAGES_LISTVIEW_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    
    messagesListView.setLayoutParams(params);
    messagesListView.setPadding(0, padding, 0, padding);
    
    wrapperLayoutFeedbackAndMessages.addView(messagesListView);
  }
  
  private void loadNameInput(Context context) {
    EditText editText = new EditText(context);
    editText.setId(NAME_EDIT_TEXT_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);

    editText.setLayoutParams(params);
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    editText.setSingleLine(true);
    editText.setTextColor(Color.GRAY);
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    editText.setTypeface(null, Typeface.NORMAL);
    editText.setHint("Name");
    editText.setHintTextColor(Color.LTGRAY);
    setEditTextBackground(context, editText);
    
    wrapperLayoutFeedback.addView(editText);
  }

  private void loadEmailInput(Context context) {
    EditText editText = new EditText(context);
    editText.setId(EMAIL_EDIT_TEXT_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);

    editText.setLayoutParams(params);
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    editText.setSingleLine(true);
    editText.setTextColor(Color.GRAY);
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    editText.setTypeface(null, Typeface.NORMAL);
    editText.setHint("Email");
    editText.setHintTextColor(Color.LTGRAY);
    setEditTextBackground(context, editText);
    
    wrapperLayoutFeedback.addView(editText);
  }

  private void loadSubjectInput(Context context) {
    EditText editText = new EditText(context);
    editText.setId(SUBJECT_EDIT_TEXT_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);

    editText.setLayoutParams(params);
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    editText.setSingleLine(true);
    editText.setTextColor(Color.GRAY);
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    editText.setTypeface(null, Typeface.NORMAL);
    editText.setHint("Subject");
    editText.setHintTextColor(Color.LTGRAY);
    setEditTextBackground(context, editText);
    
    wrapperLayoutFeedback.addView(editText);
  }

  private void loadTextInput(Context context) {
    EditText editText = new EditText(context);
    editText.setId(TEXT_EDIT_TEXT_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, getResources().getDisplayMetrics());
    int minEditTextHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 100.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);
    
    editText.setLayoutParams(params);
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    editText.setSingleLine(false);
    editText.setTextColor(Color.GRAY);
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    editText.setTypeface(null, Typeface.NORMAL);
    editText.setMinimumHeight(minEditTextHeight);
    editText.setHint("Message");
    editText.setHintTextColor(Color.LTGRAY);
    setEditTextBackground(context, editText);
    
    wrapperLayoutFeedback.addView(editText);
  }

  private void loadLastUpdatedLabel(Context context) {
    TextView textView = new TextView(context);
    textView.setId(LAST_UPDATED_TEXT_VIEW_ID);
  
    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, 0);
    
    textView.setLayoutParams(params);
    textView.setPadding(0, margin, 0, margin);
    textView.setEllipsize(TruncateAt.END);
    textView.setShadowLayer(1, 0, 1, Color.WHITE);
    textView.setSingleLine(true);
    textView.setText("Last Updated: ");
    textView.setTextColor(Color.GRAY);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    textView.setTypeface(null, Typeface.NORMAL);
    
    wrapperLayoutFeedbackAndMessages.addView(textView);
  }
  
  @SuppressWarnings("deprecation")
  private void loadSendFeedbackButton(Context context) {
    Button button = new Button(context);
    button.setId(SEND_FEEDBACK_BUTTON_ID);
  
    int paddingTopBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    int paddingLeftRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 30.0, getResources().getDisplayMetrics());
    int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    
    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    
    params.setMargins(0, 0, 0, margin);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    
    button.setLayoutParams(params);
    button.setBackgroundDrawable(getButtonSelector());
    button.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
    button.setText("Send feedback");
    button.setTextColor(Color.WHITE);
    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    
    wrapperLayoutFeedback.addView(button);
  }

  @SuppressWarnings("deprecation")
  private void loadAddResponseButton(Context context) {
    Button button = new Button(context);
    button.setId(ADD_RESPONSE_BUTTON_ID);
  
    int paddingTopBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    int marginRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 5.0, getResources().getDisplayMetrics());
    
    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    
    params.setMargins(0, 0, marginRight, margin);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    params.weight = 1.0f;
    
    button.setLayoutParams(params);
    button.setBackgroundDrawable(getButtonSelector());
    button.setPadding(0, paddingTopBottom, 0, paddingTopBottom);
    button.setGravity(Gravity.CENTER);
    button.setText("Add a Response");
    button.setTextColor(Color.WHITE);
    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    
    wrapperLayoutButtons.addView(button);
  }
  
  @SuppressWarnings("deprecation")
  private void loadRefreshButton(Context context) {
    Button button = new Button(context);
    button.setId(REFRESH_BUTTON_ID);
  
    int paddingTopBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().getDisplayMetrics());
    int marginLeft = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 5.0, getResources().getDisplayMetrics());
    
    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    
    params.setMargins(marginLeft, 0, 0, margin);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    
    button.setLayoutParams(params);
    button.setBackgroundDrawable(getButtonSelector());
    button.setPadding(0, paddingTopBottom, 0, paddingTopBottom);
    button.setGravity(Gravity.CENTER);
    button.setText("Refresh");
    button.setTextColor(Color.WHITE);
    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    params.weight = 1.0f;
    
    wrapperLayoutButtons.addView(button);
  }

  private Drawable getButtonSelector() {
    StateListDrawable drawable = new StateListDrawable();
    drawable.addState(new int[] {-android.R.attr.state_pressed}, new ColorDrawable(Color.BLACK));
    drawable.addState(new int[] {-android.R.attr.state_pressed, android.R.attr.state_focused}, new ColorDrawable(Color.DKGRAY));
    drawable.addState(new int[] {android.R.attr.state_pressed}, new ColorDrawable(Color.GRAY));
    return drawable;
  }

  @SuppressWarnings("deprecation")
  private void setEditTextBackground(Context context, EditText editText) {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
      // On devices > 3.0, we set a background drawable
      editText.setBackgroundDrawable(getEditTextBackground(context));
    }
    else {
      // ON devices >= 3.0, we just use the default style
    }
  }
 
  private Drawable getEditTextBackground(Context context) {
    int outerPadding = (int)(context.getResources().getDisplayMetrics().density * 10);
    ShapeDrawable outerShape = new ShapeDrawable(new RectShape());
    Paint outerPaint = outerShape.getPaint();
    outerPaint.setColor(Color.WHITE);
    outerPaint.setStyle(Style.FILL_AND_STROKE);
    outerPaint.setStrokeWidth(1.0f);
    outerShape.setPadding(outerPadding, outerPadding, outerPadding, outerPadding); 

    int innerPadding = (int)(context.getResources().getDisplayMetrics().density * 1.5);
    ShapeDrawable innerShape = new ShapeDrawable(new RectShape());
    Paint innerPaint = innerShape.getPaint();
    innerPaint.setColor(Color.DKGRAY);
    innerPaint.setStyle(Style.FILL_AND_STROKE);
    innerPaint.setStrokeWidth(1.0f);
    innerShape.setPadding(0, 0, 0, innerPadding);

    return new LayerDrawable(new Drawable[] { innerShape, outerShape });
  }
}