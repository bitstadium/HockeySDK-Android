package net.hockeyapp.android.utils;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

public class UiThreadUtil {
  /** Private constructor prevents instantiation from other classes */
  private UiThreadUtil() { 
  }

  /**
  * UiThreadUtilHolder is loaded on the first execution of UiThreadUtil.getInstance() 
  * or the first access to UiThreadUtilHolder.INSTANCE, not before.
  */
  private static class WbUtilHolder { 
    public static final UiThreadUtil INSTANCE = new UiThreadUtil();
  }

  public static UiThreadUtil getInstance() {
    return WbUtilHolder.INSTANCE;
  }
  
  public void dismissLoadingDialogAndDisplayError(WeakReference<Activity> weakActivity, final ProgressDialog progressDialog, 
      final int errorDialogId) {
    
    if (weakActivity != null) {
      final Activity activity = weakActivity.get();
      if (activity != null) {
        activity.runOnUiThread(new Runnable() {
          
          @SuppressWarnings("deprecation")
          @Override
          public void run() {
            if (progressDialog != null && progressDialog.isShowing()) {
              progressDialog.dismiss();
            }
            
            activity.showDialog(errorDialogId);
          }
        });         
      }
    }
  }
    
  public void dismissLoading(WeakReference<Activity> weakActivity, final ProgressDialog progressDialog) {
    if (weakActivity != null) {
      final Activity activity = weakActivity.get();
      if (activity != null) {
        activity.runOnUiThread(new Runnable() {
        
        @Override
        public void run() {
          if (progressDialog != null && progressDialog.isShowing()) {
              progressDialog.dismiss();
            }
        }
      });
      }
    }
  }
  
  public void displayToastMessage(WeakReference<Activity> weakActivity, final String message,
          final int flags) {
  
    if (weakActivity != null) {
      final Activity activity = weakActivity.get();
      if (activity != null) {
        activity.runOnUiThread(new Runnable() {
  
          @Override
          public void run() {
            Toast.makeText(activity, message, flags).show();
          }
        });
      }
    }
  }
}
