package net.hockeyapp.android.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <h3>Description</h3>
 *
 * Model for feedback threads.
 *
 */
public class Feedback implements Serializable {
    private static final long serialVersionUID = 2590172806951065320L;

    private String mName;
    private String mEmail;
    private int mId;
    private String mCreatedAt;
    private ArrayList<FeedbackMessage> mMessages;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.mCreatedAt = createdAt;
    }

    public ArrayList<FeedbackMessage> getMessages() {
        return mMessages;
    }

    public void setMessages(ArrayList<FeedbackMessage> messages) {
        this.mMessages = messages;
    }
}
