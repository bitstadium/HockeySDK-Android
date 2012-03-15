package net.hockeyapp.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UpdateView extends RelativeLayout {
  public final static int HEADER_VIEW_ID = 0x1001;
  public final static int NAME_LABEL_ID = 0x1002;
  
  public UpdateView(Context context) {
    super(context);
    
    loadLayoutParams(context);
    loadHeaderView(context);
    loadListView(context);
  }

  private void loadLayoutParams(Context context) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    setBackgroundColor(Color.WHITE);
    setLayoutParams(params);
  }

  private void loadHeaderView(Context context) {
    RelativeLayout headerView = new RelativeLayout(context);
    headerView.setId(HEADER_VIEW_ID);
    
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    headerView.setLayoutParams(params);
    headerView.setBackgroundColor(Color.rgb(230, 236, 239));
    headerView.setPadding(0, 0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics()));
    
    loadTitleLabel(headerView, context);
    loadVersionLabel(headerView, context);
    loadUpdateButton(headerView, context);
    loadShadow(headerView, context);
    
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
  }

  private void loadUpdateButton(RelativeLayout headerView, Context context) {
  }

  private void loadShadow(RelativeLayout headerView, Context context) {
  }
  
  private void loadListView(Context context) {
    ListView listView = new ListView(context);
    listView.setId(android.R.id.list);
    
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    params.addRule(BELOW, HEADER_VIEW_ID);
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
