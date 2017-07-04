package net.hockeyapp.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.hockeyapp.android.listeners.DownloadFileListener;
import net.hockeyapp.android.tasks.DownloadFileTask;
import net.hockeyapp.android.tasks.GetFileSizeTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.PermissionsUtil;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.utils.VersionHelper;

import java.util.ArrayList;
import java.util.Locale;

/**
 * <h3>Description</h3>
 *
 * Fragment to show update information and start the download
 * process if the user taps the corresponding button.
 *
 **/
public class UpdateFragment extends DialogFragment implements OnClickListener, UpdateInfoListener {

    /**
     * The URL of the APK to offer as download
     */
    public static final String FRAGMENT_URL = "url";

    /**
     * Metadata about the update
     */
    public static final String FRAGMENT_VERSION_INFO = "versionInfo";

    /**
     * Show as dialog
     */
    public static final String FRAGMENT_DIALOG = "dialog";

    public static final String FRAGMENT_TAG = "hockey_update_dialog";

    /**
     * JSON string with info for each version.
     */
    private String mVersionInfo;

    /**
     * HockeyApp URL as a string.
     */
    private String mUrlString;

    /**
     * Creates a new instance of the fragment.
     *
     * @param versionInfo JSON string with info for each version.
     * @param urlString   HockeyApp URL as a string.
     * @return Instance of Fragment
     */
    @SuppressWarnings("unused")
    static public UpdateFragment newInstance(String versionInfo, String urlString, boolean dialog) {
        Bundle arguments = new Bundle();
        arguments.putString(FRAGMENT_URL, urlString);
        arguments.putString(FRAGMENT_VERSION_INFO, versionInfo);
        arguments.putBoolean(FRAGMENT_DIALOG, dialog);

        UpdateFragment fragment = new UpdateFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Called when the fragment is starting. Sets the instance arguments
     * and the style of the fragment.
     *
     * @param savedInstanceState Data it most recently supplied in
     *                           onSaveInstanceState(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        Bundle arguments = getArguments();
        this.mUrlString = arguments.getString(FRAGMENT_URL);
        this.mVersionInfo = arguments.getString(FRAGMENT_VERSION_INFO);
        boolean dialog = arguments.getBoolean(FRAGMENT_DIALOG);
        setShowsDialog(dialog);
    }

    /**
     * Creates the root view of the fragment, set title, the version number and
     * the listener for the download button.
     *
     * @return The fragment's root view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getLayoutView();

        // Helper for version management.
        VersionHelper versionHelper = new VersionHelper(getActivity(), mVersionInfo, this);

        TextView nameLabel = view.findViewById(R.id.label_title);
        nameLabel.setText(Util.getAppName(getActivity()));
        nameLabel.setContentDescription(nameLabel.getText());

        final TextView versionLabel = view.findViewById(R.id.label_version);
        final String versionString = "Version " + versionHelper.getVersionString();
        final String fileDate = versionHelper.getFileDateString();

        String appSizeString = "Unknown size";
        long appSize = versionHelper.getFileSizeBytes();
        if (appSize >= 0L) {
            appSizeString = String.format(Locale.US, "%.2f", appSize / (1024.0f * 1024.0f)) + " MB";
        } else {
            GetFileSizeTask task = new GetFileSizeTask(getActivity(), mUrlString, new DownloadFileListener() {
                @Override
                public void downloadSuccessful(DownloadFileTask task) {
                    if (task instanceof GetFileSizeTask) {
                        long appSize = ((GetFileSizeTask) task).getSize();
                        String appSizeString = String.format(Locale.US, "%.2f", appSize / (1024.0f * 1024.0f)) + " MB";
                        versionLabel.setText(getString(R.string.hockeyapp_update_version_details_label, versionString, fileDate, appSizeString));
                    }
                }
            });
            AsyncTaskUtils.execute(task);
        }
        versionLabel.setText(getString(R.string.hockeyapp_update_version_details_label, versionString, fileDate, appSizeString));

        Button updateButton = view.findViewById(R.id.button_update);
        updateButton.setOnClickListener(this);

        WebView webView = view.findViewById(R.id.web_update_details);
        webView.clearCache(true);
        webView.destroyDrawingCache();
        webView.loadDataWithBaseURL(Constants.BASE_URL, versionHelper.getReleaseNotes(false), "text/html", "utf-8", null);

        return view;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    /**
     * Called when the download button is tapped. Starts the download task and
     * disables the button to avoid multiple taps.
     */
    @Override
    public void onClick(View view) {
        prepareDownload();
    }

    /**
     * Returns the current version of the app.
     *
     * @return The version code as integer.
     */
    public int getCurrentVersionCode() {
        int currentVersionCode = -1;
        try {
            currentVersionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA).versionCode;
        } catch (NameNotFoundException | NullPointerException ignored) {
        }
        return currentVersionCode;
    }

    private void showError(@StringRes final int message) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.hockeyapp_dialog_error_title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.hockeyapp_dialog_positive_button, null)
                        .create();
                alertDialog.show();
            }
        });
    }

    private static String[] requiredPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return permissions.toArray(new String[0]);
    }

    protected void prepareDownload() {
        if (!Util.isConnectedToNetwork(getActivity())) {
            showError(R.string.hockeyapp_error_no_network_message);
            return;
        }

        String[] permissions = requiredPermissions();
        int[] permissionsState = PermissionsUtil.permissionsState(getActivity(), permissions);
        if (!PermissionsUtil.permissionsAreGranted(permissionsState)) {
            //showError(R.string.hockeyapp_error_no_external_storage_permission);
            return;
        }

        if (!PermissionsUtil.isUnknownSourcesEnabled(getActivity())) {
            //showError(R.string.hockeyapp_error_install_form_unknown_sources_disabled);
            return;
        }

        startDownloadTask();
    }

    /**
     * Starts the download task and sets the listener for a successful
     * download, a failed download, and configuration strings.
     */
    protected void startDownloadTask() {
        AsyncTaskUtils.execute(new DownloadFileTask(getActivity(), mUrlString, new DownloadFileListener() {
            public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
                if (userWantsRetry) {
                    startDownloadTask();
                }
            }

            public void downloadSuccessful(DownloadFileTask task) {
                // Do nothing as the fragment is already dismissed
            }
        }));
    }

    /**
     * Creates and returns a new instance of the update view.
     *
     * @return Update view
     */
    public View getLayoutView() {
        LinearLayout layout = new LinearLayout(getActivity());
        LayoutInflater.from(getActivity()).inflate(R.layout.hockeyapp_fragment_update, layout);
        return layout;
    }
}
