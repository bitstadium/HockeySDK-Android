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
  /**
   * Replaces all statically defined strings with the strings defined in XML resources by its specific key.
   *
   * @param context The {@link android.content.Context} instance.
   */
  public static void initialize(Context context) {
    // Crash Dialog
    loadFromResources("hockeyapp_crash_dialog_title", Strings.CRASH_DIALOG_TITLE_ID, context);
    loadFromResources("hockeyapp_crash_dialog_message", Strings.CRASH_DIALOG_MESSAGE_ID, context);
    loadFromResources("hockeyapp_crash_dialog_negative_button", Strings.CRASH_DIALOG_NEGATIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_crash_dialog_positive_button", Strings.CRASH_DIALOG_POSITIVE_BUTTON_ID, context);

    // Download Failed
    loadFromResources("hockeyapp_download_failed_dialog_title", Strings.DOWNLOAD_FAILED_DIALOG_TITLE_ID, context);
    loadFromResources("hockeyapp_download_failed_dialog_message", Strings.DOWNLOAD_FAILED_DIALOG_MESSAGE_ID, context);
    loadFromResources("hockeyapp_download_failed_dialog_negative_button", Strings.DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_download_failed_dialog_positive_button", Strings.DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID, context);

    // Update
    loadFromResources("hockeyapp_update_mandatory_toast", Strings.UPDATE_MANDATORY_TOAST_ID, context);
    loadFromResources("hockeyapp_update_dialog_title", Strings.UPDATE_DIALOG_TITLE_ID, context);
    loadFromResources("hockeyapp_update_dialog_message", Strings.UPDATE_DIALOG_MESSAGE_ID, context);
    loadFromResources("hockeyapp_update_dialog_negative_button", Strings.UPDATE_DIALOG_NEGATIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_update_dialog_positive_button", Strings.UPDATE_DIALOG_POSITIVE_BUTTON_ID, context);

    // Expiry Info
    loadFromResources("hockeyapp_expiry_info_title", Strings.EXPIRY_INFO_TITLE_ID, context);
    loadFromResources("hockeyapp_expiry_info_text", Strings.EXPIRY_INFO_TEXT_ID, context);

    // Feedback Activity
    loadFromResources("hockeyapp_feedback_failed_title", Strings.FEEDBACK_FAILED_TITLE_ID, context);
    loadFromResources("hockeyapp_feedback_failed_text", Strings.FEEDBACK_FAILED_TEXT_ID, context);
    loadFromResources("hockeyapp_feedback_name_hint", Strings.FEEDBACK_NAME_INPUT_HINT_ID, context);
    loadFromResources("hockeyapp_feedback_email_hint", Strings.FEEDBACK_EMAIL_INPUT_HINT_ID, context);
    loadFromResources("hockeyapp_feedback_subject_hint", Strings.FEEDBACK_SUBJECT_INPUT_HINT_ID, context);
    loadFromResources("hockeyapp_feedback_message_hint", Strings.FEEDBACK_MESSAGE_INPUT_HINT_ID, context);
    loadFromResources("hockeyapp_feedback_last_updated_text", Strings.FEEDBACK_LAST_UPDATED_TEXT_ID, context);
    loadFromResources("hockeyapp_feedback_attachment_button_text", Strings.FEEDBACK_ATTACHMENT_BUTTON_TEXT_ID, context);
    loadFromResources("hockeyapp_feedback_send_button_text", Strings.FEEDBACK_SEND_BUTTON_TEXT_ID, context);
    loadFromResources("hockeyapp_feedback_response_button_text", Strings.FEEDBACK_RESPONSE_BUTTON_TEXT_ID, context);
    loadFromResources("hockeyapp_feedback_refresh_button_text", Strings.FEEDBACK_REFRESH_BUTTON_TEXT_ID, context);

    // Login Activity
    loadFromResources("hockeyapp_login_headline_text", Strings.LOGIN_HEADLINE_TEXT_ID, context);
    loadFromResources("hockeyapp_login_missing_credentials_toast", Strings.LOGIN_MISSING_CREDENTIALS_TOAST_ID, context);
    loadFromResources("hockeyapp_login_email_hint", Strings.LOGIN_EMAIL_INPUT_HINT_ID, context);
    loadFromResources("hockeyapp_login_password_hint", Strings.LOGIN_PASSWORD_INPUT_HINT_ID, context);
    loadFromResources("hockeyapp_login_login_button_text", Strings.LOGIN_LOGIN_BUTTON_TEXT_ID, context);

    // Paint Activity
    loadFromResources("hockeyapp_paint_indicator_toast", Strings.PAINT_INDICATOR_TOAST_ID, context);
    loadFromResources("hockeyapp_paint_menu_save", Strings.PAINT_MENU_SAVE_ID, context);
    loadFromResources("hockeyapp_paint_menu_undo", Strings.PAINT_MENU_UNDO_ID, context);
    loadFromResources("hockeyapp_paint_menu_clear", Strings.PAINT_MENU_CLEAR_ID, context);
    loadFromResources("hockeyapp_paint_dialog_message", Strings.PAINT_DIALOG_MESSAGE_ID, context);
    loadFromResources("hockeyapp_paint_dialog_negative_button", Strings.PAINT_DIALOG_NEGATIVE_BUTTON_ID, context);
    loadFromResources("hockeyapp_paint_dialog_positive_button", Strings.PAINT_DIALOG_POSITIVE_BUTTON_ID, context);
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
