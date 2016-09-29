package net.hockeyapp.android.utils;

import net.hockeyapp.android.objects.Feedback;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <h3>Description</h3>
 *
 * JSON parser helper class
 */
public class FeedbackParser {
    /**
     * Private constructor prevents instantiation from other classes
     */
    private FeedbackParser() {
    }

    /**
     * FeedbackParserHolder is loaded on the first execution of FeedbackParser.getInstance()
     * or the first access to FeedbackParserHolder.INSTANCE, not before.
     */
    private static class FeedbackParserHolder {
        public static final FeedbackParser INSTANCE = new FeedbackParser();
    }

    public static FeedbackParser getInstance() {
        return FeedbackParserHolder.INSTANCE;
    }

    /**
     * Parses JSON string
     *
     * @param feedbackResponseJson string with JSON response
     * @return instance of FeedbackResponse
     */
    public FeedbackResponse parseFeedbackResponse(String feedbackResponseJson) {
        FeedbackResponse feedbackResponse = null;
        Feedback feedback = null;
        if (feedbackResponseJson != null) {
            try {
                JSONObject jsonObject = new JSONObject(feedbackResponseJson);

                JSONObject feedbackObject = jsonObject.getJSONObject("feedback");
                feedback = new Feedback();

                /** Parse the Messages Array */
                JSONArray messagesArray = feedbackObject.getJSONArray("messages");
                ArrayList<FeedbackMessage> messages = null;

                FeedbackMessage feedbackMessage = null;
                if (messagesArray.length() > 0) {
                    messages = new ArrayList<FeedbackMessage>();

                    for (int i = 0; i < messagesArray.length(); i++) {
                        String subject = messagesArray.getJSONObject(i).getString("subject").toString();
                        String text = messagesArray.getJSONObject(i).getString("text").toString();
                        String oem = messagesArray.getJSONObject(i).getString("oem").toString();
                        String model = messagesArray.getJSONObject(i).getString("model").toString();
                        String osVersion = messagesArray.getJSONObject(i).getString("os_version").toString();
                        String createdAt = messagesArray.getJSONObject(i).getString("created_at").toString();
                        int id = messagesArray.getJSONObject(i).getInt("id");
                        String token = messagesArray.getJSONObject(i).getString("token").toString();
                        int via = messagesArray.getJSONObject(i).getInt("via");
                        String userString = messagesArray.getJSONObject(i).getString("user_string").toString();
                        String cleanText = messagesArray.getJSONObject(i).getString("clean_text").toString();
                        String name = messagesArray.getJSONObject(i).getString("name").toString();
                        String appId = messagesArray.getJSONObject(i).getString("app_id").toString();

                        JSONArray jsonAttachments = messagesArray.getJSONObject(i).optJSONArray("attachments");
                        List<FeedbackAttachment> feedbackAttachments = Collections.emptyList();
                        if (jsonAttachments != null) {
                            feedbackAttachments = new ArrayList<FeedbackAttachment>();

                            for (int j = 0; j < jsonAttachments.length(); j++) {
                                int attachmentId = jsonAttachments.getJSONObject(j).getInt("id");
                                int attachmentMessageId = jsonAttachments.getJSONObject(j).getInt("feedback_message_id");
                                String filename = jsonAttachments.getJSONObject(j).getString("file_name");
                                String url = jsonAttachments.getJSONObject(j).getString("url");
                                String attachmentCreatedAt = jsonAttachments.getJSONObject(j).getString("created_at");
                                String attachmentUpdatedAt = jsonAttachments.getJSONObject(j).getString("updated_at");

                                FeedbackAttachment feedbackAttachment = new FeedbackAttachment();
                                feedbackAttachment.setId(attachmentId);
                                feedbackAttachment.setMessageId(attachmentMessageId);
                                feedbackAttachment.setFilename(filename);
                                feedbackAttachment.setUrl(url);
                                feedbackAttachment.setCreatedAt(attachmentCreatedAt);
                                feedbackAttachment.setUpdatedAt(attachmentUpdatedAt);
                                feedbackAttachments.add(feedbackAttachment);
                            }
                        }

                        feedbackMessage = new FeedbackMessage();
                        feedbackMessage.setAppId(appId);
                        feedbackMessage.setCleanText(cleanText);
                        feedbackMessage.setCreatedAt(createdAt);
                        feedbackMessage.setId(id);
                        feedbackMessage.setModel(model);
                        feedbackMessage.setName(name);
                        feedbackMessage.setOem(oem);
                        feedbackMessage.setOsVersion(osVersion);
                        feedbackMessage.setSubjec(subject);
                        feedbackMessage.setText(text);
                        feedbackMessage.setToken(token);
                        feedbackMessage.setUserString(userString);
                        feedbackMessage.setVia(via);
                        feedbackMessage.setFeedbackAttachments(feedbackAttachments);
                        messages.add(feedbackMessage);
                    }
                }

                feedback.setMessages(messages);

                try {
                    feedback.setName(feedbackObject.getString("name").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    feedback.setEmail(feedbackObject.getString("email").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    feedback.setId(feedbackObject.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    feedback.setCreatedAt(feedbackObject.getString("created_at").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                feedbackResponse = new FeedbackResponse();
                feedbackResponse.setFeedback(feedback);
                try {
                    feedbackResponse.setStatus(jsonObject.getString("status").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                try {
                    feedbackResponse.setToken(jsonObject.getString("token").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return feedbackResponse;
    }
}
