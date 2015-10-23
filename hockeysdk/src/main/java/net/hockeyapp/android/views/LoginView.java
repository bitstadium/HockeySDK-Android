package net.hockeyapp.android.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.RectShape;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.hockeyapp.android.R;

/**
 * <h3>Description</h3>
 *
 * Internal helper class to draw the content view of the LoginActivity.
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
 **/
public class LoginView extends LinearLayout {
  public final static int WRAPPER_BASE_ID   = 0x3001;
  public final static int HEADLINE_TEXT_ID  = 0x3002;
  public final static int EMAIL_INPUT_ID    = 0x3003;
  public final static int PASSWORD_INPUT_ID = 0x3004;
  public final static int LOGIN_BUTTON_ID   = 0x3005;

  /** Base wrapper {@link LinearLayout} */
  private LinearLayout wrapperBase;

  public LoginView(Context context) {
    this(context, 0);
  }

  public LoginView(Context context, int mode) {
    super(context);

    loadLayoutParams(context);

    loadWrapperBase(context);
    loadHeadlineTextView(context);
    loadEmailInput(context);
    loadPasswordInput(context);
    loadLoginButton(context);
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
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, getResources().getDisplayMetrics());
    params.gravity = Gravity.CENTER | Gravity.TOP;

    wrapperBase.setLayoutParams(params);
    wrapperBase.setPadding(padding, padding, padding, padding);
    wrapperBase.setOrientation(LinearLayout.VERTICAL);

    addView(wrapperBase);
  }

  private void loadHeadlineTextView(Context context) {
    TextView textView = new TextView(context);
    textView.setId(HEADLINE_TEXT_ID);

    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 30.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);

    textView.setLayoutParams(params);
    textView.setText(R.string.hockeyapp_login_headline_text);
    textView.setTextColor(Color.GRAY);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    textView.setTypeface(null, Typeface.NORMAL);

    wrapperBase.addView(textView);
  }

  private void loadEmailInput(Context context) {
    EditText editText = new EditText(context);
    editText.setId(EMAIL_INPUT_ID);

    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 30.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);

    editText.setLayoutParams(params);
    editText.setHint(R.string.hockeyapp_login_email_hint);
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    editText.setTextColor(Color.GRAY);
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    editText.setTypeface(null, Typeface.NORMAL);
    editText.setHintTextColor(Color.LTGRAY);
    setEditTextBackground(context, editText);

    wrapperBase.addView(editText);
  }

  private void loadPasswordInput(Context context) {
    EditText editText = new EditText(context);
    editText.setId(PASSWORD_INPUT_ID);

    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 30.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);

    editText.setLayoutParams(params);
    editText.setHint(R.string.hockeyapp_login_password_hint);
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    editText.setTextColor(Color.GRAY);
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    editText.setTypeface(null, Typeface.NORMAL);
    editText.setHintTextColor(Color.LTGRAY);
    setEditTextBackground(context, editText);

    wrapperBase.addView(editText);
  }

  @SuppressWarnings("deprecation")
  private void loadLoginButton(Context context) {
    Button button = new Button(context);
    button.setId(LOGIN_BUTTON_ID);

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 30.0, getResources().getDisplayMetrics());
    params.setMargins(0, 0, 0, margin);

    button.setLayoutParams(params);
    button.setBackgroundDrawable(getButtonSelector());
    button.setText(R.string.hockeyapp_login_login_button_text);
    button.setTextColor(Color.WHITE);
    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

    wrapperBase.addView(button);
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
    outerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    outerPaint.setStrokeWidth(1.0f);
    outerShape.setPadding(outerPadding, outerPadding, outerPadding, outerPadding);

    int innerPadding = (int)(context.getResources().getDisplayMetrics().density * 1.5);
    ShapeDrawable innerShape = new ShapeDrawable(new RectShape());
    Paint innerPaint = innerShape.getPaint();
    innerPaint.setColor(Color.DKGRAY);
    innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    innerPaint.setStrokeWidth(1.0f);
    innerShape.setPadding(0, 0, 0, innerPadding);

    return new LayerDrawable(new Drawable[] { innerShape, outerShape });
  }
}
