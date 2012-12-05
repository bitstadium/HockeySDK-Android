package net.hockeyapp.android.internal;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class PullToRefreshHeader extends RelativeLayout {
	public final static int PROGRESS_BAR_ID = 0x9001;
	//public final static int PULL_TO_REFRESH_TEXT_VIEW_ID = 0x9002;
	
	private ProgressBar progressBar;
	
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
	    loadProgressBar(context);
	}

	private void loadLayoutParams(Context context) {
		/*LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setBackgroundColor(Color.WHITE);
		setLayoutParams(params);
		setGravity(Gravity.CENTER);
		setPadding(0, 10, 0, 15);*/
		
		setBackgroundColor(Color.WHITE);
		setGravity(Gravity.CENTER);
		setPadding(0, 10, 0, 15);
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
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.setMargins(marginLeft, marginTop, marginRight, 0);
		
		progressBar.setLayoutParams(params);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.GONE);
		
		addView(progressBar);
	}

	/*private void loadPullToRefreshLabel(RelativeLayout headerView, Context context) {
		TextView textView = new TextView(context);
	    textView.setId(PULL_TO_REFRESH_TEXT_VIEW_ID);
	
	    int marginSide = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)20.0, getResources().getDisplayMetrics());
	    int marginTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)10.0, getResources().getDisplayMetrics());

	    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
	  }*/
}
