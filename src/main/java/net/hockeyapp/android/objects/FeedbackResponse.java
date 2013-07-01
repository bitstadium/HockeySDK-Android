package net.hockeyapp.android.objects;

import java.io.Serializable;

/**
 * JSON helper object
 * @author Bogdan Nistor
 *
 */
public class FeedbackResponse implements Serializable {
  private static final long serialVersionUID = -1093570359639034766L;
  
  private String status;
  private Feedback feedback;
  private String token;
  
  public String getStatus() {
    return status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public Feedback getFeedback() {
    return feedback;
  }
  
  public void setFeedback(Feedback feedback) {
    this.feedback = feedback;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
