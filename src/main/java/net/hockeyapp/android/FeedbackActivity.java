package net.hockeyapp.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import net.hockeyapp.android.adapters.MessagesAdapter;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.views.FeedbackView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
 * @author Bogdan Nistor
 **/
public class FeedbackActivity extends Activity implements FeedbackActivityInterface, OnClickListener {
  /** ID of error dialog **/
  private final int DIALOG_ERROR_ID = 0;
  
  /** Reference to this **/
  private Context context;
  
  /** Widgets and layout **/
  private TextView lastUpdatedTextView;
  private EditText nameInput;
  private EditText emailInput;
  private EditText subjectInput;
  private EditText textInput;
  private Button sendFeedbackButton;
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

  /** URL for HockeyApp API **/
  private String url;
  
  /** Current error for alert dialog **/
  private ErrorObject error;
  
  /** Message data source **/ 
  private MessagesAdapter messagesAdapter;
  private ArrayList<FeedbackMessage> feedbackMessages;
  
  /** True when a message is posted **/
  private boolean inSendFeedback;
  
  /** Unique token of the message feed **/
  private String token;
  	
  /**
   * Called when the activity is starting. Sets the title and content view
   * 
   * @param savedInstanceState Data it most recently supplied in 
   *                           onSaveInstanceState(Bundle)
   */
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(getLayoutView());
    setTitle("Feedback");
    context = this;
    inSendFeedback = false;
		
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      url = extras.getString("url");
    }
		
    initFeedbackHandler();
    initParseFeedbackHandler();
    configureAppropriateView();
  }
	
  private void configureAppropriateView() {
    /** Try to retrieve the Feedback Token from {@link SharedPreferences} */
    token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this);
    if (token == null) {
      /** If Feedback Token is NULL, show the usual {@link FeedbackView} */
      configureFeedbackView(false);           
    } 
    else {
      /** If Feedback Token is NOT NULL, show the Add Response Button and fetch the feedback messages */
      configureFeedbackView(true);
      sendFetchFeedback(url, null, null, null, null, token, feedbackHandler, true);
    }
  }
  
  private void resetFeedbackView() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        PrefsUtil.getInstance().saveFeedbackTokenToPrefs(FeedbackActivity.this, null);
        configureFeedbackView(false);
      }
    });
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
            error.setMessage("Message couldn't be posted. Please check your input values and your connection, then try again.");
          }
          else if ((requestType.equals("fetch") && (statusCode != null) && ((Integer.parseInt(statusCode) == 404) || (Integer.parseInt(statusCode) == 422)))) {
            // Fetch feedback went wrong if status code is 404 or 422
            resetFeedbackView();
            success = true;
          }
          else if (responseString != null) {
            startParseFeedbackTask(responseString);
            success = true;
          }
          else {
            error.setMessage("No response from server. Please check your connection, then try again.");
          }
        }
        else {
          error.setMessage("Message couldn't be posted. Please check your input values and your connection, then try again.");
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
   * Configures the content view by initializing the input {@link EditText}s 
   * and the listener for the Send Feedback {@link Button} 
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
      } 
      else {
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
      } 
      else {
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
    				lastUpdatedTextView.setText(String.format("Last Updated: %s", formatNew.format(date)));
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
  	
  /**
   * Send feedback to HockeyApp.
   */
  @SuppressWarnings("deprecation")
  private void sendFeedback() {
  	enableDisableSendFeedbackButton(false);
  	
  	if ((nameInput.getText().toString().trim().length() <= 0) || 
  	    (emailInput.getText().toString().trim().length() <= 0) || 
  	    (subjectInput.getText().toString().trim().length() <= 0) || 
  			(textInput.getText().toString().trim().length() <= 0)) {
  		/** Not all details were submitted, we're going to display an error dialog */
  		error = new ErrorObject();
  		error.setMessage("Please provide all details");
  		
  		showDialog(DIALOG_ERROR_ID);
  		enableDisableSendFeedbackButton(true);
  	} 
  	else {
  		/** Save Name and Email to {@link SharedPreferences} */
  		PrefsUtil.getInstance().saveNameEmailSubjectToPrefs(context, nameInput.getText().toString(), emailInput.getText().toString(), subjectInput.getText().toString());
  		
  		/** Start the Send Feedback {@link AsyncTask} */
  		sendFetchFeedback(url, nameInput.getText().toString(), emailInput.getText().toString(), subjectInput.getText().toString(), textInput.getText().toString(), PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context), feedbackHandler, false);
  	}
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
  private void sendFetchFeedback(String url, String name, String email, String subject, String text, String token, Handler feedbackHandler, boolean isFetchMessages) {
    sendFeedbackTask = new SendFeedbackTask(context, url, name, email, subject, text, token, feedbackHandler, isFetchMessages);
    sendFeedbackTask.execute();
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
  	    inSendFeedback = true;
  	    break;
  			
  	  case FeedbackView.REFRESH_BUTTON_ID:
  	    sendFetchFeedback(url, null, null, null, null, PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context), feedbackHandler, true);
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
}