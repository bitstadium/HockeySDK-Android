package net.hockeyapp.android;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UpdateView extends RelativeLayout {
  public final static int HEADER_VIEW_ID = 0x1001;
  public final static int NAME_LABEL_ID = 0x1002;
  public final static int VERSION_LABEL_ID = 0x1003;
  public final static int UPDATE_BUTTON_ID = 0x1004;
  
  private boolean layoutHorizontally = false;
  private RelativeLayout headerView = null;
  
  public UpdateView(Context context) {
    this(context, true);
  }

  public UpdateView(Context context, boolean allowHorizontalLayout) {
    super(context);

    if (allowHorizontalLayout) {
      setLayoutHorizontally(context);
    }
    else {
      layoutHorizontally = false;
    }
    loadLayoutParams(context);
    loadHeaderView(context);
    loadListView(context);
    loadShadow(headerView, context);
}

  private void setLayoutHorizontally(Context context) {
    int orientation = getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      layoutHorizontally = true;
    }
    else {
      layoutHorizontally = false;
    }
  }

  private void loadLayoutParams(Context context) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    setBackgroundColor(Color.WHITE);
    setLayoutParams(params);
  }

  private void loadHeaderView(Context context) {
    headerView = new RelativeLayout(context);
    headerView.setId(HEADER_VIEW_ID);
    
    LayoutParams params = null;
    if (layoutHorizontally) {
      params = new LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)250.0, getResources().getDisplayMetrics()), LayoutParams.FILL_PARENT);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, TRUE);
      headerView.setPadding(0, 0, 0, 0);
    }
    else {
      params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
      headerView.setPadding(0, 0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics()));
    }
    headerView.setLayoutParams(params);
    headerView.setBackgroundColor(Color.rgb(230, 236, 239));
    
    loadTitleLabel(headerView, context);
    loadVersionLabel(headerView, context);
    loadUpdateButton(headerView, context);
    
    addView(headerView);
  }

  private void loadTitleLabel(RelativeLayout headerView, Context context) {
    TextView textView = new TextView(context);
    textView.setId(NAME_LABEL_ID);

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics());
    params.setMargins(margin, margin, margin, 0);
    textView.setLayoutParams(params);
    textView.setEllipsize(TruncateAt.END);
    textView.setShadowLayer(1, 0, 1, Color.WHITE);
    textView.setSingleLine(true);
    textView.setTextColor(Color.BLACK);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    textView.setTypeface(null, Typeface.BOLD);
    
    headerView.addView(textView);
  }

  private void loadVersionLabel(RelativeLayout headerView, Context context) {
    TextView textView = new TextView(context);
    textView.setId(VERSION_LABEL_ID);

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    int marginSide = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics());
    int marginTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)10.0, getResources().getDisplayMetrics());
    params.setMargins(marginSide, marginTop, marginSide, 0);
    params.addRule(RelativeLayout.BELOW, NAME_LABEL_ID);
    textView.setLayoutParams(params);
    textView.setEllipsize(TruncateAt.END);
    textView.setShadowLayer(1, 0, 1, Color.WHITE);
    textView.setLines(2);
    textView.setLineSpacing(0.0f, 1.1f);
    textView.setTextColor(Color.BLACK);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    textView.setTypeface(null, Typeface.BOLD);
    
    headerView.addView(textView);
  }

  private void loadUpdateButton(RelativeLayout headerView, Context context) {
    Button button = new Button(context);
    button.setId(UPDATE_BUTTON_ID);

    int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics());
    int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)120.0, getResources().getDisplayMetrics());
    
    LayoutParams params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
    params.setMargins(margin, margin, margin, margin);
    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, TRUE);
    params.addRule(RelativeLayout.BELOW, VERSION_LABEL_ID);
    button.setLayoutParams(params);
    button.setBackgroundDrawable(getButtonSelector());
    button.setText("Update");
    button.setTextColor(Color.WHITE);
    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    
    headerView.addView(button);
  }

  private Drawable getButtonSelector() {
    StateListDrawable drawable = new StateListDrawable();
    drawable.addState(new int[] {-android.R.attr.state_pressed}, new ColorDrawable(Color.BLACK));
    drawable.addState(new int[] {-android.R.attr.state_pressed, android.R.attr.state_focused}, new ColorDrawable(Color.DKGRAY));
    drawable.addState(new int[] {android.R.attr.state_pressed}, new ColorDrawable(Color.GRAY));
    return drawable;
  }

  private void loadShadow(RelativeLayout headerView, Context context) {
    int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)3.0, getResources().getDisplayMetrics());
    LayoutParams params = null;
    
    ImageView topShadowView = new ImageView(context);
    if (layoutHorizontally) {
      params = new LayoutParams(1, LayoutParams.FILL_PARENT);
      params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, TRUE);
      topShadowView.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
    }
    else {
      params = new LayoutParams(LayoutParams.FILL_PARENT, height);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, TRUE);
      topShadowView.setBackgroundDrawable(getGradient());
    }
    topShadowView.setLayoutParams(params);
    
    headerView.addView(topShadowView);
    
    ImageView bottomShadowView = new ImageView(context);
    params = new LayoutParams(LayoutParams.FILL_PARENT, height);
    if (layoutHorizontally) {
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, TRUE);
    }
    else {
      params.addRule(RelativeLayout.BELOW, HEADER_VIEW_ID);
    }
    bottomShadowView.setLayoutParams(params);
    bottomShadowView.setBackgroundDrawable(getGradient());
    
    addView(bottomShadowView);
  }
  
  private Drawable getGradient() {
    int colors[] = { 0xff000000, 0x00000000 };
    GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    return gradient;
  }

  private void loadListView(Context context) {
    ListView listView = new ListView(context);
    listView.setId(android.R.id.list);
    
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    if (layoutHorizontally) {
      params.addRule(RIGHT_OF, HEADER_VIEW_ID);
    }
    else {
      params.addRule(BELOW, HEADER_VIEW_ID);
    }
    params.setMargins(0, 0, 0, 0);
    listView.setLayoutParams(params);
    listView.setBackgroundColor(Color.WHITE);
    listView.setCacheColorHint(Color.WHITE);
    listView.setFastScrollEnabled(true);
    listView.setSelector(new ColorDrawable(Color.WHITE));
    listView.setScrollingCacheEnabled(false);
    listView.setTag("bottom");
    
    addView(listView);
  }
}
