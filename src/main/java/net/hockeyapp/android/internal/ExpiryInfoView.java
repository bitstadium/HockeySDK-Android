package net.hockeyapp.android.internal;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    setBackgroundColor(Color.WHITE);
    setLayoutParams(params);
  }

  private void loadShadowView(Context context) {
    int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)3.0, getResources().getDisplayMetrics());
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, height);
    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, TRUE);

    ImageView shadowView = new ImageView(context);
    shadowView.setLayoutParams(params);
    shadowView.setBackgroundDrawable(ViewHelper.getGradient());
    
    addView(shadowView);
  }

  private void loadTextView(Context context, String text) {
    int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics());

    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
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
