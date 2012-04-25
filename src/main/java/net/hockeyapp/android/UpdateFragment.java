package net.hockeyapp.android;

import net.hockeyapp.android.internal.DownloadFileListener;
import net.hockeyapp.android.internal.DownloadFileTask;
import net.hockeyapp.android.internal.UpdateInfoAdapter;
import net.hockeyapp.android.internal.UpdateInfoListener;
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

public class UpdateFragment extends DialogFragment implements OnClickListener, UpdateInfoListener {
  private DownloadFileTask downloadTask;
  private JSONArray versionInfo;
  private String urlString;
  private UpdateInfoAdapter adapter;
  
  static public UpdateFragment newInstance(final JSONArray versionInfo, String urlString) {
    Bundle state = new Bundle();
    state.putString("url", urlString);
    state.putString("versionInfo", versionInfo.toString());

    UpdateFragment fragment = new UpdateFragment();
    fragment.setArguments(state);
    return fragment;
  }
  
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

  public void onClick(View v) {
    startDownloadTask(this.getActivity());
    dismiss();
  }
    
  private void startDownloadTask(final Activity activity) {
    downloadTask = new DownloadFileTask(activity, urlString, new DownloadFileListener() {
      public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
        if (userWantsRetry) {
          startDownloadTask(activity);
        }
      }

      public void downloadSuccessful(DownloadFileTask task) {
      }
    });
    downloadTask.execute();
  }
  
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

  public CharSequence getAppName() {
    Activity activity = getActivity();
    
    try {
      PackageManager pm = activity.getPackageManager();
      ApplicationInfo applicationInfo = pm.getApplicationInfo(activity.getPackageName(), 0);
      return pm.getApplicationLabel(applicationInfo);
    }
    catch (NameNotFoundException exception) {
      return "";
    }
  }
  
  public View getLayoutView() {
    return new UpdateView(getActivity(), false);
  }
}
