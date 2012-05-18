package net.hockeyapp.android;

import net.hockeyapp.android.internal.DownloadFileListener;
import net.hockeyapp.android.internal.DownloadFileTask;
import net.hockeyapp.android.internal.UpdateInfoAdapter;
import net.hockeyapp.android.internal.UpdateView;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * <h4>Description</h4>
 * 
 * Fragment to show update information and start the download
 * process if the user taps the corresponding button. 
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2012 Codenauts UG
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
   * Adapter to provide views and data for the list viw. 
   */
  private UpdateInfoAdapter adapter;
  
  /**
   * Creates a new instance of the fragment. 
   * 
   * @param versionInfo JSON array with a JSON object for each version.
   * @param urlString HockeyApp URL as a string.
   * @return Instance of Fragment
   */
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
   * @params savedInstanceState Data it most recently supplied in 
   *                            onSaveInstanceState(Bundle)
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

    adapter = new UpdateInfoAdapter(getActivity(), versionInfo.toString(), this);

    ListView listView = (ListView)view.findViewById(android.R.id.list);
    listView.setDivider(null);
    listView.setAdapter(adapter);
    
    TextView nameLabel = (TextView)view.findViewById(UpdateView.NAME_LABEL_ID);
    nameLabel.setText(getAppName());
    
    TextView versionLabel = (TextView)view.findViewById(UpdateView.VERSION_LABEL_ID);
    versionLabel.setText("Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString());

    Button updateButton = (Button)view.findViewById(UpdateView.UPDATE_BUTTON_ID);
    updateButton.setOnClickListener(this);
    
    return view;
  }

  /**
   * Called when the download button is tapped. Starts the download task and
   * disables the button to avoid multiple taps.
   */
  public void onClick(View view) {
    startDownloadTask(this.getActivity());
    dismiss();
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
      
      public String getStringForResource(int resourceID) {
        UpdateManagerListener listener = UpdateManager.getLastListener();
        if (listener != null) {
          return listener.getStringForResource(resourceID);
        }
        else {
          return null;
        }
      }
    });
    downloadTask.execute();
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
   * Creates and returns a new instance of UpdateView.
   * 
   * @return Instance of UpdateView
   */
  public View getLayoutView() {
    return new UpdateView(getActivity(), false);
  }
}
