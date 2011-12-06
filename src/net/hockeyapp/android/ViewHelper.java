package net.hockeyapp.android;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class ViewHelper {
  public static void moveViewBelowOrBesideHeader(Activity activity, View view, View headerView, float offset, Boolean inFragment) {
    float density = activity.getResources().getDisplayMetrics().density;
    RelativeLayout.LayoutParams layoutParams = null;
    if (inFragment) {
      layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, (int)(400 * density));
    }
    else {
      layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, activity.getWindowManager().getDefaultDisplay().getHeight() - headerView.getHeight() + (int)(offset * density));
    }
    if (((String)view.getTag()).equalsIgnoreCase("right")) {
      layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.header_view);
      layoutParams.setMargins(-(int)(offset * density), 0, 0, (int)(10 * density));
    }
    else {
      layoutParams.addRule(RelativeLayout.BELOW, R.id.header_view);
      layoutParams.setMargins(0, -(int)(offset * density), 0, (int)(10 * density));
    }
    view.setLayoutParams(layoutParams);
  }
}
