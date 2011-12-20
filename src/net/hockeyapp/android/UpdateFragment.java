package net.hockeyapp.android;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    View view = inflater.inflate(getLayout(), container, false);

    adapter = new UpdateInfoAdapter(this.getActivity(), versionInfo.toString(), this);

    ListView listView = (ListView)view.findViewById(R.id.list_view);
    listView.setDivider(null);
    listView.setAdapter(adapter);
    
    TextView versionLabel = (TextView)view.findViewById(R.id.version_label);
    versionLabel.setText("Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString());

    ImageButton updateButton = (ImageButton)view.findViewById(R.id.update_button);
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
  
  public int getLayout() {
    return R.layout.update_fragment;
  }
}
