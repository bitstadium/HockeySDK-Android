package net.hockeyapp.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.hockeyapp.android.listeners.DownloadFileListener;
import net.hockeyapp.android.tasks.DownloadFileTask;
import net.hockeyapp.android.tasks.GetFileSizeTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.VersionHelper;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * <h3>Description</h3>
 * 
 * Fragment to show update information and start the download
 * process if the user taps the corresponding button. 
 * 
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
 *
 * @author Thomas Dohmke
 **/
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UpdateFragment extends DialogFragment implements OnClickListener, UpdateInfoListener {
  /**
   * Task to download the .apk file.
   */
  private DownloadFileTask downloadTask;
  
  /**
   * JSON array with a JSON object for each version.
   */
  private JSONArray versionInfo;
  
  /**
   * HockeyApp URL as a string.
   */
  private String urlString;
  
  /**
   * Helper for version management.
   */
  private VersionHelper versionHelper;
  
  /**
   * Creates a new instance of the fragment. 
   * 
   * @param versionInfo JSON array with a JSON object for each version.
   * @param urlString HockeyApp URL as a string.
   * @return Instance of Fragment
   */
  @SuppressWarnings("unused")
  static public UpdateFragment newInstance(final JSONArray versionInfo, String urlString) {
    Bundle state = new Bundle();
    state.putString("url", urlString);
    state.putString("versionInfo", versionInfo.toString());

    UpdateFragment fragment = new UpdateFragment();
    fragment.setArguments(state);
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

    try {
      this.urlString = getArguments().getString("url");
      this.versionInfo = new JSONArray(getArguments().getString("versionInfo"));
    }
    catch (JSONException e) {
      dismiss();
      return;
    }
    
    setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
  }
  
  /**
   * Creates the root view of the fragement, set title, the version number and
   * the listener for the download button.
   * 
   * @return The fragment's root view.
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = getLayoutView();

    versionHelper = new VersionHelper(getActivity(), versionInfo.toString(), this);

    TextView nameLabel = (TextView)view.findViewById(R.id.label_title);
    nameLabel.setText(getAppName());
    
    final TextView versionLabel = (TextView)view.findViewById(R.id.label_version);
    final String versionString = "Version " + versionHelper.getVersionString();
    final String fileDate = versionHelper.getFileDateString();

    String appSizeString = "Unknown size";
    long appSize = versionHelper.getFileSizeBytes();
    if (appSize >= 0L) {
      appSizeString = String.format("%.2f", appSize / (1024.0f * 1024.0f)) + " MB";
    }
    else {
      GetFileSizeTask task = new GetFileSizeTask(getActivity(), urlString, new DownloadFileListener() {
        @Override
        public void downloadSuccessful(DownloadFileTask task) {
          if (task instanceof GetFileSizeTask) {
            long appSize = ((GetFileSizeTask)task).getSize();
            String appSizeString = String.format("%.2f", appSize / (1024.0f * 1024.0f)) + " MB";
            versionLabel.setText(versionString + "\n" + fileDate + " - " + appSizeString);
          }
        }
      });
      AsyncTaskUtils.execute(task);
    }
    versionLabel.setText(versionString + "\n" + fileDate + " - " + appSizeString);

    Button updateButton = (Button)view.findViewById(R.id.button_update);
    updateButton.setOnClickListener(this);
    
    WebView webView = (WebView)view.findViewById(R.id.web_update_details);
    webView.clearCache(true);
    webView.destroyDrawingCache();
    webView.loadDataWithBaseURL(Constants.BASE_URL, versionHelper.getReleaseNotes(false), "text/html", "utf-8", null);

    return view;
  }

  /**
   * Called when the download button is tapped. Starts the download task and
   * disables the button to avoid multiple taps.
   */
  public void onClick(View view) {
    prepareDownload();
  }

  public void prepareDownload() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      // Only if we're running on Android M or later
      if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        // We don't have the permission to write to external storage yet, so we have to request it asynchronously.
        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.UPDATE_PERMISSIONS_REQUEST);
        return;
      }
    }

    startDownloadTask(this.getActivity());
    dismiss();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (permissions.length == 0 || grantResults.length == 0) {
      // User cancelled permissions dialog -> don't do anything.
      return;
    }

    if (requestCode == Constants.UPDATE_PERMISSIONS_REQUEST) {
      // Check for the grant result on write permission
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permission granted, start download
        startDownloadTask(getActivity());
      } else {
        // Permission denied, show user alert
        Log.w(Constants.TAG, "User denied write permission, can't continue with updater task.");

        UpdateManagerListener listener = UpdateManager.getLastListener();
        if (listener != null) {
          listener.onUpdatePermissionsNotGranted();
        } else {
          final UpdateFragment updateFragment = this;
          new AlertDialog.Builder(getActivity())
                  .setTitle(getString(R.string.hockeyapp_permission_update_title))
                  .setMessage(getString(R.string.hockeyapp_permission_update_message))
                  .setNegativeButton(getString(R.string.hockeyapp_permission_dialog_negative_button), null)
                  .setPositiveButton(getString(R.string.hockeyapp_permission_dialog_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      updateFragment.prepareDownload();
                    }
                  })
                  .create()
                  .show();
        }
      }
    }
  }

  /**
   * Starts the download task and sets the listener for a successful
   * download, a failed download, and configuration strings.
   */
  private void startDownloadTask(final Activity activity) {
    downloadTask = new DownloadFileTask(activity, urlString, new DownloadFileListener() {
      public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
        if (userWantsRetry) {
          startDownloadTask(activity);
        }
      }

      public void downloadSuccessful(DownloadFileTask task) {
        // Do nothing as the fragment is already dismissed
      }
      
    });
    AsyncTaskUtils.execute(downloadTask);
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
    }
    catch (NameNotFoundException e) {
    }
    catch (NullPointerException e) {
    }
    
    return currentVersionCode;
  }

  /**
   * Returns the app's name.
   * 
   * @return The app's name as a String.
   */
  public String getAppName() {
    Activity activity = getActivity();
    
    try {
      PackageManager pm = activity.getPackageManager();
      ApplicationInfo applicationInfo = pm.getApplicationInfo(activity.getPackageName(), 0);
      return pm.getApplicationLabel(applicationInfo).toString();
    }
    catch (NameNotFoundException exception) {
      return "";
    }
  }
  
  /**
   * Creates and returns a new instance of the update view.
   * 
   * @return Update view
   */
  public View getLayoutView() {
    LinearLayout layout = new LinearLayout(getActivity());
    LayoutInflater.from(getActivity()).inflate(R.layout.fragment_update, layout);
    return layout;
  }
}
