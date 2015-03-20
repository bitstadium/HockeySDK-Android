package net.hockeyapp.android.objects;

/**
 * Created by mat on 15.12.14.
 */
public enum FeedbackUserDataElement {

  DONT_SHOW(0), OPTIONAL(1), REQUIRED(2);

  private final int value;

  private FeedbackUserDataElement(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
