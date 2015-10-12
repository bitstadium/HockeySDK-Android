package net.hockeyapp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import net.hockeyapp.android.adapters.MessagesAdapter;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.objects.FeedbackUserDataElement;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.views.AttachmentListView;
import net.hockeyapp.android.views.AttachmentView;
import net.hockeyapp.android.views.FeedbackView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <h3>Description</h3>
 *
 * Activity to show the feedback form.
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
 * @author Bogdan Nistor
 * @author Patrick Eschenbach
 * @author Thomas Dohmke
 **/
public class FeedbackActivity extends Activity implements FeedbackActivityInterface, OnClickListener {
  /** Number of attachments allowed per message. **/
  private static final int MAX_ATTACHMENTS_PER_MSG = 3;

  /** ID of error dialog **/
  private final int DIALOG_ERROR_ID = 0;
  /** Activity request constants for ContextMenu and Chooser Intent */
  private final int ATTACH_PICTURE = 1;
  private final int ATTACH_FILE = 2;
  private final int PAINT_IMAGE = 3;
  /** Reference to this **/
  private Context context;
  /** Widgets and layout **/
  private TextView lastUpdatedTextView;
  private EditText nameInput;
  private EditText emailInput;
  private EditText subjectInput;
  private EditText textInput;
  private Button sendFeedbackButton;
  private Button addAttachmentButton;
  private Button addResponseButton;
  private Button refreshButton;
  private ScrollView feedbackScrollView;
  private LinearLayout wrapperLayoutFeedbackAndMessages;
  private ListView messagesListView;
  /** Send feedback {@link AsyncTask} */
  private SendFeedbackTask sendFeedbackTask;
  private Handler feedbackHandler;

  /** Parse feedback {@link AsyncTask} */
  private ParseFeedbackTask parseFeedbackTask;
  private Handler parseFeedbackHandler;

  /** Initial attachment uris */
  private List<Uri> initialAttachments;

    /** URL for HockeyApp API **/
  private String url;

  /** Current error for alert dialog **/
  private ErrorObject error;

  /** Message data source **/
  private MessagesAdapter messagesAdapter;
  private ArrayList<FeedbackMessage> feedbackMessages;

  /** True when a message is posted **/
  private boolean inSendFeedback;

  /** True when the view was initialized **/
  private boolean feedbackViewInitialized;

  /** Unique token of the message feed **/
  private String token;

  /**
   * Enables/Disables the Send Feedback button.
   *
   * @param isEnable the button is enabled if true
   */
  public void enableDisableSendFeedbackButton(boolean isEnable) {
  	if (sendFeedbackButton != null) {
  		sendFeedbackButton.setEnabled(isEnable);
  	}
  }

  /**
   * Creates and returns a new instance of {@link FeedbackView}
   *
   * @return Instance of {@link FeedbackView}
   */
  public ViewGroup getLayoutView() {
    return new FeedbackView(this);
  }

