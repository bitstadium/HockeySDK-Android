package net.hockeyapp.android.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class ViewHelper {
  public static Drawable getGradient() {
    int colors[] = { 0xff000000, 0x00000000 };
    GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    return gradient;
  }
}
