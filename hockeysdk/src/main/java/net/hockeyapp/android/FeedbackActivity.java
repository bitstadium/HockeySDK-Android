package net.hockeyapp.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.adapters.MessagesAdapter;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.objects.FeedbackUserDataElement;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.views.AttachmentListView;
import net.hockeyapp.android.views.AttachmentView;

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
public class FeedbackActivity extends Activity implements OnClickListener {

    /**
     * The URL of the feedback endpoint for this app.
     */
    public static final String EXTRA_URL = "url";

    /**
     * Extra for initial username to set for the feedback message.
     */
    public static final String EXTRA_INITIAL_USER_NAME = "initialUserName";

    /**
     * Extra for initial email address to set for the feedback message.
     */
    public static final String EXTRA_INITIAL_USER_EMAIL = "initialUserEmail";

    /**
     * Extra for any initial attachments to add to the feedback message.
     */
    public static final String EXTRA_INITIAL_ATTACHMENTS = "initialAttachments";

    /**
     * Number of attachments allowed per message.
     **/
    private static final int MAX_ATTACHMENTS_PER_MSG = 3;

    /**
     * ID of error dialog
     **/
    private static final int DIALOG_ERROR_ID = 0;
    /**
     * Activity request constants for ContextMenu and Chooser Intent
     */
    private static final int ATTACH_PICTURE = 1;
    private static final int ATTACH_FILE = 2;
    private static final int PAINT_IMAGE = 3;

    /**
     * Initial user's name to pre-fill the feedback form with
     */
    private String initialUserName;

    /**
     * Initial user's e-mail to pre-fill the feedback form with
     */
    private String initialUserEmail;

    /**
     * Reference to this
     **/
    private Context mContext;
    /**
     * Widgets and layout
     **/
    private TextView mLastUpdatedTextView;
    private EditText mNameInput;
    private EditText mEmailInput;
    private EditText mSubjectInput;
    private EditText mTextInput;
    private Button mSendFeedbackButton;
    private Button mAddAttachmentButton;
    private Button mAddResponseButton;
    private Button mRefreshButton;
    private ScrollView mFeedbackScrollview;
    private LinearLayout mWrapperLayoutFeedbackAndMessages;
    private ListView mMessagesListView;
    /**
     * Send feedback {@link AsyncTask}
     */
    private SendFeedbackTask mSendFeedbackTask;
    private Handler mFeedbackHandler;

    /**
     * Parse feedback {@link AsyncTask}
     */
    private ParseFeedbackTask mParseFeedbackTask;
    private Handler mParseFeedbackHandler;

    /**
     * Initial attachment uris
     */
    private List<Uri> mInitialAttachments;

    /**
     * URL for HockeyApp API
     **/
    private String mUrl;

    /**
     * Current error for alert dialog
     **/
    private ErrorObject mError;

    /**
     * Message data source
     **/
    private MessagesAdapter mMessagesAdapter;
    private ArrayList<FeedbackMessage> mFeedbackMessages;

    /**
     * True when a message is posted
     **/
    private boolean mInSendFeedback;

    /**
     * True when the view was initialized
     **/
    private boolean mFeedbackViewInitialized;

    /**
     * Unique token of the message feed
     **/
    private String mToken;