  /**
   * Called when the Send Feedback {@link Button} is tapped. Sends the feedback and disables
   * the button to avoid multiple taps.
   */
  @Override
  public void onClick(View v) {
  	switch (v.getId()) {
  	  case FeedbackView.SEND_FEEDBACK_BUTTON_ID:
  	    sendFeedback();
  	    break;

      case FeedbackView.ADD_ATTACHMENT_BUTTON_ID:
        ViewGroup attachments = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
        if (attachments.getChildCount() >= MAX_ATTACHMENTS_PER_MSG) {
          Toast.makeText(this, String.format("", MAX_ATTACHMENTS_PER_MSG), 1000).show();
        } else {
          openContextMenu(v);
        }
        break;

  	  case FeedbackView.ADD_RESPONSE_BUTTON_ID:
  	    configureFeedbackView(false);
  	    inSendFeedback = true;
  	    break;

  	  case FeedbackView.REFRESH_BUTTON_ID:
  	    sendFetchFeedback(url, null, null, null, null, null, PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context), feedbackHandler, true);
  	    break;

  	  default:
  	    break;
  	}
  }

  /**
   * Called when user clicked on context menu item.
   */
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case ATTACH_FILE:
      case ATTACH_PICTURE:
        return addAttachment(item.getItemId());

      default:
        return super.onContextItemSelected(item);
    }
  }

  /**
   * Called when the activity is starting. Sets the title and content view
   *
   * @param savedInstanceState Data it most recently supplied in
   *                           onSaveInstanceState(Bundle)
   */
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(getLayoutView());

    setTitle(getString(R.string.hockeyapp_feedback_title));
    context = this;

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      url = extras.getString("url");

      Parcelable[] initialAttachmentsArray = extras.getParcelableArray("initialAttachments");
      if (initialAttachmentsArray != null) {
        initialAttachments = new ArrayList<Uri>();
          for (Parcelable parcelable : initialAttachmentsArray) {
              initialAttachments.add((Uri) parcelable);
          }
      }
    }

    if (savedInstanceState != null) {
      feedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
      inSendFeedback = savedInstanceState.getBoolean("inSendFeedback");
    }
    else {
      inSendFeedback = false;
      feedbackViewInitialized = false;
    }

    // Cancel notification
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(ParseFeedbackTask.NEW_ANSWER_NOTIFICATION_ID);

    initFeedbackHandler();
    initParseFeedbackHandler();
    configureAppropriateView();
  }

  /**
   * Called when context menu is needed (on add attachment button).
   */
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    menu.add(0, ATTACH_FILE, 0, getString(R.string.hockeyapp_feedback_attach_file));
    menu.add(0, ATTACH_PICTURE, 0, getString(R.string.hockeyapp_feedback_attach_picture));
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (sendFeedbackTask != null) {
      sendFeedbackTask.detach();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)  {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (inSendFeedback) {
        inSendFeedback = false;
        configureAppropriateView();
      } else {
        finish();
      }

      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * Detaches the activity from the send feedback task and returns the task
   * as last instance. This way the task is restored when the activity
   * is immediately re-created.
   *
   * @return The download task if present.
   */
  @Override
  public Object onRetainNonConfigurationInstance() {
  	if (sendFeedbackTask != null) {
  		sendFeedbackTask.detach();
  	}

  	return sendFeedbackTask;
  }

  /**
   * Configures the content view by initializing the input {@link EditText}s
   * and the listener for the Send Feedback {@link Button}
   *
   * @param haveToken the message list is shown if true
   */
  protected void configureFeedbackView(boolean haveToken) {
    feedbackScrollView = (ScrollView) findViewById(FeedbackView.FEEDBACK_SCROLLVIEW_ID);
    wrapperLayoutFeedbackAndMessages = (LinearLayout) findViewById(FeedbackView.WRAPPER_LAYOUT_FEEDBACK_AND_MESSAGES_ID);
    messagesListView = (ListView) findViewById(FeedbackView.MESSAGES_LISTVIEW_ID);

    if (haveToken) {
      /** If a token exists, the list of messages should be displayed */
      wrapperLayoutFeedbackAndMessages.setVisibility(View.VISIBLE);
      feedbackScrollView.setVisibility(View.GONE);

      lastUpdatedTextView = (TextView) findViewById(FeedbackView.LAST_UPDATED_TEXT_VIEW_ID);

      addResponseButton = (Button) findViewById(FeedbackView.ADD_RESPONSE_BUTTON_ID);
      addResponseButton.setOnClickListener(this);

      refreshButton = (Button) findViewById(FeedbackView.REFRESH_BUTTON_ID);
      refreshButton.setOnClickListener(this);
    }
    else {
      /** if the token doesn't exist, the feedback details inputs to be sent need to be displayed */
      wrapperLayoutFeedbackAndMessages.setVisibility(View.GONE);
      feedbackScrollView.setVisibility(View.VISIBLE);

      nameInput = (EditText)findViewById(FeedbackView.NAME_EDIT_TEXT_ID);
      emailInput = (EditText)findViewById(FeedbackView.EMAIL_EDIT_TEXT_ID);
      subjectInput = (EditText)findViewById(FeedbackView.SUBJECT_EDIT_TEXT_ID);
      textInput = (EditText)findViewById(FeedbackView.TEXT_EDIT_TEXT_ID);

      /** Check to see if the Name and Email are saved in {@link SharedPreferences} */
      if (!feedbackViewInitialized) {
        String nameEmailSubject = PrefsUtil.getInstance().getNameEmailFromPrefs(context);
        if (nameEmailSubject != null) {
          /** We have Name and Email. Prepopulate the appropriate fields */
          String[] nameEmailSubjectArray = nameEmailSubject.split("\\|");
          if (nameEmailSubjectArray != null && nameEmailSubjectArray.length >= 2) {
            nameInput.setText(nameEmailSubjectArray[0]);
            emailInput.setText(nameEmailSubjectArray[1]);

            if (nameEmailSubjectArray.length >= 3) {
              subjectInput.setText(nameEmailSubjectArray[2]);
              textInput.requestFocus();
            }
            else {
              subjectInput.requestFocus();
            }
          }
        }
        else {
          /** We dont have Name and Email. Reset those fields */
          nameInput.setText("");
          emailInput.setText("");
          subjectInput.setText("");
          nameInput.requestFocus();
        }

        feedbackViewInitialized = true;
      }

      /** Reset the remaining fields if previously populated */
      textInput.setText("");

      /** Check to see if the Feedback Token is availabe */
      if (PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context) != null) {
        /** If Feedback Token is available, hide the Subject Input field */
        subjectInput.setVisibility(View.GONE);
      }
      else {
        /** If Feedback Token is not available, display the Subject Input field */
        subjectInput.setVisibility(View.VISIBLE);
      }

      /** Reset the attachment list */
      ViewGroup attachmentListView = (ViewGroup)findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
      attachmentListView.removeAllViews();

      if (initialAttachments != null) {
        for (Uri attachmentUri : initialAttachments) {
            attachmentListView.addView(new AttachmentView(this, attachmentListView, attachmentUri, true));
        }
      }

      /** Use of context menu needs to be enabled explicitly */
      addAttachmentButton = (Button)findViewById(FeedbackView.ADD_ATTACHMENT_BUTTON_ID);
      addAttachmentButton.setOnClickListener(this);
      registerForContextMenu(addAttachmentButton);

      sendFeedbackButton = (Button)findViewById(FeedbackView.SEND_FEEDBACK_BUTTON_ID);
      sendFeedbackButton.setOnClickListener(this);
  	}
  }

 /**
  * Called when the request for sending the feedback has finished.
  *
  * @param success is true if the sending of the feedback was successful
  */
  protected void onSendFeedbackResult(final boolean success) {}

  /**
   * Called when picture or file was chosen.
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != RESULT_OK) {
      return;
    }

    if (requestCode == ATTACH_FILE) {
      /** User picked file */
      Uri uri = data.getData();

      if (uri != null) {
        final ViewGroup attachments = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
        attachments.addView(new AttachmentView(this, attachments, uri, true));
      }

    } else if (requestCode == ATTACH_PICTURE) {
      /** User picked image */
      Uri uri = data.getData();

      /** Start PaintActivity */
      if (uri != null) {
        try {
          Intent intent = new Intent(this, PaintActivity.class);
          intent.putExtra("imageUri", uri);
          startActivityForResult(intent, PAINT_IMAGE);
        } catch (ActivityNotFoundException e) {
          Log.e(Util.LOG_IDENTIFIER, "Paint activity not declared!", e);
        }

      }

    } else if (requestCode == PAINT_IMAGE) {
      /** Final attachment picture received and ready to be added to list. */
      Uri uri = data.getParcelableExtra("imageUri");

      if (uri != null) {
        final ViewGroup attachments = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
        attachments.addView(new AttachmentView(this, attachments, uri, true));
      }

    } else return;
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch(id) {
      case DIALOG_ERROR_ID:
        return new AlertDialog.Builder(this)
          .setMessage(getString(R.string.hockeyapp_dialog_error_message))
          .setCancelable(false)
          .setTitle(getString(R.string.hockeyapp_dialog_error_title))
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setPositiveButton(getString(R.string.hockeyapp_dialog_positive_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              error = null;
              dialog.cancel();
            }
          }).create();
    }

    return null;
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    switch(id) {
    case DIALOG_ERROR_ID:
      AlertDialog messageDialogError = (AlertDialog) dialog;
      if (error != null) {
        /** If the ErrorObject is not null, display the ErrorObject message */
        messageDialogError.setMessage(error.getMessage());
      } else {
        /** If the ErrorObject is null, display the general error message */
        messageDialogError.setMessage(getString(R.string.hockeyapp_feedback_generic_error));
      }

      break;
    }
  }

  /**
   * Restore all attachments.
   */
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      ViewGroup attachmentList = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
      ArrayList<Uri> attachmentsUris = savedInstanceState.getParcelableArrayList("attachments");
      for (Uri attachmentUri : attachmentsUris) {
        attachmentList.addView(new AttachmentView(this, attachmentList, attachmentUri, true));
      }

      feedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
    }

    super.onRestoreInstanceState(savedInstanceState);
  }

  /**
   * Save all attachments.
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    AttachmentListView attachmentListView = (AttachmentListView) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);

    outState.putParcelableArrayList("attachments", attachmentListView.getAttachments());
    outState.putBoolean("feedbackViewInitialized", feedbackViewInitialized);
    outState.putBoolean("inSendFeedback", inSendFeedback);

    super.onSaveInstanceState(outState);
  }

  /**
   * Adds either file or picture attachment by intent picker.
   *
   * @param request Either ATTACH_FILE or ATTACH_PICTURE.
   */
  private boolean addAttachment(int request) {
    if (request == ATTACH_FILE) {
      Intent intent = new Intent();
      intent.setType("*/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, getString(R.string.hockeyapp_feedback_select_file)), ATTACH_FILE);
      return true;

    } else if (request == ATTACH_PICTURE) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, getString(R.string.hockeyapp_feedback_select_picture)), ATTACH_PICTURE);
      return true;

    } else return false;
  }

  private void configureAppropriateView() {
    /** Try to retrieve the Feedback Token from {@link SharedPreferences} */
    token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this);
    if ((token == null) || (inSendFeedback)) {
      /** If Feedback Token is NULL, show the usual {@link FeedbackView} */
      configureFeedbackView(false);
    }
    else {
      /** If Feedback Token is NOT NULL, show the Add Response Button and fetch the feedback messages */
      configureFeedbackView(true);
      sendFetchFeedback(url, null, null, null, null, null, token, feedbackHandler, true);
    }
  }

  /**
   * Initializes the {@link ParseFeedbackTask}
   * @param feedbackResponseString	JSON string response
   */
  private void createParseFeedbackTask(String feedbackResponseString, String requestType) {
  	parseFeedbackTask = new ParseFeedbackTask(this, feedbackResponseString, parseFeedbackHandler, requestType);
  }

  private void hideKeyboard() {
    if (textInput != null) {
      InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
      manager.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
    }
  }

  /**
   * Initializes the Feedback response {@link Handler}
   */
  private void initFeedbackHandler() {
    feedbackHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        boolean success = false;
        error = new ErrorObject();

        if (msg != null && msg.getData() != null) {
          Bundle bundle = msg.getData();
          String responseString = bundle.getString("feedback_response");
          String statusCode = bundle.getString("feedback_status");
          String requestType = bundle.getString("request_type");
          if ((requestType.equals("send") && ((responseString == null) || (Integer.parseInt(statusCode) != 201)))) {
            // Send feedback went wrong if response is empty or status code != 201
            error.setMessage(getString(R.string.hockeyapp_feedback_send_generic_error));
          }
          else if ((requestType.equals("fetch") && (statusCode != null) && ((Integer.parseInt(statusCode) == 404) || (Integer.parseInt(statusCode) == 422)))) {
            // Fetch feedback went wrong if status code is 404 or 422
            resetFeedbackView();
            success = true;
          }
          else if (responseString != null) {
            startParseFeedbackTask(responseString, requestType);
            success = true;
          }
          else {
            error.setMessage(getString(R.string.hockeyapp_feedback_send_network_error));
          }
        }
        else {
          error.setMessage(getString(R.string.hockeyapp_feedback_send_generic_error));
        }

        if (!success) {
          runOnUiThread(new Runnable() {

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
              enableDisableSendFeedbackButton(true);
              showDialog(DIALOG_ERROR_ID);
            }
          });
        }

        onSendFeedbackResult(success);
      }
    };
  }

  /**
   * Initialize the Feedback response parse result {@link Handler}
   */
  private void initParseFeedbackHandler() {
    parseFeedbackHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        boolean success = false;
        error = new ErrorObject();

        if (msg != null && msg.getData() != null) {
          Bundle bundle = msg.getData();
          FeedbackResponse feedbackResponse = (FeedbackResponse) bundle.getSerializable("parse_feedback_response");
          if (feedbackResponse != null) {
            if (feedbackResponse.getStatus().equalsIgnoreCase("success")) {
              /** We have a valid result from JSON parsing */
              success = true;

              if (feedbackResponse.getToken() != null) {
                /** Save the Token to SharedPreferences */
                PrefsUtil.getInstance().saveFeedbackTokenToPrefs(context, feedbackResponse.getToken());
                /** Load the existing feedback messages */
                loadFeedbackMessages(feedbackResponse);
                inSendFeedback = false;
              }
            }
            else {
              success = false;
            }
          }
        }

        /** Something went wrong, so display an error dialog */
        if (!success) {
          runOnUiThread(new Runnable() {

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
              showDialog(DIALOG_ERROR_ID);
            }
          });
        }

        enableDisableSendFeedbackButton(true);
      }
    };
  }

  /**
   * Load the feedback messages fetched from server
   * @param feedbackResponse	{@link FeedbackResponse} object
   */
  private void loadFeedbackMessages(final FeedbackResponse feedbackResponse) {
    runOnUiThread(new Runnable() {

    	@Override
    	public void run() {
    		configureFeedbackView(true);

    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    		SimpleDateFormat formatNew = new SimpleDateFormat("d MMM h:mm a");

    		Date date = null;
    		if (feedbackResponse != null && feedbackResponse.getFeedback() != null &&
    				feedbackResponse.getFeedback().getMessages() != null && feedbackResponse.
    				getFeedback().getMessages().size() > 0) {

    			feedbackMessages = feedbackResponse.getFeedback().getMessages();
    			/** Reverse the order of the feedback messages list, so we show the latest one first */
    			Collections.reverse(feedbackMessages);

    			/** Set the lastUpdatedTextView text as the date of the latest feedback message */
    			try {
    				date = format.parse(feedbackMessages.get(0).getCreatedAt());
    				lastUpdatedTextView.setText(String.format(getString(R.string.hockeyapp_feedback_last_updated_text) + " %s", formatNew.format(date)));
    			}
    			catch (ParseException e1) {
    				e1.printStackTrace();
    			}

    			if (messagesAdapter == null) {
    				messagesAdapter = new MessagesAdapter(context, feedbackMessages);
    			}
    			else {
    				messagesAdapter.clear();
    				for (FeedbackMessage message : feedbackMessages) {
    					messagesAdapter.add(message);
    				}

    				messagesAdapter.notifyDataSetChanged();
    			}

    			messagesListView.setAdapter(messagesAdapter);
    		}
    	}
    });
  }

  private void resetFeedbackView() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        PrefsUtil.getInstance().saveFeedbackTokenToPrefs(FeedbackActivity.this, null);

        getSharedPreferences(ParseFeedbackTask.PREFERENCES_NAME, 0)
                .edit()
                .remove(ParseFeedbackTask.ID_LAST_MESSAGE_SEND)
                .remove(ParseFeedbackTask.ID_LAST_MESSAGE_PROCESSED)
                .apply();

        configureFeedbackView(false);
      }
    });
  }

  /**
   * Send feedback to HockeyApp.
   */
  private void sendFeedback() {
  	enableDisableSendFeedbackButton(false);
  	hideKeyboard();

  	String token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context);

  	String name = nameInput.getText().toString().trim();
  	String email = emailInput.getText().toString().trim();
  	String subject = subjectInput.getText().toString().trim();
  	String text = textInput.getText().toString().trim();

    if(TextUtils.isEmpty(subject)){
      subjectInput.setVisibility(View.VISIBLE);
      setError(subjectInput, R.string.hockeyapp_feedback_validate_subject_error);
    }
    else if (FeedbackManager.getRequireUserName() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(name)) {
      setError(nameInput, R.string.hockeyapp_feedback_validate_name_error);
    }
    else if (FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(email)) {
      setError(emailInput, R.string.hockeyapp_feedback_validate_email_empty);
    }
    else if(TextUtils.isEmpty(text)) {
      setError(textInput, R.string.hockeyapp_feedback_validate_text_error);
    }
  	else if (FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.REQUIRED && !Util.isValidEmail(email)) {
      setError(emailInput, R.string.hockeyapp_feedback_validate_email_error);
  	}
  	else {
  		/** Save Name and Email to {@link SharedPreferences} */
  		PrefsUtil.getInstance().saveNameEmailSubjectToPrefs(context, name, email, subject);

      /** Make list for attachments file paths */
      AttachmentListView attachmentListView = (AttachmentListView) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
      List<Uri> attachmentUris = attachmentListView.getAttachments();

  		/** Start the Send Feedback {@link AsyncTask} */
  		sendFetchFeedback(url, name, email, subject, text, attachmentUris, token, feedbackHandler, false);
  	}
  }

   private void setError(EditText inputField, int feedbackStringId) {
      inputField.setError(getString(feedbackStringId));
      enableDisableSendFeedbackButton(true);
   }

   /**
   * Initialize the {@link SendFeedbackTask}
   * @param url             URL to HockeyApp API
   * @param name            Name of the feedback sender
   * @param email           Email of the feedback sender
   * @param subject         Message subject
   * @param text            The message
   * @param token           Token for message feed
   * @param feedbackHandler Handler to handle the response
   * @param isFetchMessages Set true to fetch messages, false to send one
   */
  private void sendFetchFeedback(String url, String name, String email, String subject, String text, List<Uri> attachmentUris, String token, Handler feedbackHandler, boolean isFetchMessages) {
    sendFeedbackTask = new SendFeedbackTask(context, url, name, email, subject, text, attachmentUris, token, feedbackHandler, isFetchMessages);
    AsyncTaskUtils.execute(sendFeedbackTask);
  }

  /**
   * Creates and starts execution of the {@link ParseFeedbackTask}
   * @param feedbackResponseString	JSON string response
   */
  private void startParseFeedbackTask(String feedbackResponseString, String requestType) {
  	createParseFeedbackTask(feedbackResponseString, requestType);
    AsyncTaskUtils.execute(parseFeedbackTask);
  }
}
