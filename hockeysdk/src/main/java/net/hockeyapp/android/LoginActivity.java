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
 * <p/>
 * Activity to authenticate the user.
 * <p/>
 * <h3>License</h3>
 * <p/>
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
     * URL for HockeyApp API
     */
    private String url;

    /**
     * The APP secret.
     */
    private String secret;

    /**
     * The Login Mode.
     */
    private int mode;

    /**
     * The LoginTask.
     */
    private LoginTask loginTask;

    /**
     * The Handler for the LoginTask.
     */
    private Handler loginHandler;

    /**
     * The Login button.
     */
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            url = extras.getString("url");
            secret = extras.getString("secret");
            mode = extras.getInt("mode");
        }

        configureView();
        initLoginHandler();

        @SuppressWarnings("deprecation")
        Object object = getLastNonConfigurationInstance();
        if (object != null) {
            loginTask = (LoginTask) object;
            loginTask.attach(this, loginHandler);
        }
    }

    private void configureView() {
        if (mode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {
            EditText passwordInput = (EditText) findViewById(R.id.input_password);
            passwordInput.setVisibility(View.INVISIBLE);
        }

        buttonLogin = (Button) findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAuthentication();
            }
        });
    }

    private void initLoginHandler() {
        loginHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                boolean success = bundle.getBoolean("success");

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

        if (mode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {
            ready = !TextUtils.isEmpty(email);
            params.put("email", email);
            params.put("authcode", md5(secret + email));
        } else if (mode == LoginManager.LOGIN_MODE_EMAIL_PASSWORD) {
            ready = !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password);
            params.put("email", email);
            params.put("password", password);
        }

        if (ready) {
            loginTask = new LoginTask(this, loginHandler, url, mode, params);
            AsyncTaskUtils.execute(loginTask);
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

    /**
     * Detaches the activity from the LoginTask and returns the task
     * as last instance. This way the task is restored when the activity
     * is immediately re-created.
     *
     * @return The login task if present and null otherwise.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        if (loginTask != null) {
            loginTask.detach();
        }

        return loginTask;
    }
}
