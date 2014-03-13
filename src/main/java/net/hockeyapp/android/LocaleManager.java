package net.hockeyapp.android;

import android.content.Context;
import android.text.TextUtils;

/**
 * <h4>Description</h4>
 *
 * The LocaleManager replaces statically initialized strings with
 * the respective strings defined in the resources.
 *
 * <h4>License</h4>
 *
 * <pre>
 * Copyright (c) 2011-2013 Bit Stadium GmbH
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
 * @author Patrick Eschenbach
 */
public class LocaleManager {

  public static void initialize(Context context) {
    loadFromResources("hockeyapp_crash_dialog_title", Strings.CRASH_DIALOG_TITLE_ID, context);
    loadFromResources("hockeyapp_crash_dialog_message", Strings.CRASH_DIALOG_MESSAGE_ID, context);
    loadFromResources("hockeyapp_crash_dialog_negative_button", Strings.CRASH_DIALOG_NEGATIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_crash_dialog_positive_button", Strings.CRASH_DIALOG_POSITIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_download_failed_dialog_title", Strings.DOWNLOAD_FAILED_DIALOG_TITLE_ID, context);
    loadFromResources("hockeyapp_download_failed_dialog_message", Strings.DOWNLOAD_FAILED_DIALOG_MESSAGE_ID, context);
    loadFromResources("hockeyapp_download_failed_dialog_negative_button", Strings.DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_download_failed_dialog_positive_button", Strings.DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_update_mandatory_toast", Strings.UPDATE_MANDATORY_TOAST_ID, context);
    loadFromResources("hockeyapp_update_dialog_title", Strings.UPDATE_DIALOG_TITLE_ID, context);
    loadFromResources("hockeyapp_update_dialog_message", Strings.UPDATE_DIALOG_MESSAGE_ID, context);
    loadFromResources("hockeyapp_update_dialog_negative_button", Strings.UPDATE_DIALOG_NEGATIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_update_dialog_positive_button", Strings.UPDATE_DIALOG_POSITIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_expiry_info_title", Strings.EXPIRY_INFO_TITLE_ID, context);
    loadFromResources("hockeyapp_expiry_info_text", Strings.EXPIRY_INFO_TEXT_ID, context);
    loadFromResources("hockeyapp_feedback_failed_title", Strings.FEEDBACK_FAILED_TITLE_ID, context);
    loadFromResources("hockeyapp_feedback_failed_text", Strings.FEEDBACK_FAILED_TEXT_ID, context);
  }

  private static void loadFromResources(String name, int resourceId, Context context) {
    int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());
    if (resId == 0) {
      return;
    }

    String string = context.getString(resId);
    if (!TextUtils.isEmpty(string)) {
      Strings.set(resourceId, string);
    }
  }
}
