package net.hockeyapp.android.objects;

import java.io.Serializable;

/**
 * JSON helper object
 * @author Bogdan Nistor
 *
 */
public class FeedbackMessage implements Serializable {
  private static final long serialVersionUID = -8773015828853994624L;
  
  private String subject;
  private String text;
  private String oem;
  private String model;
  private String osVersion;
  private String createdAt;
  private int id;
  private String token;
  private int via;
  private String userString;
  private String cleanText;
  private String name;
  private String appId;
  
  public String getSubjec() {
    return subject;
  }
  
  public void setSubjec(String subjec) {
    this.subject = subjec;
  }
  
  public String getText() {
    return text;
  }
  
  public void setText(String text) {
    this.text = text;
  }
  
  public String getOem() {
    return oem;
  }
  
  public void setOem(String oem) {
    this.oem = oem;
  }
  
  public String getModel() {
    return model;
  }
  
  public void setModel(String model) {
    this.model = model;
  }
  
  public String getOsVersion() {
    return osVersion;
  }
  
  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }
  
  public String getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getToken() {
    return token;
  }
  
  public void setToken(String token) {
    this.token = token;
  }
  
  public int getVia() {
    return via;
  }
  
  public void setVia(int via) {
    this.via = via;
  }
  
  public String getUserString() {
    return userString;
  }
  
  public void setUserString(String userString) {
    this.userString = userString;
  }
  
  public String getCleanText() {
    return cleanText;
  }
  
  public void setCleanText(String cleanText) {
    this.cleanText = cleanText;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getAppId() {
    return appId;
  }
  
  public void setAppId(String appId) {
    this.appId = appId;
  }
}
