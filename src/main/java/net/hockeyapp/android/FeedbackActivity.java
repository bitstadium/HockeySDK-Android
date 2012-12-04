package net.hockeyapp.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import net.hockeyapp.android.adapters.MessagesAdapter;
import net.hockeyapp.android.internal.DownloadFileListener;
import net.hockeyapp.android.internal.DownloadFileTask;
import net.hockeyapp.android.internal.FeedbackMessageView;
import net.hockeyapp.android.internal.FeedbackView;
import net.hockeyapp.android.internal.SendFeedbackListener;
import net.hockeyapp.android.internal.UpdateView;
import net.hockeyapp.android.internal.VersionHelper;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.objects.Feedback;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.PrefsUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * <h4>Description</h4>
 * 
 * Activity to show the feedback form.
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2012 Codenauts UG
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
 **/
public class FeedbackActivity extends Activity implements FeedbackActivityInterface, OnClickListener {
	private final int DIALOG_ERROR_ID = 0;
	private Context context;
	private TextView lastUpdatedTextView;
	private EditText nameInput;
	private EditText emailInput;
	private EditText subjectInput;
	private EditText textInput;
	private Button sendFeedbackButton;
	private Button addResponseButton;
	//private LinearLayout wrapperLayoutFeedback;
	private ScrollView feedbackScrollView;
	private LinearLayout wrapperLayoutFeedbackAndMessages;
	//private LinearLayout wrapperLayoutActualMessages;
	private ListView messagesListView;
	
	/** Send feedback {@link AsyncTask} */
	private SendFeedbackTask sendFeedbackTask;
	/** Parse feedback {@link AsyncTask} */
	private ParseFeedbackTask parseFeedbackTask;
	private Handler feedbackHandler;
	private Handler parseFeedbackHandler;
	private ErrorObject error;
	private SendFeedbackListener sendFeedbackListener;
	private String url;
	private MessagesAdapter messagesAdapter;
	
