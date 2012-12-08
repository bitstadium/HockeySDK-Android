package net.hockeyapp.android.internal;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class PullToRefreshHeader extends RelativeLayout {
  public final static int PROGRESS_BAR_ID = 0x9001;
  public final static int REL_LAYOUT_ID_ID = 0x9002;
    
  private ProgressBar progressBar;
  private RelativeLayout relLayout;
    
  public PullToRefreshHeader(Context context) {
    this(context, true);
  }

  public PullToRefreshHeader(Context context, AttributeSet attrs) {
    this(context, true, false);
  }

  public PullToRefreshHeader(Context context, boolean allowHorizontalLayout) {
    this(context, true, false);
  }

  public PullToRefreshHeader(Context context, boolean allowHorizontalLayout, boolean limitHeight) {
    super(context);
        
    loadLayoutParams(context);
    loadRelLayout(context);
    loadProgressBar(context);
  }

  private void loadLayoutParams(Context context) {
    setBackgroundColor(Color.WHITE);
    setGravity(Gravity.CENTER);
    
    android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(RelativeLayout
        .LayoutParams.MATCH_PARENT, 0);
    
    params.height = 0;
    setLayoutParams(params);
  }
    
  private void loadRelLayout(Context context) {
    relLayout = new RelativeLayout(context);
    relLayout.setId(REL_LAYOUT_ID_ID);
    
    LayoutParams params = null;
    params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT, TRUE);
    
    relLayout.setLayoutParams(params);
    
    addView(relLayout);
  }
    
  private void loadProgressBar(Context context) {
    progressBar = new ProgressBar(context);
    progressBar.setId(PROGRESS_BAR_ID);
    
    int marginLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, getResources().
            getDisplayMetrics());
    int marginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.0, getResources().
            getDisplayMetrics());
    int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10.0, getResources().
            getDisplayMetrics());
    
    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT);
    params.setMargins(marginLeft, marginTop, marginRight, 0);
    
    progressBar.setLayoutParams(params);
    progressBar.setIndeterminate(true);
    progressBar.setVisibility(View.GONE);
    
    relLayout.addView(progressBar);
  }
}