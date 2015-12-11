package net.hockeyapp.android.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
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
 */
public class UiThreadUtil {
    /**
     * Private constructor prevents instantiation from other classes
     */
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

    public void dismissLoadingDialogAndDisplayError(WeakReference<Activity> weakActivity, final ProgressDialog progressDialog, final int errorDialogId) {

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

    public void displayToastMessage(WeakReference<Activity> weakActivity, final String message, final int flags) {
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
