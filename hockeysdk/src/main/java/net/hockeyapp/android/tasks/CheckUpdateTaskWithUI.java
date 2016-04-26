package net.hockeyapp.android.tasks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import net.hockeyapp.android.R;
import net.hockeyapp.android.UpdateActivity;
import net.hockeyapp.android.UpdateFragment;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.utils.VersionCache;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * <h3>Description</h3>
 *
 * Internal helper class. Checks if a new update is available by
 * fetching version data from Hockeyapp.
 *
 **/
public class CheckUpdateTaskWithUI extends CheckUpdateTask {

    private Activity mActivity = null;
    private AlertDialog mDialog = null;
    protected boolean mIsDialogRequired = false;

    public CheckUpdateTaskWithUI(WeakReference<Activity> weakActivity, String urlString, String appIdentifier, UpdateManagerListener listener, boolean isDialogRequired) {
        super(weakActivity, urlString, appIdentifier, listener);

        if (weakActivity != null) {
            mActivity = weakActivity.get();
        }

        this.mIsDialogRequired = isDialogRequired;
    }

    @Override
    public void detach() {
        super.detach();

        mActivity = null;

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onPostExecute(JSONArray updateInfo) {
        super.onPostExecute(updateInfo);

        if ((updateInfo != null) && (mIsDialogRequired)) {
            showDialog(updateInfo);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showDialog(final JSONArray updateInfo) {

        //Reason for enabled Caching
        //we want to prevent users from being able to weasle around mandatory updates by going offline.
        if (getCachingEnabled()) {
            HockeyLog.verbose("HockeyUpdate", "Caching is enabled. Setting version to cached one.");
            VersionCache.setVersionInfo(mActivity, updateInfo.toString());
        }

        if ((mActivity == null) || (mActivity.isFinishing())) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.hockeyapp_update_dialog_title);

        if (!mandatory) {
            builder.setMessage(R.string.hockeyapp_update_dialog_message);
            builder.setNegativeButton(R.string.hockeyapp_update_dialog_negative_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    cleanUp();
                    if (null != listener) {
                        listener.onCancel();
                    }
                }
            });

            builder.setPositiveButton(R.string.hockeyapp_update_dialog_positive_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (getCachingEnabled()) {
                        VersionCache.setVersionInfo(mActivity, "[]");
                    }

                    WeakReference<Activity> weakActivity = new WeakReference<Activity>(mActivity);
                    if ((Util.fragmentsSupported()) && (Util.runsOnTablet(weakActivity))) {
                        showUpdateFragment(updateInfo);
                    } else {
                        startUpdateIntent(updateInfo, false);
                    }
                }
            });

            mDialog = builder.create();
            mDialog.show();
        } else {
            String appName = Util.getAppName(mActivity);
            String toast = String.format(mActivity.getString(R.string.hockeyapp_update_mandatory_toast),
                    appName);
            Toast.makeText(mActivity, toast, Toast.LENGTH_LONG).show();
            startUpdateIntent(updateInfo, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showUpdateFragment(final JSONArray updateInfo) {
        if (mActivity != null) {
            FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            Fragment existingFragment = mActivity.getFragmentManager().findFragmentByTag("hockey_update_dialog");
            if (existingFragment != null) {
                fragmentTransaction.remove(existingFragment);
            }
            fragmentTransaction.addToBackStack(null);

            // Create and show the dialog
            Class<? extends UpdateFragment> fragmentClass = UpdateFragment.class;
            if (listener != null) {
                fragmentClass = listener.getUpdateFragmentClass();
            }

            try {
                Method method = fragmentClass.getMethod("newInstance", JSONArray.class, String.class);
                DialogFragment updateFragment = (DialogFragment) method.invoke(null, updateInfo, getURLString("apk"));
                updateFragment.show(fragmentTransaction, "hockey_update_dialog");
            } catch (Exception e) { // can't catch ReflectiveOperationException here because not targeting API level 19 or later
                HockeyLog.error("An exception happened while showing the update fragment:");
                e.printStackTrace();
                HockeyLog.error("Showing update activity instead.");
                startUpdateIntent(updateInfo, false);
            }
        }
    }

    private void startUpdateIntent(final JSONArray updateInfo, Boolean finish) {
        Class<?> activityClass = null;
        if (listener != null) {
            activityClass = listener.getUpdateActivityClass();
        }
        if (activityClass == null) {
            activityClass = UpdateActivity.class;
        }

        if (mActivity != null) {
            Intent intent = new Intent();
            intent.setClass(mActivity, activityClass);
            intent.putExtra(UpdateActivity.EXTRA_JSON, updateInfo.toString());
            intent.putExtra(UpdateActivity.EXTRA_URL, getURLString(APK));
            mActivity.startActivity(intent);

            if (finish) {
                mActivity.finish();
            }
        }

        cleanUp();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();
        mActivity = null;
        mDialog = null;
    }
}
