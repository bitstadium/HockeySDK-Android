package net.hockeyapp.android.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * JSON helper object
 * @author Bogdan Nistor
 *
 */
public class Feedback implements Serializable {
  private static final long serialVersionUID = 2590172806951065320L;
  
  private String name;
  private String email;
  private int id;
  private String createdAt;
  private ArrayList<FeedbackMessage> messages;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
  
  public ArrayList<FeedbackMessage> getMessages() {
    return messages;
  }
  
  public void setMessages(ArrayList<FeedbackMessage> messages) {
    this.messages = messages;
  }
}
