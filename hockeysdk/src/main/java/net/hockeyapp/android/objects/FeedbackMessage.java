package net.hockeyapp.android.objects;

import java.io.Serializable;
import java.util.List;

/**
 * <h3>Description</h3>
 *
 * Model for feedback messages.
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
 */
public class FeedbackMessage implements Serializable {
    private static final long serialVersionUID = -8773015828853994624L;

    private String mSubject;
    private String mText;
    private String mDeviceOem;
    private String mDeviceModel;
    private String mDeviceOsVersion;
    private String mCreatedAt;
    private int mId;
    private String mToken;
    private int mVia;
    private String mUserString;
    private String mCleanText;
    private String mName;
    private String mAppId;
    private List<FeedbackAttachment> mFeedbackAttachments;

    /**
     * @return
     * @deprecated as of 3.7.0, replaced by {@link #getSubject()}
     */
    @Deprecated
    @SuppressWarnings("unused")
    public String getSubjec() {
        return mSubject;
    }

    /**
     * @param subjec
     * @deprecated as of 3.7.0, replaced by {@link #setSubject(String)}
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void setSubjec(String subjec) {
        this.mSubject = subjec;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subjec) {
        this.mSubject = subjec;
    }


    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public String getOem() {
        return mDeviceOem;
    }

    public void setOem(String oem) {
        this.mDeviceOem = oem;
    }

    public String getModel() {
        return mDeviceModel;
    }

    public void setModel(String model) {
        this.mDeviceModel = model;
    }

    public String getOsVersion() {
        return mDeviceOsVersion;
    }

    public void setOsVersion(String osVersion) {
        this.mDeviceOsVersion = osVersion;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.mCreatedAt = createdAt;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public int getVia() {
        return mVia;
    }

    public void setVia(int via) {
        this.mVia = via;
    }

    public String getUserString() {
        return mUserString;
    }

    public void setUserString(String userString) {
        this.mUserString = userString;
    }

    public String getCleanText() {
        return mCleanText;
    }

    public void setCleanText(String cleanText) {
        this.mCleanText = cleanText;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAppId() {
        return mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public List<FeedbackAttachment> getFeedbackAttachments() {
        return mFeedbackAttachments;
    }

    public void setFeedbackAttachments(List<FeedbackAttachment> feedbackAttachments) {
        this.mFeedbackAttachments = feedbackAttachments;
    }
}
