package net.hockeyapp.android;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;
import net.hockeyapp.android.views.PaintView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Patrick Eschenbach
 */
public class PaintActivity extends Activity {

  private static final int MENU_SAVE_ID  = Menu.FIRST;
  private static final int MENU_UNDO_ID  = Menu.FIRST + 1;
  private static final int MENU_CLEAR_ID = Menu.FIRST + 2;

  private PaintView paintView;
  private String imageName;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    /* Get image path. */
    Bundle extras = getIntent().getExtras();
    Uri imageUri = extras.getParcelable("imageUri");
    Log.e("pe", "Image URI: " + imageUri.toString());

    imageName = determineFilename(imageUri, imageUri.getLastPathSegment());

    int displayWidth = getResources().getDisplayMetrics().widthPixels;
    int displayHeight = getResources().getDisplayMetrics().heightPixels;
    int currentOrientation = displayWidth > displayHeight ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                                                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    int desiredOrientation = PaintView.determineOrientation(getContentResolver(), imageUri);
    setRequestedOrientation(desiredOrientation);

    if (currentOrientation != desiredOrientation) {
      /* Activity will be destroyed again.. skip the following expensive operations. */
      Log.d(Constants.TAG, "Image loading skipped because activity will be destroyed for orientation change.");
      return;
    }

    /* Create view and find out which orientation is needed. */
    paintView = new PaintView(this, imageUri, displayWidth, displayHeight);

    LinearLayout vLayout = new LinearLayout(this);
    LinearLayout.LayoutParams vParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    vLayout.setLayoutParams(vParams);
    vLayout.setGravity(Gravity.CENTER);
    vLayout.setOrientation(LinearLayout.VERTICAL);

    LinearLayout hLayout = new LinearLayout(this);
    LinearLayout.LayoutParams hParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    hLayout.setLayoutParams(hParams);
    hLayout.setGravity(Gravity.CENTER);
    hLayout.setOrientation(LinearLayout.HORIZONTAL);

    vLayout.addView(hLayout);
    hLayout.addView(paintView);
    setContentView(vLayout);

    Toast toast = Toast.makeText(this, "Draw something!", 1000);
    toast.show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add(0, MENU_SAVE_ID, 0, "Save");
    menu.add(0, MENU_UNDO_ID, 0, "Undo");
    menu.add(0, MENU_CLEAR_ID, 0, "Clear");

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_SAVE_ID:
        makeResult();
        return true;

      case MENU_UNDO_ID:
        paintView.undo();
        return true;

      case MENU_CLEAR_ID:
        paintView.clearImage();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void makeResult() {
    File result = new File(getCacheDir(), imageName);
    Log.e("pe", "Resulting image: " + result.getAbsolutePath());

    try {
      paintView.setDrawingCacheEnabled(true);
      Bitmap bitmap = paintView.getDrawingCache();

      FileOutputStream out = new FileOutputStream(result);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    Intent intent = new Intent();
    Uri uri = Uri.fromFile(result);
    intent.putExtra("imageUri", uri);

    if (getParent() == null) {
      setResult(Activity.RESULT_OK, intent);
    } else {
      getParent().setResult(Activity.RESULT_OK, intent);
    }
    finish();
  }

  private String determineFilename(Uri uri, String fallback) {
    String[] projection = {MediaStore.MediaColumns.DATA};
    String path = null;

    ContentResolver cr = getApplicationContext().getContentResolver();
    Cursor metaCursor = cr.query(uri, projection, null, null, null);

    if (metaCursor != null) {
      try {
        if (metaCursor.moveToFirst()) {
          path = metaCursor.getString(0);
        }
      } finally {
        metaCursor.close();
      }
    }

    return path == null ? fallback : path;
  }
}
