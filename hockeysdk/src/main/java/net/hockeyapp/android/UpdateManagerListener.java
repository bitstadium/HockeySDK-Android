package net.hockeyapp.android;

import org.json.JSONArray;

import java.util.Date;

/**
 * <h3>Description</h3>
 *
 * Abstract class for callbacks to be invoked from the UpdateManager.
 *
 **/
public abstract class UpdateManagerListener {
    /**
     * Return your own subclass of UpdateActivity for customization.
     *
     * @return subclass of UpdateActivity
     */
    public Class<? extends UpdateActivity> getUpdateActivityClass() {
        return UpdateActivity.class;
    }

    /**
     * Return your own subclass of UpdateFragment for customization.
     *
     * @return subclass of UpdateFragment
     */
    public Class<? extends UpdateFragment> getUpdateFragmentClass() {
        return UpdateFragment.class;
    }

    /**
     * Called when the update manager found no update.
     */
    public void onNoUpdateAvailable() {
        // Do nothing
    }

    /**
     * Called when the update manager found an update.
     */
    public void onUpdateAvailable() {
        // Do nothing
    }

    /**
     * Called when the user dismisses the update dialog.
     */
    public void onCancel() {
        // Do nothing
    }

    /**
     * Called when the update manager found an update.
     *
     * @param data Information about the update.
     * @param url  Link to apk file update.
     */
    public void onUpdateAvailable(JSONArray data, String url) {
        onUpdateAvailable();
    }

    /**
     * Return an expiry date for this build or null. After this date the
     * build will be blocked by a dialog.
     *
     * @return a valid date object
     */
    public Date getExpiryDate() {
        return null;
    }

    /**
     * Called when the build is expired. Return false to if you handle
     * the expiry in your code.
     *
     * @return app handles the expiration itself
     */
    public boolean onBuildExpired() {
        return true;
    }

    /**
     * To allow updates even if installed from a market, override this
     * to return true. Exercise caution with this, as some markets'
     * policies don't allow apps to update internally!
     *
     * @return app can be updated when installed from market
     */
    public boolean canUpdateInMarket() {
        return false;
    }

    /**
     * Called when the update permissions had not been granted.
     * Implement your custom action to override the default behavior.
     */
    public void onUpdatePermissionsNotGranted() {
        // Do nothing
    }

}
  
