package net.hockeyapp.android;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class UpdateFragment extends DialogFragment implements OnClickListener {
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
    View view = inflater.inflate(R.layout.update_fragment, container, false);

    ViewGroup headerView = (ViewGroup)view.findViewById(R.id.header_view); 
    ListView listView = (ListView)view.findViewById(R.id.list_view);
    ViewHelper.moveViewBelowOrBesideHeader(getActivity(), listView, headerView, 23, true);

    adapter = new UpdateInfoAdapter(this.getActivity(), versionInfo.toString());
    listView.setDivider(null);
    listView.setAdapter(adapter);

    if (UpdateActivity.iconDrawableId != -1) {
      ImageView iconView = (ImageView)view.findViewById(R.id.icon_view);
      iconView.setImageDrawable(getResources().getDrawable(UpdateActivity.iconDrawableId));
    }
    
    TextView versionLabel = (TextView)view.findViewById(R.id.version_label);
    versionLabel.setText("Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString());

    ImageButton updateButton = (ImageButton)view.findViewById(R.id.update_button);
    updateButton.setOnClickListener(this);
    
    return view;
  }

  @Override
  public void onClick(View v) {
    startDownloadTask(this.getActivity());
    dismiss();
  }
    
  private void startDownloadTask(final Activity activity) {
    downloadTask = new DownloadFileTask(activity, urlString, new DownloadFileNotifier() {
      @Override
      public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
        if (userWantsRetry) {
          startDownloadTask(activity);
        }
      }
    });
    downloadTask.execute();
  }
}
