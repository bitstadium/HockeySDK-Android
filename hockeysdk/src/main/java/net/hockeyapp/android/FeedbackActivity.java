package net.hockeyapp.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.adapters.MessagesAdapter;
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

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static java.text.DateFormat.SHORT;

/**
 * <h3>Description</h3>
 *
 * Activity to show the feedback form.
 **/
@SuppressWarnings("DanglingJavadoc")
public class FeedbackActivity extends Activity implements OnClickListener, View.OnFocusChangeListener {

    /**
     * The URL of the feedback endpoint for this app.
     */
    public static final String EXTRA_URL = "url";

    /**
     * Token of the message feed.
     */
    public static final String EXTRA_TOKEN = "token";

    /**
     * Optional extra that can be passed as {@code true} to force a new feedback message thread.
     */
    public static final String EXTRA_FORCE_NEW_THREAD = "forceNewThread";

    /**
     * Extra for initial username to set for the feedback message.
     */
    public static final String EXTRA_INITIAL_USER_NAME = "initialUserName";

    /**
     * Extra for initial email address to set for the feedback message.
     */
    public static final String EXTRA_INITIAL_USER_EMAIL = "initialUserEmail";

    /**
     * Extra for initial email address to set for the feedback message.
     */
    public static final String EXTRA_INITIAL_USER_SUBJECT = "initialUserSubject";

    /**
     * Extra for any initial attachments to add to the feedback message.
     */
    public static final String EXTRA_INITIAL_ATTACHMENTS = "initialAttachments";

    /**
     * Number of attachments allowed per message.
     **/
    private static final int MAX_ATTACHMENTS_PER_MSG = 3;

    /**
     * Activity request constants for ContextMenu and Chooser Intent
     */
    private static final int ATTACH_PICTURE = 1;
    private static final int ATTACH_FILE = 2;
    private static final int PAINT_IMAGE = 3;

    /**
     * Initial user's name to pre-fill the feedback form with
     */
    private String mInitialUserName;

    /**
     * Initial user's e-mail to pre-fill the feedback form with
     */
    private String mInitialUserEmail;

    /**
     * Initial user's subject to pre-fill the feedback form with
     */
    private String mInitialUserSubject;

    /**
     * Initial attachment uris
     */
    private List<Uri> mInitialAttachments = new ArrayList<>();

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
    private ListView mMessagesListView;
    private AttachmentListView mAttachmentListView;
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
     * URL for HockeyApp API
     **/
    private String mUrl;

    /**
     * Message data source
     **/
    private MessagesAdapter mMessagesAdapter;

    /**
     * True when a message is posted
     **/
    private boolean mInSendFeedback;

    /**
     * Indicates if a new thread should be created for each new feedback message as opposed to
     * the default resume thread behaviour.
     */
    private boolean mForceNewThread;

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

        setTitle(R.string.hockeyapp_feedback_title);
        mContext = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUrl = extras.getString(EXTRA_URL);
            mToken = extras.getString(EXTRA_TOKEN);
            mForceNewThread = extras.getBoolean(EXTRA_FORCE_NEW_THREAD);
            mInitialUserName = extras.getString(EXTRA_INITIAL_USER_NAME);
            mInitialUserEmail = extras.getString(EXTRA_INITIAL_USER_EMAIL);
            mInitialUserSubject = extras.getString(EXTRA_INITIAL_USER_SUBJECT);

