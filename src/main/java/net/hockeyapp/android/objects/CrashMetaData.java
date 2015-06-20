package net.hockeyapp.android.objects;

/**
 * This class provides properties that can be attached to a crash report via a custom alert view flow
 * @author Andreas Wörner
 */
public class CrashMetaData {
    private String userDescription;
    private String userName;
    private String userEmail;
    private String userID;

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(final String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(final String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "\n" + CrashMetaData.class.getSimpleName()
            + "\n" + "userDescription " + userDescription
            + "\n" + "userName        " + userName
            + "\n" + "userEmail       " + userEmail
            + "\n" + "userID          " + userID
            ;
    }
}
