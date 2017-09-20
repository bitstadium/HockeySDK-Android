package net.hockeyapp.android.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import net.hockeyapp.android.R;
import net.hockeyapp.android.UpdateActivity;
import net.hockeyapp.android.UpdateFragment;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

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

    private WeakReference<Activity> mWeakActivity = null;
    private AlertDialog mDialog = null;
    protected boolean mIsDialogRequired = false;

    public CheckUpdateTaskWithUI(WeakReference<Activity> weakActivity, String urlString, String appIdentifier, UpdateManagerListener listener, boolean isDialogRequired) {
        super(weakActivity, urlString, appIdentifier, listener);

        this.mWeakActivity = weakActivity;
        this.mIsDialogRequired = isDialogRequired;
    }

    @Override
    public void detach() {
        super.detach();

        mWeakActivity = null;
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onPostExecute(JSONArray updateInfo) {
        super.onPostExecute(updateInfo);

        if ((updateInfo != null) && (mIsDialogRequired)) {
            showDialog(mWeakActivity.get(), updateInfo);
        }
    }

    private void showDialog(final Activity activity, final JSONArray updateInfo) {
        if ((activity == null) || (activity.isFinishing())) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cleanUp();
                    if (null != listener) {
                        listener.onCancel();
                    }
                }
            });

            builder.setPositiveButton(R.string.hockeyapp_update_dialog_positive_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean useUpdateDialog = listener != null
                            ? listener.useUpdateDialog(activity)
                            : Util.runsOnTablet(activity);
                    if (useUpdateDialog) {
                        showUpdateFragment(activity, updateInfo);
                    } else {
                        startUpdateIntent(activity, updateInfo, false);
                    }
                }
            });

            mDialog = builder.create();
            mDialog.show();
        } else {
            String appName = Util.getAppName(activity);
            String toast = activity.getString(R.string.hockeyapp_update_mandatory_toast, appName);
            Toast.makeText(activity, toast, Toast.LENGTH_LONG).show();
            startUpdateIntent(activity, updateInfo, true);
        }
    }

    private void showUpdateFragment(Activity activity, final JSONArray updateInfo) {
        if (activity != null) {
            FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            Fragment existingFragment = activity.getFragmentManager().findFragmentByTag(UpdateFragment.FRAGMENT_TAG);
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
                Method method = fragmentClass.getMethod("newInstance", String.class, String.class, boolean.class);
                DialogFragment updateFragment = (DialogFragment) method.invoke(null, updateInfo.toString(), apkUrlString, true);
                updateFragment.show(fragmentTransaction, UpdateFragment.FRAGMENT_TAG);
            } catch (Exception e) { // can't catch ReflectiveOperationException here because not targeting API level 19 or later
                HockeyLog.error("An exception happened while showing the update fragment", e);
            }
        }
    }

    private void startUpdateIntent(Activity activity, final JSONArray updateInfo, Boolean finish) {
        if (activity != null) {
            Class<? extends UpdateFragment> fragmentClass = UpdateFragment.class;
            if (listener != null) {
                fragmentClass = listener.getUpdateFragmentClass();
            }

            Intent intent = new Intent();
            intent.setClass(activity, UpdateActivity.class);
            intent.putExtra(UpdateActivity.FRAGMENT_CLASS, fragmentClass.getName());
            intent.putExtra(UpdateFragment.FRAGMENT_VERSION_INFO, updateInfo.toString());
            intent.putExtra(UpdateFragment.FRAGMENT_URL, apkUrlString);
            intent.putExtra(UpdateFragment.FRAGMENT_DIALOG, false);
            activity.startActivity(intent);

            if (finish) {
                activity.finish();
            }
        }

        cleanUp();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();
        mWeakActivity = null;
        mDialog = null;
    }
}
