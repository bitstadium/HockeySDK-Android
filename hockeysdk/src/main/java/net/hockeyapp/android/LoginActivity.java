package net.hockeyapp.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.tasks.LoginTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3>Description</h3>
 *
 * Activity to authenticate the user.
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
 * @author Patrick Eschenbach
 **/
public class LoginActivity extends Activity {

    /**
     * Parameter to supply login endpoint URL
     */
    public static final String EXTRA_URL = "url";

    /**
     * Parameter to supply the app secret for the login API
     */
    public static final String EXTRA_SECRET = "secret";

    /**
     * Parameter to define the verification mode for the login API
     */
    public static final String EXTRA_MODE = "mode";

    /**
     * URL for HockeyApp API
     */
    private String mUrl;

    /**
     * The APP mSecret.
     */
    private String mSecret;

    /**
     * The Login Mode.
     */
    private int mMode;

    /**
     * The LoginTask.
     */
    private LoginTask mLoginTask;

    /**
     * The Handler for the LoginTask.
     */
    private Handler mLoginHandler;

    /**
     * The Login button.
     */
    private Button mButtonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hockeyapp_activity_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUrl = extras.getString(EXTRA_URL);
            mSecret = extras.getString(EXTRA_SECRET);
            mMode = extras.getInt(EXTRA_MODE);
        }

        configureView();
        initLoginHandler();

        @SuppressWarnings("deprecation")
        Object object = getLastNonConfigurationInstance();
        if (object != null) {
            mLoginTask = (LoginTask) object;
            mLoginTask.attach(this, mLoginHandler);
        }
    }

    /**
     * Detaches the activity from the LoginTask and returns the task
     * as last instance. This way the task is restored when the activity
     * is immediately re-created.
     *
     * @return The login task if present and null otherwise.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        if (mLoginTask != null) {
            mLoginTask.detach();
        }

        return mLoginTask;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (LoginManager.listener != null) {
                LoginManager.listener.onBack();
            } else {
                Intent intent = new Intent(this, LoginManager.mainActivity);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(LoginManager.LOGIN_EXIT_KEY, true);
                startActivity(intent);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void configureView() {
        if (mMode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {
            EditText passwordInput = (EditText) findViewById(R.id.input_password);
            passwordInput.setVisibility(View.INVISIBLE);
        }

        TextView headlineText = (TextView) findViewById(R.id.text_headline);
        headlineText.setText(mMode == LoginManager.LOGIN_MODE_EMAIL_ONLY ? R.string.hockeyapp_login_headline_text_email_only : R.string.hockeyapp_login_headline_text);

        mButtonLogin = (Button) findViewById(R.id.button_login);
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAuthentication();
            }
        });
    }

    private void initLoginHandler() {
        mLoginHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                boolean success = bundle.getBoolean(LoginTask.BUNDLE_SUCCESS);

                if (success) {
                    finish();

                    if (LoginManager.listener != null) {
                        LoginManager.listener.onSuccess();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Check your credentials.", Toast.LENGTH_LONG)
                            .show();
                }
            }
        };
    }

    private void performAuthentication() {
        if (!Util.isConnectedToNetwork(this)) {
            Toast errorToast = Toast.makeText(this, R.string.hockeyapp_error_no_network_message, Toast.LENGTH_LONG);
            errorToast.show();
            return;
        }

        String email = ((EditText) findViewById(R.id.input_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.input_password)).getText().toString();

        boolean ready = false;
        Map<String, String> params = new HashMap<String, String>();

        if (mMode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {
            ready = !TextUtils.isEmpty(email);
            params.put("email", email);
            params.put("authcode", md5(mSecret + email));
        } else if (mMode == LoginManager.LOGIN_MODE_EMAIL_PASSWORD) {
            ready = !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password);
            params.put("email", email);
            params.put("password", password);
        }

        if (ready) {
            mLoginTask = new LoginTask(this, mLoginHandler, mUrl, mMode, params);
            AsyncTaskUtils.execute(mLoginTask);
        } else {
            Toast.makeText(this, getString(R.string.hockeyapp_login_missing_credentials_toast), Toast.LENGTH_LONG).show();
        }
    }

    public String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