    /**
     * Called when the activity is starting. Sets the title and content view
     *
     * @param savedInstanceState Data it most recently supplied in
     *                           onSaveInstanceState(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutView());

        setTitle(getString(R.string.hockeyapp_feedback_title));
        mContext = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUrl = extras.getString(EXTRA_URL);
            initialUserName = extras.getString(EXTRA_INITIAL_USER_NAME);
            initialUserEmail = extras.getString(EXTRA_INITIAL_USER_EMAIL);

            Parcelable[] initialAttachmentsArray = extras.getParcelableArray(EXTRA_INITIAL_ATTACHMENTS);
            if (initialAttachmentsArray != null) {
                mInitialAttachments = new ArrayList<Uri>();
                for (Parcelable parcelable : initialAttachmentsArray) {
                    mInitialAttachments.add((Uri) parcelable);
                }
            }
        }

        if (savedInstanceState != null) {
            mFeedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
            mInSendFeedback = savedInstanceState.getBoolean("inSendFeedback");
        } else {
            mInSendFeedback = false;
            mFeedbackViewInitialized = false;
        }

        // Cancel notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ParseFeedbackTask.NEW_ANSWER_NOTIFICATION_ID);

        initFeedbackHandler();
        initParseFeedbackHandler();
        configureAppropriateView();
    }

    /**
     * Restore all attachments.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ViewGroup attachmentList = (ViewGroup) findViewById(R.id.wrapper_attachments);
            ArrayList<Uri> attachmentsUris = savedInstanceState.getParcelableArrayList("attachments");
            for (Uri attachmentUri : attachmentsUris) {
                attachmentList.addView(new AttachmentView(this, attachmentList, attachmentUri, true));
            }

            mFeedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Save all attachments.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        AttachmentListView attachmentListView = (AttachmentListView) findViewById(R.id.wrapper_attachments);

        outState.putParcelableArrayList("attachments", attachmentListView.getAttachments());
        outState.putBoolean("feedbackViewInitialized", mFeedbackViewInitialized);
        outState.putBoolean("inSendFeedback", mInSendFeedback);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mSendFeedbackTask != null) {
            mSendFeedbackTask.detach();
        }
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
        if (mSendFeedbackTask != null) {
            mSendFeedbackTask.detach();
        }

        return mSendFeedbackTask;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mInSendFeedback) {
                mInSendFeedback = false;
                configureAppropriateView();
            } else {
                finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Called when the Send Feedback {@link Button} is tapped. Sends the feedback and disables
     * the button to avoid multiple taps.
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.button_send) {
            sendFeedback();
        } else if (viewId == R.id.button_attachment) {
            ViewGroup attachments = (ViewGroup) findViewById(R.id.wrapper_attachments);
            if (attachments.getChildCount() >= MAX_ATTACHMENTS_PER_MSG) {
                //TODO should we add some more text here?
                Toast.makeText(this, String.valueOf(MAX_ATTACHMENTS_PER_MSG), Toast.LENGTH_SHORT).show();
            } else {
                openContextMenu(v);
            }
        } else if (viewId == R.id.button_add_response) {
            configureFeedbackView(false);
            mInSendFeedback = true;
        } else if (viewId == R.id.button_refresh) {
            sendFetchFeedback(mUrl, null, null, null, null, null, PrefsUtil.getInstance().getFeedbackTokenFromPrefs(mContext), mFeedbackHandler, true);
        }
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

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ERROR_ID:
                return new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.hockeyapp_dialog_error_message))
                        .setCancelable(false)
                        .setTitle(getString(R.string.hockeyapp_dialog_error_title))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(getString(R.string.hockeyapp_dialog_positive_button), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mError = null;
                                dialog.cancel();
                            }
                        }).create();
        }

        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_ERROR_ID:
                AlertDialog messageDialogError = (AlertDialog) dialog;
                if (mError != null) {
                    /** If the ErrorObject is not null, display the ErrorObject message */
                    messageDialogError.setMessage(mError.getMessage());
                } else {
                    /** If the ErrorObject is null, display the general error message */
                    messageDialogError.setMessage(getString(R.string.hockeyapp_feedback_generic_error));
                }
                break;
            default:
                break;
        }

    }

    /**
     * Called when picture or file was chosen.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == ATTACH_FILE) {
            /** User picked file */
            Uri uri = data.getData();

            if (uri != null) {
                final ViewGroup attachments = (ViewGroup) findViewById(R.id.wrapper_attachments);
                attachments.addView(new AttachmentView(this, attachments, uri, true));
            }

        } else if (requestCode == ATTACH_PICTURE) {
            /** User picked image */
            Uri uri = data.getData();

            /** Start PaintActivity */
            if (uri != null) {
                try {
                    Intent intent = new Intent(this, PaintActivity.class);
                    intent.putExtra(PaintActivity.EXTRA_IMAGE_URI, uri);
                    startActivityForResult(intent, PAINT_IMAGE);
                } catch (ActivityNotFoundException e) {
                    HockeyLog.error(Util.LOG_IDENTIFIER, "Paint activity not declared!", e);
                }

            }

        } else if (requestCode == PAINT_IMAGE) {
            /** Final attachment picture received and ready to be added to list. */
            Uri uri = data.getParcelableExtra(PaintActivity.EXTRA_IMAGE_URI);

            if (uri != null) {
                final ViewGroup attachments = (ViewGroup) findViewById(R.id.wrapper_attachments);
                attachments.addView(new AttachmentView(this, attachments, uri, true));
            }

        } else return;
    }

    @SuppressLint("InflateParams")
    public View getLayoutView() {
        return getLayoutInflater().inflate(R.layout.hockeyapp_activity_feedback, null);
    }

    /**
     * Enables/Disables the Send Feedback button.
     *
     * @param isEnable the button is enabled if true
     */
    public void enableDisableSendFeedbackButton(boolean isEnable) {
        if (mSendFeedbackButton != null) {
            mSendFeedbackButton.setEnabled(isEnable);
        }
    }

    /**
     * Configures the content view by initializing the input {@link EditText}s
     * and the listener for the Send Feedback {@link Button}
     *
     * @param haveToken the message list is shown if true
     */
    protected void configureFeedbackView(boolean haveToken) {
        mFeedbackScrollview = (ScrollView) findViewById(R.id.wrapper_feedback_scroll);
        mWrapperLayoutFeedbackAndMessages = (LinearLayout) findViewById(R.id.wrapper_messages);
        mMessagesListView = (ListView) findViewById(R.id.list_feedback_messages);

        if (haveToken) {
            /** If a token exists, the list of messages should be displayed */
            mWrapperLayoutFeedbackAndMessages.setVisibility(View.VISIBLE);
            mFeedbackScrollview.setVisibility(View.GONE);

            mLastUpdatedTextView = (TextView) findViewById(R.id.label_last_updated);

            mAddResponseButton = (Button) findViewById(R.id.button_add_response);
            mAddResponseButton.setOnClickListener(this);

            mRefreshButton = (Button) findViewById(R.id.button_refresh);
            mRefreshButton.setOnClickListener(this);
        } else {
            /** if the token doesn't exist, the feedback details inputs to be sent need to be displayed */
            mWrapperLayoutFeedbackAndMessages.setVisibility(View.GONE);
            mFeedbackScrollview.setVisibility(View.VISIBLE);

            mNameInput = (EditText) findViewById(R.id.input_name);
            mEmailInput = (EditText) findViewById(R.id.input_email);
            mSubjectInput = (EditText) findViewById(R.id.input_subject);
            mTextInput = (EditText) findViewById(R.id.input_message);

            /** Check to see if the Name and Email are saved in {@link SharedPreferences} */
            if (!mFeedbackViewInitialized) {
                String nameEmailSubject = PrefsUtil.getInstance().getNameEmailFromPrefs(mContext);
                if (nameEmailSubject != null) {
                    /** We have Name and Email. Prepopulate the appropriate fields */
                    String[] nameEmailSubjectArray = nameEmailSubject.split("\\|");
                    if (nameEmailSubjectArray != null && nameEmailSubjectArray.length >= 2) {
                        mNameInput.setText(nameEmailSubjectArray[0]);
                        mEmailInput.setText(nameEmailSubjectArray[1]);

                        if (nameEmailSubjectArray.length >= 3) {
                            mSubjectInput.setText(nameEmailSubjectArray[2]);
                            mTextInput.requestFocus();
                        } else {
                            mSubjectInput.requestFocus();
                        }
                    }
                } else {
                    /** We dont have Name and Email. Check if initial values were provided */
                    mNameInput.setText(initialUserName);
                    mEmailInput.setText(initialUserEmail);
                    mSubjectInput.setText("");
                    if (TextUtils.isEmpty(initialUserName)) {
                        mNameInput.requestFocus();
                    } else if (TextUtils.isEmpty(initialUserEmail)) {
                        mEmailInput.requestFocus();
                    } else {
                        mSubjectInput.requestFocus();
                    }
                }

                mFeedbackViewInitialized = true;
            }

            /** Reset the remaining fields if previously populated */
            mTextInput.setText("");

            /** Check to see if the Feedback Token is availabe */
            if (PrefsUtil.getInstance().getFeedbackTokenFromPrefs(mContext) != null) {
                /** If Feedback Token is available, hide the Subject Input field */
                mSubjectInput.setVisibility(View.GONE);
            } else {
                /** If Feedback Token is not available, display the Subject Input field */
                mSubjectInput.setVisibility(View.VISIBLE);
            }

            /** Reset the attachment list */
            ViewGroup attachmentListView = (ViewGroup) findViewById(R.id.wrapper_attachments);
            attachmentListView.removeAllViews();

            if (mInitialAttachments != null) {
                for (Uri attachmentUri : mInitialAttachments) {
                    attachmentListView.addView(new AttachmentView(this, attachmentListView, attachmentUri, true));
                }
            }

            /** Use of context menu needs to be enabled explicitly */
            mAddAttachmentButton = (Button) findViewById(R.id.button_attachment);
            mAddAttachmentButton.setOnClickListener(this);
            registerForContextMenu(mAddAttachmentButton);

            mSendFeedbackButton = (Button) findViewById(R.id.button_send);
            mSendFeedbackButton.setOnClickListener(this);
        }
    }

    /**
     * Called when the request for sending the feedback has finished.
     *
     * @param success is true if the sending of the feedback was successful
     */
    protected void onSendFeedbackResult(final boolean success) {
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
        mToken = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this);
        if ((mToken == null) || (mInSendFeedback)) {
            /** If Feedback Token is NULL, show the usual feedback view */
            configureFeedbackView(false);
        } else {
            /** If Feedback Token is NOT NULL, show the Add Response Button and fetch the feedback messages */
            configureFeedbackView(true);
            sendFetchFeedback(mUrl, null, null, null, null, null, mToken, mFeedbackHandler, true);
        }
    }

    /**
     * Initializes the {@link ParseFeedbackTask}
     *
     * @param feedbackResponseString JSON string response
     */
    private void createParseFeedbackTask(String feedbackResponseString, String requestType) {
        mParseFeedbackTask = new ParseFeedbackTask(this, feedbackResponseString, mParseFeedbackHandler, requestType);
    }

    private void hideKeyboard() {
        if (mTextInput != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(mTextInput.getWindowToken(), 0);
        }
    }

    /**
     * Initializes the Feedback response {@link Handler}
     */
    private void initFeedbackHandler() {
        mFeedbackHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                boolean success = false;
                mError = new ErrorObject();

                if (msg != null && msg.getData() != null) {
                    Bundle bundle = msg.getData();
                    String responseString = bundle.getString(SendFeedbackTask.BUNDLE_FEEDBACK_RESPONSE);
                    String statusCode = bundle.getString(SendFeedbackTask.BUNDLE_FEEDBACK_STATUS);
                    String requestType = bundle.getString(SendFeedbackTask.BUNDLE_REQUEST_TYPE);
                    if ((requestType.equals("send") && ((responseString == null) || (Integer.parseInt(statusCode) != 201)))) {
                        // Send feedback went wrong if response is empty or status code != 201
                        mError.setMessage(getString(R.string.hockeyapp_feedback_send_generic_error));
                    } else if ((requestType.equals("fetch") && (statusCode != null) && ((Integer.parseInt(statusCode) == 404) || (Integer.parseInt(statusCode) == 422)))) {
                        // Fetch feedback went wrong if status code is 404 or 422
                        resetFeedbackView();
                        success = true;
                    } else if (responseString != null) {
                        startParseFeedbackTask(responseString, requestType);
                        success = true;
                    } else {
                        mError.setMessage(getString(R.string.hockeyapp_feedback_send_network_error));
                    }
                } else {
                    mError.setMessage(getString(R.string.hockeyapp_feedback_send_generic_error));
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
        mParseFeedbackHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                boolean success = false;
                mError = new ErrorObject();

                if (msg != null && msg.getData() != null) {
                    Bundle bundle = msg.getData();
                    FeedbackResponse feedbackResponse = (FeedbackResponse) bundle.getSerializable(ParseFeedbackTask.BUNDLE_PARSE_FEEDBACK_RESPONSE);
                    if (feedbackResponse != null) {
                        if (feedbackResponse.getStatus().equalsIgnoreCase("success")) {
                            /** We have a valid result from JSON parsing */
                            success = true;

                            if (feedbackResponse.getToken() != null) {
                                /** Save the Token to SharedPreferences */
                                PrefsUtil.getInstance().saveFeedbackTokenToPrefs(mContext, feedbackResponse.getToken());
                                /** Load the existing feedback messages */
                                loadFeedbackMessages(feedbackResponse);
                                mInSendFeedback = false;
                            }
                        } else {
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
     *
     * @param feedbackResponse {@link FeedbackResponse} object
     */
    @SuppressLint("SimpleDateFormat")
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

                    mFeedbackMessages = feedbackResponse.getFeedback().getMessages();
                    /** Reverse the order of the feedback messages list, so we show the latest one first */
                    Collections.reverse(mFeedbackMessages);

                    /** Set the lastUpdatedTextView text as the date of the latest feedback message */
                    try {
                        date = format.parse(mFeedbackMessages.get(0).getCreatedAt());
                        mLastUpdatedTextView.setText(getString(R.string.hockeyapp_feedback_last_updated_text, formatNew.format(date)));
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    if (mMessagesAdapter == null) {
                        mMessagesAdapter = new MessagesAdapter(mContext, mFeedbackMessages);
                    } else {
                        mMessagesAdapter.clear();
                        for (FeedbackMessage message : mFeedbackMessages) {
                            mMessagesAdapter.add(message);
                        }

                        mMessagesAdapter.notifyDataSetChanged();
                    }

                    mMessagesListView.setAdapter(mMessagesAdapter);
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
        if (!Util.isConnectedToNetwork(this)) {
            Toast errorToast = Toast.makeText(this, R.string.hockeyapp_error_no_network_message, Toast.LENGTH_LONG);
            errorToast.show();
            return;
        }

        enableDisableSendFeedbackButton(false);
        hideKeyboard();

        String token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(mContext);

        String name = mNameInput.getText().toString().trim();
        String email = mEmailInput.getText().toString().trim();
        String subject = mSubjectInput.getText().toString().trim();
        String text = mTextInput.getText().toString().trim();

        if (TextUtils.isEmpty(subject)) {
            mSubjectInput.setVisibility(View.VISIBLE);
            setError(mSubjectInput, R.string.hockeyapp_feedback_validate_subject_error);
        } else if (FeedbackManager.getRequireUserName() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(name)) {
            setError(mNameInput, R.string.hockeyapp_feedback_validate_name_error);
        } else if (FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(email)) {
            setError(mEmailInput, R.string.hockeyapp_feedback_validate_email_empty);
        } else if (TextUtils.isEmpty(text)) {
            setError(mTextInput, R.string.hockeyapp_feedback_validate_text_error);
        } else if (FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.REQUIRED && !Util.isValidEmail(email)) {
            setError(mEmailInput, R.string.hockeyapp_feedback_validate_email_error);
        } else {
            /** Save Name and Email to {@link SharedPreferences} */
            PrefsUtil.getInstance().saveNameEmailSubjectToPrefs(mContext, name, email, subject);

            /** Make list for attachments file paths */
            AttachmentListView attachmentListView = (AttachmentListView) findViewById(R.id.wrapper_attachments);
            List<Uri> attachmentUris = attachmentListView.getAttachments();

            /** Start the Send Feedback {@link AsyncTask} */
            sendFetchFeedback(mUrl, name, email, subject, text, attachmentUris, token, mFeedbackHandler, false);
        }
    }

    private void setError(EditText inputField, int feedbackStringId) {
        inputField.setError(getString(feedbackStringId));
        enableDisableSendFeedbackButton(true);
    }

    /**
     * Initialize the {@link SendFeedbackTask}
     *
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
        mSendFeedbackTask = new SendFeedbackTask(mContext, url, name, email, subject, text, attachmentUris, token, feedbackHandler, isFetchMessages);
        AsyncTaskUtils.execute(mSendFeedbackTask);
    }

    /**
     * Creates and starts execution of the {@link ParseFeedbackTask}
     *
     * @param feedbackResponseString JSON string response
     */
    private void startParseFeedbackTask(String feedbackResponseString, String requestType) {
        createParseFeedbackTask(feedbackResponseString, requestType);
        AsyncTaskUtils.execute(mParseFeedbackTask);
    }
}
