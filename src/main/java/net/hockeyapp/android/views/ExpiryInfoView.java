package net.hockeyapp.android.views;

import net.hockeyapp.android.utils.ViewHelper;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * <h4>Description</h4>
 * 
 * Internal helper class to draw the content view of ExpiryInfoActivity. 
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
 * @author Thomas Dohmke
 **/
public class ExpiryInfoView extends RelativeLayout {
  public ExpiryInfoView(Context context) {
    this(context, "");
  }

  public ExpiryInfoView(Context context, String text) {
    super(context);
    
    loadLayoutParams(context);
    loadShadowView(context);
    loadTextView(context, text);
  }

  private void loadLayoutParams(Context context) {
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    setBackgroundColor(Color.WHITE);
    setLayoutParams(params);
  }

  @SuppressWarnings("deprecation")
  private void loadShadowView(Context context) {
    int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)3.0, getResources().getDisplayMetrics());
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, TRUE);

    ImageView shadowView = new ImageView(context);
    shadowView.setLayoutParams(params);
    shadowView.setBackgroundDrawable(ViewHelper.getGradient());
    
    addView(shadowView);
  }

  private void loadTextView(Context context, String text) {
    int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics());

    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT, TRUE);
    params.setMargins(margin, margin, margin, margin);
    
    TextView textView = new TextView(context);
    textView.setGravity(Gravity.CENTER);
    textView.setLayoutParams(params);
    textView.setText(text);
    textView.setTextColor(Color.BLACK);
    
    addView(textView);
  }
}