	/**
	 * Called when the activity is starting. Sets the title and content view
	 * 
	 * @param savedInstanceState Data it most recently supplied in 
	 *                           onSaveInstanceState(Bundle)
	 */
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getLayoutView());
		setTitle("Feedback");
		context = this;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			url = extras.getString("url");
		}
		
		initFeedbackHandler();
		initParseFeedbackHandler();

		/** Try to retrieve the Feedback Token from {@link SharedPreferences} */
		String token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this);
		if (token == null) {
			/** If Feedback Token is NULL, show the usual {@link FeedbackView} */
			configureFeedbackView(false);			
		} else {
			/** If Feedback Token is NOT NULL, show the Add Response Button and fetch the feedback messages */
			configureFeedbackView(true);
			sendFetchFeedback(url, null, null, null, null, token, feedbackHandler, true);
		}
	}
	
	/**
	 * Initializes the Feedback response {@link Handler}
	 */
	private void initFeedbackHandler() {
		feedbackHandler = new Handler() {
			
			@Override
			public void handleMessage(Message msg) {
				if (msg != null && msg.getData() != null) {
					Bundle bundle = msg.getData();
					String feedbackResponseString = bundle.getString("feedback_response");
					if (feedbackResponseString != null) {
						startParseFeedbackTask(feedbackResponseString);
					}
				}
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
							}
						} else {
							success = false;
							error = new ErrorObject();
							error.setMessage("");
						}
					} else {
						success = false;
					}
				} else {
					success = false;
				}
				
				/** Something went wrong, so display an error dialog */
				if (!success) {
					runOnUiThread(new Runnable() {
						
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
	 * Configures the content view by initializing the input {@link EditText}s 
	 * and the listener for the Send Feedback {@link Button} 
	 */
	protected void configureFeedbackView(boolean haveToken) {
		feedbackScrollView = (ScrollView) findViewById(FeedbackView.FEEDBACK_SCROLLVIEW_ID);
		wrapperLayoutFeedbackAndMessages = (LinearLayout) findViewById(FeedbackView.WRAPPER_LAYOUT_FEEDBACK_AND_MESSAGES_ID);
		//wrapperLayoutActualMessages = (LinearLayout) findViewById(FeedbackView.WRAPPER_LAYOUT_ACTUAL_MESSAGES_ID);
		messagesListView = (ListView) findViewById(FeedbackView.MESSAGES_LISTVIEW_ID);
		if (haveToken) {
			/** If a token exists, the list of messages should be displayed*/
			wrapperLayoutFeedbackAndMessages.setVisibility(View.VISIBLE);
			feedbackScrollView.setVisibility(View.GONE);
			
			lastUpdatedTextView = (TextView) findViewById(FeedbackView.LAST_UPDATED_TEXT_VIEW_ID);
			addResponseButton = (Button) findViewById(FeedbackView.ADD_RESPONSE_BUTTON_ID);
			addResponseButton.setOnClickListener(this);
		} else {
			/** if the token doesn't exist, the feedback details inputs to be sent need to be displayed */ 
			wrapperLayoutFeedbackAndMessages.setVisibility(View.GONE);
			feedbackScrollView.setVisibility(View.VISIBLE);
			
			nameInput = (EditText) findViewById(FeedbackView.NAME_EDIT_TEXT_ID);
			emailInput = (EditText) findViewById(FeedbackView.EMAIL_EDIT_TEXT_ID);
			subjectInput = (EditText) findViewById(FeedbackView.SUBJECT_EDIT_TEXT_ID);
			textInput = (EditText) findViewById(FeedbackView.TEXT_EDIT_TEXT_ID);
			
			/** Check to see if the Name and Email are saved in {@link SharedPreferences} */
			String nameEmailSubject = PrefsUtil.getInstance().getNameEmailFromPrefs(context);
			if (nameEmailSubject != null) {
				/** We have Name and Email. Prepopulate the appropriate fields */
				String[] nameEmailSubjectArray = nameEmailSubject.split("\\|");
				if (nameEmailSubjectArray != null && nameEmailSubjectArray.length == 3) {
					nameInput.setText(nameEmailSubjectArray[0]);
					emailInput.setText(nameEmailSubjectArray[1]);
					subjectInput.setText(nameEmailSubjectArray[2]);
				}
			} else {
				/** We dont have Name and Email. Reset those fields */
				nameInput.setText("");
				emailInput.setText("");
				subjectInput.setText("");
			}
			
			/** Reset the remaining fields if previously populated */
			textInput.setText("");
			
			/** Check to see if the Feedback Token is availabe */
			if (PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context) != null) {
				/** If Feedback Token is available, hide the Subject Input field */
				subjectInput.setVisibility(View.GONE);
			} else {
				/** If Feedback Token is not available, display the Subject Input field */
				subjectInput.setVisibility(View.VISIBLE);
			}
			
			sendFeedbackButton = (Button) findViewById(FeedbackView.SEND_FEEDBACK_BUTTON_ID);
			sendFeedbackButton.setOnClickListener(this);
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
				
				FeedbackMessageView feedbackMessageView = null;
				Date date = null;
				if (feedbackResponse != null && feedbackResponse.getFeedback() != null && 
						feedbackResponse.getFeedback().getMessages() != null && feedbackResponse.
						getFeedback().getMessages().size() > 0) {
					
					ArrayList<FeedbackMessage> feedbackMessages = feedbackResponse.getFeedback().getMessages();
					//if (messagesAdapter == null) {
						messagesAdapter = new MessagesAdapter(context, feedbackMessages);
					/*} else {
						messagesAdapter.clear();
						
						for (FeedbackMessage message : feedbackMessages) {
							messagesAdapter.add(message);
						}
						
						messagesAdapter.notifyDataSetChanged();
					}*/
					
					messagesListView.setAdapter(messagesAdapter);
					
					/*wrapperLayoutActualMessages.removeAllViews();
					
					ArrayList<FeedbackMessage> feedbackMessages = feedbackResponse.getFeedback().getMessages();
					
					*//** Reverse the order of the feedback messages list, so we show the latest one first *//*
					Collections.reverse(feedbackMessages);
					
					*//** Set the lastUpdatedTextView text as the date of the latest feedback message *//*
					try {
						date = format.parse(feedbackMessages.get(0).getCreatedAt());
						lastUpdatedTextView.setText(String.format("Last Updated: %s", formatNew.format(date)));
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
					
					for (FeedbackMessage message : feedbackMessages) {
						feedbackMessageView = new FeedbackMessageView(context);
						feedbackMessageView.setMessageLabelText(message.getText());
						feedbackMessageView.setAuthorLabelText(message.getName());
						
						try {
							date = format.parse(message.getCreatedAt());
							feedbackMessageView.setDateLabelText(formatNew.format(date));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						feedbackMessageView.setFeedbackMessageViewBgAndTextColor(feedbackMessages.indexOf(message) % 2 == 0 ? 
								0 : 1);
						
						wrapperLayoutActualMessages.addView(feedbackMessageView);
					}*/
				}
			}
		});
	}
	
	/**
	 * Initialize the {@link SendFeedbackTask}
	 * @param url
	 * @param name
	 * @param email
	 * @param subject
	 * @param text
	 * @param token
	 * @param feedbackHandler
	 * @param isFetchMessages
	 */
	private void sendFetchFeedback(String url, String name, String email, String subject, String text, 
			String token, Handler feedbackHandler, boolean isFetchMessages) {
		
		sendFeedbackTask = new SendFeedbackTask(context, url, name, email, subject, text, token, 
				feedbackHandler, isFetchMessages);
		
		sendFeedbackTask.execute();
	}
	
	@SuppressWarnings("deprecation")
	private void sendFeedback() {
		enableDisableSendFeedbackButton(false);
		
		if (nameInput.getText().toString().trim().length() <= 0 || emailInput.getText().toString().
				trim().length() <= 0 || subjectInput.getText().toString().trim().length() <= 0 || 
				textInput.getText().toString().trim().length() <= 0) {
			
			/** Not all details were submitted, we're going to display an error dialog */
			error = new ErrorObject();
			error.setMessage("Please provide all details");
			
			showDialog(DIALOG_ERROR_ID);
			enableDisableSendFeedbackButton(true);
		} else {
			/** Save Name and Email to {@link SharedPreferences} */
			PrefsUtil.getInstance().saveNameEmailSubjectToPrefs(context, nameInput.getText().toString(), emailInput.getText().
					toString(), subjectInput.getText().toString());
			
			/** Start the Send Feedback {@link AsyncTask} */
			sendFetchFeedback(url, nameInput.getText().toString(), emailInput.getText().toString(), 
					subjectInput.getText().toString(), textInput.getText().toString(), PrefsUtil.getInstance().
					getFeedbackTokenFromPrefs(context), feedbackHandler, false);
		}
	}
	
	/**
	 * Creates and starts execution of the {@link ParseFeedbackTask}
	 * @param feedbackResponseString	JSON string response
	 */
	private void startParseFeedbackTask(String feedbackResponseString) {
		createParseFeedbackTask(feedbackResponseString);
		parseFeedbackTask.execute();
	}
	
	/**
	 * Initializes the {@link ParseFeedbackTask}
	 * @param feedbackResponseString	JSON string response
	 */
	private void createParseFeedbackTask(String feedbackResponseString) {
		parseFeedbackTask = new ParseFeedbackTask(this, feedbackResponseString, parseFeedbackHandler);
	}

	/**
	 * Enables/Disables the Send Feedback button.
	 */
	public void enableDisableSendFeedbackButton(boolean isEnable) {
		if (sendFeedbackButton != null) {
			sendFeedbackButton.setEnabled(isEnable);
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
		if (sendFeedbackTask != null) {
			sendFeedbackTask.detach();
		}
		
		return sendFeedbackTask;
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
				
			case FeedbackView.ADD_RESPONSE_BUTTON_ID:
				configureFeedbackView(false);
				
				break;
	
			default:
				break;
		}
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_ERROR_ID:
                return new AlertDialog.Builder(this)
                	.setMessage("An error has occured")
                	.setCancelable(false)
                	.setTitle("Error")
                	.setIcon(android.R.drawable.ic_dialog_alert)
                	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                    messageDialogError.setMessage("An error has occured");
                }

                break;
        }
    }
}