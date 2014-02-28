package net.hockeyapp.android.views;

import android.content.Context;
import android.net.Uri;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * A LinearLayout providing a convenience method to get all attachments as Strings.
 *
 * @author Patrick Eschenbach
 */
public class AttachmentListView extends LinearLayout {

  public AttachmentListView(Context context) {
    super(context);
  }

  public ArrayList<Uri> getAttachments() {
    ArrayList<Uri> attachments = new ArrayList<Uri>();

    for (int i = 0; i < getChildCount(); i++) {
      AttachmentView attachmentView = (AttachmentView) getChildAt(i);
      attachments.add(attachmentView.getAttachmentUri());
    }
    return attachments;
  }
}