            Parcelable[] initialAttachmentsArray = extras.getParcelableArray(EXTRA_INITIAL_ATTACHMENTS);
            if (initialAttachmentsArray != null) {
                mInitialAttachments.clear();
                for (Parcelable parcelable : initialAttachmentsArray) {
                    mInitialAttachments.add((Uri) parcelable);
                }
            }
        }

        if (savedInstanceState != null) {
            mFeedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
            mInSendFeedback = savedInstanceState.getBoolean("inSendFeedback");
            mToken = savedInstanceState.getString("token");
        } else {
            mInSendFeedback = false;
            mFeedbackViewInitialized = false;
        }

        // Cancel notification
        Util.cancelNotification(this, FeedbackManager.NEW_ANSWER_NOTIFICATION_ID);

        initFeedbackHandler();
        initParseFeedbackHandler();
        restoreSendFeedbackTask();
        configureAppropriateView();
    }

    private void restoreSendFeedbackTask() {
        Object object = getLastNonConfigurationInstance();
        if (object != null && object instanceof SendFeedbackTask) {
            mSendFeedbackTask = (SendFeedbackTask) object;
            /**
             * We are restoring mSendFeedbackTask object and we need to replace old handler
             * with newly created, so that task could send messages to right handler.
             */
            mSendFeedbackTask.setHandler(mFeedbackHandler);
        }
    }

    /**
     * Restore all attachments.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ArrayList<Uri> attachmentsUris = savedInstanceState.getParcelableArrayList("attachments");
            if (attachmentsUris != null) {
                for (Uri attachmentUri : attachmentsUris) {
                    if (!mInitialAttachments.contains(attachmentUri)) {
                        mAttachmentListView.addView(new AttachmentView(this, mAttachmentListView, attachmentUri, true));
                    }
                }
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
        outState.putParcelableArrayList("attachments", mAttachmentListView.getAttachments());
        outState.putBoolean("feedbackViewInitialized", mFeedbackViewInitialized);
        outState.putBoolean("inSendFeedback", mInSendFeedback);
        outState.putString("token", mToken);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSendFeedbackTask != null){
            mSendFeedbackTask.attach(this);
        }
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
            if (mAttachmentListView.getChildCount() >= MAX_ATTACHMENTS_PER_MSG) {
                Toast.makeText(this, getString(R.string.hockeyapp_feedback_max_attachments_allowed, MAX_ATTACHMENTS_PER_MSG), Toast.LENGTH_SHORT).show();
            } else {
                openContextMenu(v);
            }
        } else if (viewId == R.id.button_add_response) {
            mInSendFeedback = true;
            configureFeedbackView(false);
        } else if (viewId == R.id.button_refresh) {
            sendFetchFeedback(mUrl, null, null, null, null, null, mToken, mFeedbackHandler, true);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
                if (v instanceof EditText) {
                showKeyboard(v);
            }
            else if (v instanceof Button || v instanceof ImageButton) {
                hideKeyboard();
            }
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
                mAttachmentListView.addView(new AttachmentView(this, mAttachmentListView, uri, true));
                Util.announceForAccessibility(mAttachmentListView, getString(R.string.hockeyapp_feedback_attachment_added));
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
                    HockeyLog.error("Paint activity not declared!", e);
                }

            }

        } else if (requestCode == PAINT_IMAGE) {
            /** Final attachment picture received and ready to be added to list. */
            Uri uri = data.getParcelableExtra(PaintActivity.EXTRA_IMAGE_URI);

            if (uri != null) {
                mAttachmentListView.addView(new AttachmentView(this, mAttachmentListView, uri, true));
                Util.announceForAccessibility(mAttachmentListView, getString(R.string.hockeyapp_feedback_attachment_added));
            }

        }
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
        ScrollView feedbackScrollView = findViewById(R.id.wrapper_feedback_scroll);
        LinearLayout wrapperLayoutFeedbackAndMessages = findViewById(R.id.wrapper_messages);
        mMessagesListView = findViewById(R.id.list_feedback_messages);
        mAttachmentListView = findViewById(R.id.wrapper_attachments);

        if (haveToken) {
            /** If a token exists, the list of messages should be displayed */
            wrapperLayoutFeedbackAndMessages.setVisibility(View.VISIBLE);
            feedbackScrollView.setVisibility(View.GONE);

            mLastUpdatedTextView = findViewById(R.id.label_last_updated);
            mLastUpdatedTextView.setVisibility(View.INVISIBLE);

            Button addResponseButton = findViewById(R.id.button_add_response);
            addResponseButton.setOnClickListener(this);
            addResponseButton.setOnFocusChangeListener(this);

            Button refreshButton = findViewById(R.id.button_refresh);
            refreshButton.setOnClickListener(this);
            refreshButton.setOnFocusChangeListener(this);
        } else {
            /** if the token doesn't exist, the feedback details inputs to be sent need to be displayed */
            wrapperLayoutFeedbackAndMessages.setVisibility(View.GONE);
            feedbackScrollView.setVisibility(View.VISIBLE);

            mNameInput = findViewById(R.id.input_name);
            mNameInput.setOnFocusChangeListener(this);
            mEmailInput = findViewById(R.id.input_email);
            mEmailInput.setOnFocusChangeListener(this);
            mSubjectInput = findViewById(R.id.input_subject);
            mSubjectInput.setOnFocusChangeListener(this);
            mTextInput = findViewById(R.id.input_message);
            mTextInput.setOnFocusChangeListener(this);

            configureHints();

            if (!mFeedbackViewInitialized) {
                mNameInput.setText(mInitialUserName);
                mEmailInput.setText(mInitialUserEmail);
                mSubjectInput.setText(mInitialUserSubject);
                if (TextUtils.isEmpty(mInitialUserName)) {
                    mNameInput.requestFocus();
                } else if (TextUtils.isEmpty(mInitialUserEmail)) {
                    mEmailInput.requestFocus();
                } else if (TextUtils.isEmpty(mInitialUserSubject)) {
                    mSubjectInput.requestFocus();
                } else {
                    mTextInput.requestFocus();
                }
                mFeedbackViewInitialized = true;
            }

            mNameInput.setVisibility(FeedbackManager.getRequireUserName() == FeedbackUserDataElement.DONT_SHOW ? View.GONE : View.VISIBLE);
            mEmailInput.setVisibility(FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.DONT_SHOW ? View.GONE : View.VISIBLE);

            /** Reset the remaining fields if previously populated */
            mTextInput.setText("");

            /** Check to see if the Feedback Token is available */
            if ((!mForceNewThread || mInSendFeedback) && mToken != null) {
                /** If Feedback Token is available, hide the Subject Input field */
                mSubjectInput.setVisibility(View.GONE);
            } else {
                /** If Feedback Token is not available, display the Subject Input field */
                mSubjectInput.setVisibility(View.VISIBLE);
            }

            /** Reset the attachment list */
            mAttachmentListView.removeAllViews();

            for (Uri attachmentUri : mInitialAttachments) {
                mAttachmentListView.addView(new AttachmentView(this, mAttachmentListView, attachmentUri, true));
            }

            /** Use of context menu needs to be enabled explicitly */
            Button addAttachmentButton = findViewById(R.id.button_attachment);
            addAttachmentButton.setOnClickListener(this);
            addAttachmentButton.setOnFocusChangeListener(this);
            registerForContextMenu(addAttachmentButton);

            mSendFeedbackButton = findViewById(R.id.button_send);
            mSendFeedbackButton.setOnClickListener(this);
            addAttachmentButton.setOnFocusChangeListener(this);
        }
    }

    /**
     * Called when the request for sending the feedback has finished.
     *
     * @param success is true if the sending of the feedback was successful
     */
    @SuppressWarnings("UnusedParameters")
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

    private void configureHints() {
        if (FeedbackManager.getRequireUserName() == FeedbackUserDataElement.REQUIRED) {
            mNameInput.setHint(getString(R.string.hockeyapp_feedback_name_hint_required));
        }
        if (FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.REQUIRED) {
            mEmailInput.setHint(getString(R.string.hockeyapp_feedback_email_hint_required));
        }
        mSubjectInput.setHint(getString(R.string.hockeyapp_feedback_subject_hint_required));
        mTextInput.setHint(getString(R.string.hockeyapp_feedback_message_hint_required));
    }

    private void configureAppropriateView() {
        if (mToken == null || mInSendFeedback) {
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

    private void showKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        if (mTextInput != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(mTextInput.getWindowToken(), 0);
        }
    }

    private void showError(final int message) {
        AlertDialog alertDialog = new AlertDialog.Builder(FeedbackActivity.this)
                .setTitle(R.string.hockeyapp_dialog_error_title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.hockeyapp_dialog_positive_button, null)
                .create();
        alertDialog.show();
    }

    /**
     * Initializes the Feedback response {@link Handler}
     */
    private void initFeedbackHandler() {
        mFeedbackHandler = new FeedbackHandler(this);
    }

    /**
     * Initialize the Feedback response parse result {@link Handler}
     */
    private void initParseFeedbackHandler() {
        mParseFeedbackHandler = new ParseFeedbackHandler(this);
    }

    /**
     * Load the feedback messages fetched from server
     *
     * @param feedbackResponse {@link FeedbackResponse} object
     */
    @SuppressLint("SimpleDateFormat")
    private void loadFeedbackMessages(final FeedbackResponse feedbackResponse) {
        configureFeedbackView(true);

        Date date;
        if (feedbackResponse != null && feedbackResponse.getFeedback() != null &&
                feedbackResponse.getFeedback().getMessages() != null && feedbackResponse.
                getFeedback().getMessages().size() > 0) {

            ArrayList<FeedbackMessage> feedbackMessages = feedbackResponse.getFeedback().getMessages();
            /** Reverse the order of the feedback messages list, so we show the latest one first */
            Collections.reverse(feedbackMessages);

            /** Set the lastUpdatedTextView text as the date of the latest feedback message */
            try {
                /** An ISO 8601 format */
                DateFormat dateFormatIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                dateFormatIn.setTimeZone(TimeZone.getTimeZone("UTC"));

                /** Localized short format */
                DateFormat dateFormatOut = DateFormat.getDateTimeInstance(SHORT, SHORT);

                date = dateFormatIn.parse(feedbackMessages.get(0).getCreatedAt());
                mLastUpdatedTextView.setText(String.format(getString(R.string.hockeyapp_feedback_last_updated_text), dateFormatOut.format(date)));
                mLastUpdatedTextView.setContentDescription(mLastUpdatedTextView.getText());
                mLastUpdatedTextView.setVisibility(View.VISIBLE);
            } catch (ParseException e1) {
                HockeyLog.error("Failed to parse feedback", e1);
            }

            if (mMessagesAdapter == null) {
                mMessagesAdapter = new MessagesAdapter(mContext, feedbackMessages);
            } else {
                mMessagesAdapter.clear();
                for (FeedbackMessage message : feedbackMessages) {
                    mMessagesAdapter.add(message);
                }

                mMessagesAdapter.notifyDataSetChanged();
            }

            mMessagesListView.setAdapter(mMessagesAdapter);
        }
    }

    private void resetFeedbackView() {
        mToken = null;
        AsyncTaskUtils.execute(new AsyncTask<Void, Object, Object>() {

            @Override
            protected Object doInBackground(Void... voids) {
                PrefsUtil.getInstance().saveFeedbackTokenToPrefs(FeedbackActivity.this, null);
                getSharedPreferences(ParseFeedbackTask.PREFERENCES_NAME, 0)
                        .edit()
                        .remove(ParseFeedbackTask.ID_LAST_MESSAGE_SEND)
                        .remove(ParseFeedbackTask.ID_LAST_MESSAGE_PROCESSED)
                        .apply();
                return null;
            }
        });

        configureFeedbackView(false);
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

        final String token = mForceNewThread && !mInSendFeedback ? null : mToken;
        final String name = mNameInput.getText().toString().trim();
        final String email = mEmailInput.getText().toString().trim();
        final String subject = mSubjectInput.getText().toString().trim();
        final String text = mTextInput.getText().toString().trim();

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
            AsyncTaskUtils.execute(new AsyncTask<Void, Object, Object>() {
                @Override
                protected Object doInBackground(Void... voids) {
                    PrefsUtil.getInstance().saveNameEmailSubjectToPrefs(mContext, name, email, subject);
                    return null;
                }
            });

            /** Make list for attachments file paths */
            List<Uri> attachmentUris = mAttachmentListView.getAttachments();

            /** Start the Send Feedback {@link AsyncTask} */
            sendFetchFeedback(mUrl, name, email, subject, text, attachmentUris, token, mFeedbackHandler, false);

            hideKeyboard();
        }
    }

    private void setError(final EditText inputField, int feedbackStringId) {
        inputField.setError(getString(feedbackStringId));

        // requestFocus and showKeyboard on next frame to read error message via talkback
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                inputField.requestFocus();
            }
        });
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

    private static class FeedbackHandler extends Handler {

        private final WeakReference<FeedbackActivity> mWeakFeedbackActivity;

        FeedbackHandler(FeedbackActivity feedbackActivity) {
            mWeakFeedbackActivity = new WeakReference<>(feedbackActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            boolean success = false;
            int errorMessage = 0;

            final FeedbackActivity feedbackActivity = mWeakFeedbackActivity.get();
            if (feedbackActivity == null) {
                return;
            }

            if (msg != null && msg.getData() != null) {
                Bundle bundle = msg.getData();
                String responseString = bundle.getString(SendFeedbackTask.BUNDLE_FEEDBACK_RESPONSE);
                String statusCode = bundle.getString(SendFeedbackTask.BUNDLE_FEEDBACK_STATUS);
                String requestType = bundle.getString(SendFeedbackTask.BUNDLE_REQUEST_TYPE);
                if ("send".equals(requestType) && (responseString == null || Integer.parseInt(statusCode) != 201)) {
                    // Send feedback went wrong if response is empty or status code != 201
                    errorMessage = R.string.hockeyapp_feedback_send_generic_error;
                } else if ("fetch".equals(requestType) && statusCode != null && (Integer.parseInt(statusCode) == 404 || Integer.parseInt(statusCode) == 422)) {
                    // Fetch feedback went wrong if status code is 404 or 422
                    feedbackActivity.resetFeedbackView();
                    success = true;
                } else if (responseString != null) {
                    feedbackActivity.startParseFeedbackTask(responseString, requestType);
                    if ("send".equals(requestType)) {

                        // Remove sent initial attachments.
                        ArrayList<Uri> attachments = feedbackActivity.mAttachmentListView.getAttachments();
                        feedbackActivity.mInitialAttachments.removeAll(attachments);

                        Toast.makeText(feedbackActivity, R.string.hockeyapp_feedback_sent_toast, Toast.LENGTH_LONG).show();
                    }
                    success = true;
                } else {
                    errorMessage = R.string.hockeyapp_feedback_send_network_error;
                }
            } else {
                errorMessage = R.string.hockeyapp_feedback_send_generic_error;
            }

            if (!success) {
                feedbackActivity.showError(errorMessage);
            }

            feedbackActivity.onSendFeedbackResult(success);
        }

    }

    private static class ParseFeedbackHandler extends Handler {

        private final WeakReference<FeedbackActivity> mWeakFeedbackActivity;

        ParseFeedbackHandler(FeedbackActivity feedbackActivity) {
            mWeakFeedbackActivity = new WeakReference<>(feedbackActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            boolean success = false;

            final FeedbackActivity feedbackActivity = mWeakFeedbackActivity.get();
            if (feedbackActivity == null) {
                return;
            }

            if (msg != null && msg.getData() != null) {
                Bundle bundle = msg.getData();
                final FeedbackResponse feedbackResponse = (FeedbackResponse) bundle.getSerializable(ParseFeedbackTask.BUNDLE_PARSE_FEEDBACK_RESPONSE);
                if (feedbackResponse != null) {
                    if (feedbackResponse.getStatus().equalsIgnoreCase("success")) {
                        /** We have a valid result from JSON parsing */
                        success = true;

                        if (feedbackResponse.getToken() != null) {
                            /** Save the Token to SharedPreferences */
                            feedbackActivity.mToken = feedbackResponse.getToken();
                            AsyncTaskUtils.execute(new AsyncTask<Void, Object, Object>() {

                                @Override
                                protected Object doInBackground(Void... voids) {
                                    PrefsUtil.getInstance().saveFeedbackTokenToPrefs(feedbackActivity, feedbackResponse.getToken());
                                    return null;
                                }
                            });
                            /** Load the existing feedback messages */
                            feedbackActivity.loadFeedbackMessages(feedbackResponse);
                            feedbackActivity.mInSendFeedback = false;
                        }
                    } else {
                        success = false;
                    }
                }
            }

            /** Something went wrong, so display an error dialog */
            if (!success) {
                feedbackActivity.showError(R.string.hockeyapp_dialog_error_message);
            }

            feedbackActivity.enableDisableSendFeedbackButton(true);
        }
    }
}
