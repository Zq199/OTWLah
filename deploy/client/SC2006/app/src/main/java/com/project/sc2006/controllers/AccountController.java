/**
 * The AccountController class provides functionality for managing user account operations
 * and authentication within the OTA Lah app.
 */
package com.project.sc2006.controllers;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.project.sc2006.LoginActivity;
import com.project.sc2006.MainActivity;
import com.project.sc2006.R;

import org.json.JSONException;
import org.json.JSONObject;

//import java.io.IOException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AccountController {
    private String userName;
    private String email;
    private String phoneNumber;
    private String password_hashed;
    private String sessionID;
    private static int userID;

    private Context context;

    public enum LOGIN_METHODS {
        LOGIN_WITH_EMAIL,
        LOGIN_WITH_PHONE
    }

    ;

    // 0: email
    // 1: phone
    public enum SEND_OTP_METHODS {
        EMAIL_OTP,
        PHONE_OTP
    }

    ;
    private LOGIN_METHODS loginMethod = LOGIN_METHODS.LOGIN_WITH_EMAIL;
    private SEND_OTP_METHODS otpMethod = SEND_OTP_METHODS.EMAIL_OTP;

    private int status = 0;

    // 0 - waiting for input
    // Register:
    // 0 -> 1: send otp
    // 1 -> 2: otp verify success
    // Login:
    // 0 -> 3: login success
    public AccountController(Context context) {
        this.context = context;
    }

    public static int getUserID() {
        return userID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public LOGIN_METHODS getLoginMethod() {
        return loginMethod;
    }

    public SEND_OTP_METHODS getOtpMethod() {
        return otpMethod;
    }

    public void setLoginMethod(LOGIN_METHODS loginMethod) {
        this.loginMethod = loginMethod;
    }

    public void setOtpMethod(SEND_OTP_METHODS otpMethod) {
        this.otpMethod = otpMethod;
    }

    /**
     * Sends an HTTP GET request to a specified URL and handles the response using a callback.
     *
     * @param url      The URL to send the GET request to.
     * @param callback The callback to handle the response.
     */
    public void httpGETJson(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(callback);
    }

    /**
     * Logs in a user using their email address and password.
     *
     * @param email           The user's email address for login.
     * @param password        The user's password for login.
     * @param runningActivity The Activity instance where the login is initiated.
     */
    public void loginViaEmail(String email, String password, Activity runningActivity) {
        loginMethod = LOGIN_METHODS.LOGIN_WITH_EMAIL;
        String encryptedPassword = SecurityController.encryptPassword(password);
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/emailwithpassword").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("password", encryptedPassword);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // login
                                    sessionID = json.getString("session");
//                                    Toast.makeText(runningActivity, "Login Success, Session: " + sessionID, Toast.LENGTH_SHORT).show();
                                    login(runningActivity);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.statusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    if (message.contains("User Not Exist!") || message.contains("Account Not Exist!")) {
                                        accountInput.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        accountInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                    } else if (message.contains("Wrong Password!")) {
                                        passwordInput.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        passwordInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Get session Info
     *
     * @param session           The user's session for login.
     * @param runningActivity The Activity instance where the login is initiated.
     */
    public void getSessionInfo(String session, Activity runningActivity) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/sessionsinfo").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // login
                                    userName = json.getString("username");
                                    userID = json.getInt("userID");
                                    MainActivity.getPartyModeController().setUserID(userID);
//                                    Toast.makeText(runningActivity, "Get Session, Session: " + sessionID + " U " + userName + " ID " + userID, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Logs in a user using their phone number and password.
     *
     * @param phone           The user's phone number for login.
     * @param password        The user's password for login.
     * @param runningActivity The Activity instance where the login is initiated.
     */
    public void loginViaPhone(String phone, String password, Activity runningActivity) {
        loginMethod = LOGIN_METHODS.LOGIN_WITH_PHONE;
        String encryptedPassword = SecurityController.encryptPassword(password);
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/phonewithpassword").newBuilder();
        urlBuilder.addQueryParameter("phone", phone);
        urlBuilder.addQueryParameter("password", encryptedPassword);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // login
                                    sessionID = json.getString("session");
//                                    Toast.makeText(runningActivity, "Login Success", Toast.LENGTH_SHORT).show();
                                    login(runningActivity);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.statusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    if (message.contains("User Not Exist!") || message.contains("Account Not Exist!")) {
                                        accountInput.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        accountInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                    } else if (message.contains("Wrong Password!")) {
                                        passwordInput.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        passwordInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Verifies the OTP for account registration.
     *
     * @param otp             The one-time password for verification.
     * @param runningActivity The LoginActivity instance where the verification is initiated.
     */
    public void registerVerifyOTP(String otp, LoginActivity runningActivity) {
        String encryptedPassword = password_hashed;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/register/register").newBuilder();
        urlBuilder.addQueryParameter("username", userName);
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("phone", phoneNumber);
        urlBuilder.addQueryParameter("password", encryptedPassword);
        urlBuilder.addQueryParameter("otp", otp);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextView statusDisplay = runningActivity.findViewById(R.id.OTPRegisterStatusMessage);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    sessionID = json.getString("session");
//                                    Toast.makeText(runningActivity, "OTP Correct", Toast.LENGTH_SHORT).show();
                                    statusDisplay.setTextColor(runningActivity.getResources().getColor(R.color.primaryTextColor));
                                    login(runningActivity);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.statusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    statusDisplay.setTextColor(Color.RED);
                                    statusDisplay.setText(message);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Sends a request for another OTP during the registration process.
     *
     * @param runningActivity The LoginActivity instance where the request is initiated.
     */
    public void registerGetAnotherOTP(LoginActivity runningActivity) {
        String encryptedPassword = SecurityController.encryptPassword(password_hashed);
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/register/registerotp").newBuilder();
        urlBuilder.addQueryParameter("username", userName);
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("phone", phoneNumber);
        urlBuilder.addQueryParameter("password", encryptedPassword);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextInputLayout registerUsername = runningActivity.findViewById(R.id.registerUsername);
                            TextInputLayout registerEmail = runningActivity.findViewById(R.id.registerEmail);
                            TextInputLayout registerPhone = runningActivity.findViewById(R.id.registerPhone);
                            TextView statusDisplay = runningActivity.findViewById(R.id.registerStatusMessage);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    Toast.makeText(runningActivity, "OTP Sent!", Toast.LENGTH_SHORT).show();
                                    registerUsername.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerUsername.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerEmail.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerPhone.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    statusDisplay.setTextColor(runningActivity.getResources().getColor(R.color.primaryTextColor));
                                    // prompt user type in OTP:
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(2);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.statusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    if (message.contains("Account Exist!")) {
                                        registerUsername.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerUsername.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerEmail.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerPhone.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                        statusDisplay.setTextColor(Color.RED);
                                        statusDisplay.setText(message);
                                    }
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(1);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Sends a request to register a new user and obtain an OTP for verification.
     *
     * @param userName        The desired username for the new account.
     * @param email           The user's email address for registration.
     * @param phoneNumber     The user's phone number for registration.
     * @param password        The password for the new account.
     * @param runningActivity The LoginActivity instance where the registration is initiated.
     */
    public void registerGetOTP(String userName, String email, String phoneNumber, String password, LoginActivity runningActivity) {
        String encryptedPassword = SecurityController.encryptPassword(password);
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/register/registerotp").newBuilder();
        urlBuilder.addQueryParameter("username", userName);
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("phone", phoneNumber);
        urlBuilder.addQueryParameter("password", encryptedPassword);
        this.userName = userName;
        this.email = email;
        this.password_hashed = encryptedPassword;
        this.phoneNumber = phoneNumber;
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextInputLayout registerUsername = runningActivity.findViewById(R.id.registerUsername);
                            TextInputLayout registerEmail = runningActivity.findViewById(R.id.registerEmail);
                            TextInputLayout registerPhone = runningActivity.findViewById(R.id.registerPhone);
                            TextView statusDisplay = runningActivity.findViewById(R.id.registerStatusMessage);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    Toast.makeText(runningActivity, "OTP Sent!", Toast.LENGTH_SHORT).show();
                                    registerUsername.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerUsername.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerEmail.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerPhone.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    statusDisplay.setTextColor(runningActivity.getResources().getColor(R.color.primaryTextColor));
                                    // prompt user type in OTP:
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(2);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.statusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    if (message.contains("Account Exist!")) {
                                        registerUsername.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerUsername.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerEmail.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerPhone.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                        registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                        statusDisplay.setTextColor(Color.RED);
                                        statusDisplay.setText(message);
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Sends a request for OTP via phone for login verification.
     *
     * @param phoneNumber     The user's phone number for login verification.
     * @param runningActivity The LoginActivity instance where the request is initiated.
     */
    public void loginPhoneGetOTP(String phoneNumber, LoginActivity runningActivity) {
        loginMethod = LOGIN_METHODS.LOGIN_WITH_PHONE;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/phonewithotp").newBuilder();
        urlBuilder.addQueryParameter("phone", phoneNumber);
        this.phoneNumber = phoneNumber;
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextView statusDisplay = runningActivity.findViewById(R.id.statusMessage);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    Toast.makeText(runningActivity, "OTP Sent!", Toast.LENGTH_SHORT).show();
                                    statusDisplay.setTextColor(runningActivity.getResources().getColor(R.color.primaryTextColor));
                                    // prompt user type in OTP:
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(3);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.statusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    statusDisplay.setTextColor(Color.RED);
                                    statusDisplay.setText(message);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Sends a request for OTP via email for login verification.
     *
     * @param email           The user's email address for login verification.
     * @param runningActivity The LoginActivity instance where the request is initiated.
     */
    public void loginEmailGetOTP(String email, LoginActivity runningActivity) {
        loginMethod = LOGIN_METHODS.LOGIN_WITH_EMAIL;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/emailwithotp").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        this.email = email;
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextView statusDisplay = runningActivity.findViewById(R.id.statusMessage);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    Toast.makeText(runningActivity, "OTP Sent!", Toast.LENGTH_SHORT).show();
                                    statusDisplay.setTextColor(runningActivity.getResources().getColor(R.color.primaryTextColor));
                                    // prompt user type in OTP:
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(3);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.statusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    statusDisplay.setTextColor(Color.RED);
                                    statusDisplay.setText(message);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Sends a request for another OTP for login verification.
     *
     * @param runningActivity The LoginActivity instance where the request is initiated.
     */
    public void loginGetAnotherOTP(LoginActivity runningActivity) {
        HttpUrl.Builder urlBuilder;
        if (loginMethod == LOGIN_METHODS.LOGIN_WITH_EMAIL) {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/emailwithotp").newBuilder();
            urlBuilder.addQueryParameter("email", email);
        } else {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/phonewithotp").newBuilder();
            urlBuilder.addQueryParameter("phone", phoneNumber);
        }
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextView statusDisplay = runningActivity.findViewById(R.id.OTPLoginStatusMessage);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    Toast.makeText(runningActivity, "OTP Sent!", Toast.LENGTH_SHORT).show();
                                    statusDisplay.setTextColor(runningActivity.getResources().getColor(R.color.primaryTextColor));
                                    // prompt user type in OTP:
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(3);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.OTPLoginStatusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    statusDisplay.setTextColor(Color.RED);
                                    statusDisplay.setText(message);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Verifies the OTP for login purposes.
     *
     * @param otp             The one-time password for verification.
     * @param runningActivity The LoginActivity instance where the verification is initiated.
     */
    public void loginVerifyOTP(String otp, LoginActivity runningActivity) {
        HttpUrl.Builder urlBuilder;
        if (loginMethod == LOGIN_METHODS.LOGIN_WITH_EMAIL) {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/emailvalidateotp").newBuilder();
            urlBuilder.addQueryParameter("email", email);
            urlBuilder.addQueryParameter("otp", otp);
        } else {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/login/phonevalidateotp").newBuilder();
            urlBuilder.addQueryParameter("phone", phoneNumber);
            urlBuilder.addQueryParameter("otp", otp);
        }
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextView statusDisplay = runningActivity.findViewById(R.id.OTPLoginStatusMessage);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    sessionID = json.getString("session");
//                                    Toast.makeText(runningActivity, "OTP Correct", Toast.LENGTH_SHORT).show();
                                    statusDisplay.setTextColor(runningActivity.getResources().getColor(R.color.primaryTextColor));
                                    login(runningActivity);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    TextView errorDisplay = runningActivity.findViewById(R.id.OTPLoginStatusMessage);
                                    TextInputLayout accountInput = runningActivity.findViewById(R.id.textInputEmail);
                                    TextInputLayout passwordInput = runningActivity.findViewById(R.id.textInputPassword);
                                    errorDisplay.setText(message);
                                    errorDisplay.setTextColor(Color.RED);
                                    statusDisplay.setTextColor(Color.RED);
                                    statusDisplay.setText(message);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Sends a request for password recovery via email using the provided email address.
     *
     * @param email           The user's email address for recovery.
     * @param runningActivity The LoginActivity instance where the request is initiated.
     */
    public void forgotPasswordEmail(String email, LoginActivity runningActivity) {
        otpMethod = SEND_OTP_METHODS.EMAIL_OTP;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/reset/withemail").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        String url = urlBuilder.build().toString();
        this.email = email;
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextInputLayout emailInput = runningActivity.findViewById(R.id.resetMethodTextInput);
                            EditText emailText = runningActivity.findViewById(R.id.resetMethodTextEdit);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // send to OTP screen
                                    emailInput.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    emailInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    emailText.getText().clear();
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(5);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    emailInput.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                    emailInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    /**
     * Sends a request for password recovery via phone using the provided phone number.
     *
     * @param phoneNumber     The user's phone number for recovery.
     * @param runningActivity The LoginActivity instance where the request is initiated.
     */
    public void forgotPasswordPhone(String phoneNumber, LoginActivity runningActivity) {
        otpMethod = SEND_OTP_METHODS.PHONE_OTP;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/reset/withphone").newBuilder();
        urlBuilder.addQueryParameter("phone", phoneNumber);
        String url = urlBuilder.build().toString();
        this.phoneNumber = phoneNumber;
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextInputLayout phoneInput = runningActivity.findViewById(R.id.resetMethodTextInput);
                            EditText phoneText = runningActivity.findViewById(R.id.resetMethodTextEdit);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // send to OTP screen
                                    phoneInput.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    phoneInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    phoneText.getText().clear();
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(5);
                                } else {
                                    Toast.makeText(runningActivity, "Error! " + message, Toast.LENGTH_SHORT).show();
                                    phoneInput.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                    phoneInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    /**
     * Resets the password using a received OTP and a new password.
     *
     * @param otp             The one-time password for verification.
     * @param newPassword     The new password to set.
     * @param runningActivity The LoginActivity instance where the reset is initiated.
     */
    public void resetPassword(String otp, String newPassword, LoginActivity runningActivity) {
        HttpUrl.Builder urlBuilder;
        if (this.getOtpMethod() == SEND_OTP_METHODS.EMAIL_OTP) {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/reset/emailvalidate").newBuilder();
            urlBuilder.addQueryParameter("email", email);
            String password = SecurityController.encryptPassword(newPassword);
            urlBuilder.addQueryParameter("newpassword", password);
        } else {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/reset/phonevalidate").newBuilder();
            urlBuilder.addQueryParameter("phone", phoneNumber);
            String password = SecurityController.encryptPassword(newPassword);
            urlBuilder.addQueryParameter("newpassword", password);
        }
        urlBuilder.addQueryParameter("otp", otp);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            TextInputLayout otpInput = runningActivity.findViewById(R.id.forgotPasswordOTPTextLayout);
                            EditText otpText = runningActivity.findViewById(R.id.forgotPasswordOTPTextEdit);
                            EditText newPasswordText = runningActivity.findViewById(R.id.newPasswordTextEdit);
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    sessionID = json.getString("session");
                                    Toast.makeText(runningActivity, "Password Reset!", Toast.LENGTH_SHORT).show();
                                    otpInput.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    otpInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.GRAY));
                                    otpText.getText().clear();
                                    newPasswordText.getText().clear();
                                    runningActivity.getViewFlipper().setOutAnimation(runningActivity,
                                            R.anim.slide_left);
                                    runningActivity.getViewFlipper().setInAnimation(runningActivity,
                                            R.anim.slide_right);
                                    runningActivity.getViewFlipper().setDisplayedChild(0);
                                } else {
                                    otpInput.setHintTextColor(ColorStateList.valueOf(Color.RED));
                                    otpInput.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    /**
     * Sends a request for another OTP for password recovery via phone.
     *
     * @param runningActivity The LoginActivity instance where the request is initiated.
     */
    public void forgotPasswordGetAnotherOTP(LoginActivity runningActivity) {
        HttpUrl.Builder urlBuilder;
        if (otpMethod == SEND_OTP_METHODS.EMAIL_OTP) {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/reset/withemail").newBuilder();
            urlBuilder.addQueryParameter("email", email);
        } else {
            urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/reset/withphone").newBuilder();
            urlBuilder.addQueryParameter("phone", phoneNumber);
        }
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "OK!" + responseData, Toast.LENGTH_SHORT).show();
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                Toast.makeText(runningActivity, "OTP Sent!", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    public void login(Activity runningActivity){
        MainActivity.getPartyModeController().updateParticipatedParties(sessionID, runningActivity);
        getSessionInfo(sessionID, runningActivity);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("session", sessionID);
        context.startActivity(intent);
    }

}
