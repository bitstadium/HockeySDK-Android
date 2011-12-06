package net.hockeyapp.android;

import org.json.JSONArray;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class UpdateFragment extends DialogFragment {
  private JSONArray versionInfo;
  private UpdateInfoAdapter adapter;
  
  public UpdateFragment(final JSONArray versionInfo) {
    this.versionInfo = versionInfo;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
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

    return view;
  }
}
