/**
 * The LoginActivity class serves as a boundary interface for user authentication
 * and login-related actions within the OTA Lah app. This class provides the user
 * interface and interactions for login, password recovery, and account registration.
 * Users can log in via phone, email, and verify OTPs, reset their passwords, and
 * register new accounts using this interface.
 */
package com.project.sc2006;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.material.textfield.TextInputLayout;
import com.project.sc2006.controllers.AccountController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    protected static AccountController accountController;
    protected LoginActivity thisActivity;
    private ViewFlipper viewFlipper;

    public ViewFlipper getViewFlipper() {
        return viewFlipper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        thisActivity = this;

        accountController = new AccountController(LoginActivity.this);

        Button loginBtn = findViewById(R.id.cirLoginButton);
        TextView signUpBtn = findViewById(R.id.signup);
        TextView backLoginBtn = findViewById(R.id.backToLogin);
        TextView switchLoginMethod = findViewById(R.id.switchLogin);
        EditText userName = findViewById(R.id.editTextEmail);
        EditText password = findViewById(R.id.editTextPassword);
        TextInputLayout textInputLayout = findViewById(R.id.textInputEmail);
        viewFlipper = findViewById(R.id.viewFlipper);
        TextInputLayout registerUsername = findViewById(R.id.registerUsername);
        TextInputLayout registerEmail = findViewById(R.id.registerEmail);
        TextInputLayout registerPhone = findViewById(R.id.registerPhone);
        EditText registerUsernameEditText = findViewById(R.id.registerUsernameEditText);
        EditText registerEmailEditText = findViewById(R.id.registerEmailEditText);
        EditText registerPhoneEditText = findViewById(R.id.registerPhoneEditText);
        TextInputLayout registerPassword1 = findViewById(R.id.password1);
        TextInputLayout registerPassword2 = findViewById(R.id.password2);
        EditText registerPassword1EditText = findViewById(R.id.password1EditText);
        EditText registerPassword2EditText = findViewById(R.id.password2EditText);
        Button registerButton = findViewById(R.id.registerButton);
        TextView getAnotherOTP = findViewById(R.id.registerGetAnotherOTP);
        TextView otpPageGoBack = findViewById(R.id.otpRegsiterGoBack);
        Button verifyOTPButton = findViewById(R.id.otpRegisterOTPVerify);
        EditText otpEditText = findViewById(R.id.registerOTPTextEdit);
        TextView loginWithOTP = findViewById(R.id.loginOTP);
        TextView otpLoginPageGoBack = findViewById(R.id.otpLoginGoBack);
        TextView loginGetAnotherOTP = findViewById(R.id.loginGetAnotherOTP);
        Button loginVerifyOTPButton = findViewById(R.id.otpLoginOTPVerify);
        EditText loginOtpEditText = findViewById(R.id.loginOTPTextEdit);
        TextInputLayout resetMethodTextInput = findViewById(R.id.resetMethodTextInput);
        TextView forgotPasswordInfo = findViewById(R.id.forgotPasswordInfo);
        EditText resetMethodTextEdit = findViewById(R.id.resetMethodTextEdit);
        TextView switchResetMethod = findViewById(R.id.switchResetMethod);
        Button sendOTPResetButton = findViewById(R.id.sendOTPResetButton);
        TextView forgotPasswordGoBack = findViewById(R.id.forgotPasswordGoBack);
        TextView forgotPasswordLogin = findViewById(R.id.forgotPasswordLogin);
        EditText forgotPasswordOTPTextEdit = findViewById(R.id.forgotPasswordOTPTextEdit);
        TextView forgotPasswordGetAnotherOTP = findViewById(R.id.forgotPasswordGetAnotherOTP);
        Button forgotPasswordOTPVerify = findViewById(R.id.forgotPasswordOTPVerify);
        TextView forgotPasswordOTPGoBack = findViewById(R.id.forgotPasswordOTPGoBack);
        EditText newPasswordEditText = findViewById(R.id.newPasswordTextEdit);

        loginGetAnotherOTP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                accountController.loginGetAnotherOTP(LoginActivity.this);
            }
        });


        loginVerifyOTPButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String otp = loginOtpEditText.getText().toString();
                accountController.loginVerifyOTP(otp, LoginActivity.this);
            }
        });


        otpLoginPageGoBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewFlipper.setOutAnimation(LoginActivity.this,
                        R.anim.slide_left);
                viewFlipper.setInAnimation(LoginActivity.this,
                        R.anim.slide_right);
                viewFlipper.setDisplayedChild(0);
            }
        });

        loginWithOTP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (accountController.getLoginMethod() == AccountController.LOGIN_METHODS.LOGIN_WITH_PHONE) {
                    String phone = userName.getText().toString();
                    ;
                    accountController.loginPhoneGetOTP(phone, thisActivity);
                } else {
                    String email = userName.getText().toString();
                    ;
                    accountController.loginEmailGetOTP(email, thisActivity);
                }
            }
        });

        verifyOTPButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String otp = otpEditText.getText().toString();
                accountController.registerVerifyOTP(otp, LoginActivity.this);
            }
        });


        getAnotherOTP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                accountController.registerGetAnotherOTP(LoginActivity.this);
            }
        });

        otpPageGoBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewFlipper.setOutAnimation(LoginActivity.this,
                        R.anim.slide_left);
                viewFlipper.setInAnimation(LoginActivity.this,
                        R.anim.slide_right);
                viewFlipper.setDisplayedChild(1);
            }
        });

        registerPassword1EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String password1 = registerPassword1EditText.getText().toString();
                String password2 = registerPassword2EditText.getText().toString();
                if (password1.compareTo(password2) != 0) {
                    registerPassword1.setHint("Password Not Match!");
                    registerPassword2.setHint("Password Not Match!");
                    registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                } else {
                    if (password1.length() < 8 || !isValidPassword(password1)) {
                        registerPassword1.setHint("Weak Password!");
                        registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword2.setHint("Weak Password!");
                        registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                        return;
                    }
                    registerPassword1.setHint("Password");
                    registerPassword2.setHint("Re-enter Password");
                    registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                }
            }
        });


        registerPassword2EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String password1 = registerPassword1EditText.getText().toString();
                String password2 = registerPassword2EditText.getText().toString();
                if (password1.compareTo(password2) != 0) {
                    registerPassword1.setHint("Password Not Match!");
                    registerPassword2.setHint("Password Not Match!");
                    registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                } else {
                    if (password1.length() < 8 || !isValidPassword(password1)) {
                        registerPassword1.setHint("Weak Password!");
                        registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword2.setHint("Weak Password!");
                        registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                        return;
                    }
                    registerPassword1.setHint("Password");
                    registerPassword2.setHint("Re-enter Password");
                    registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                }
            }
        });

        registerPhoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = registerPhoneEditText.getText().toString();
                if (!isValidPhone(phone)) {
                    registerPhone.setHint("Invalid Mobile Number!");
                    registerPhone.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                } else {
                    registerPhone.setHint("Mobile Number");
                    registerPhone.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        registerEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = registerEmailEditText.getText().toString();
                if (!isValidEmail(email)) {
                    registerEmail.setHint("Invalid Email!");
                    registerEmail.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                } else {
                    registerEmail.setHint("Email");
                    registerEmail.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String userName = registerUsernameEditText.getText().toString();
                String email = registerEmailEditText.getText().toString();
                String phone = registerPhoneEditText.getText().toString();
                String password1 = registerPassword1EditText.getText().toString();
                String password2 = registerPassword2EditText.getText().toString();

                // Check valid email
                if (!isValidEmail(email)) {
                    registerEmail.setHint("Invalid Email!");
                    registerEmail.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                    return;
                } else {
                    registerEmail.setHint("Email");
                    registerEmail.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerEmail.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                }

                // Check valid phone
                if (!isValidPhone(phone)) {
                    registerPhone.setHint("Invalid Mobile Number!");
                    registerPhone.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                    return;
                } else {
                    registerPhone.setHint("Mobile Number");
                    registerPhone.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPhone.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                }

                // Check valid password
                if (password1.compareTo(password2) != 0) {
                    registerPassword1.setHint("Password Not Match!");
                    registerPassword2.setHint("Password Not Match!");
                    registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.RED));
                    registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                } else {
                    if (password1.length() < 8 || !isValidPassword(password1)) {
                        registerPassword1.setHint("Weak Password!");
                        registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword2.setHint("Weak Password!");
                        registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.RED));
                        registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                        return;
                    }
                    registerPassword1.setHint("Password");
                    registerPassword2.setHint("Re-enter Password");
                    registerPassword1.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword1.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword2.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    registerPassword2.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#1F5BF3")));
                    // password match, proceed
                    accountController.registerGetOTP(userName, email, phone, password1, LoginActivity.this);
                }
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (accountController.getLoginMethod() == AccountController.LOGIN_METHODS.LOGIN_WITH_EMAIL) {
                    // via email
                    String email = userName.getText().toString();
                    ;
                    String passwordStr = password.getText().toString();
                    ;
                    accountController.loginViaEmail(email, passwordStr, thisActivity);
                } else {
                    // via phone
                    String phone = userName.getText().toString();
                    ;
                    String passwordStr = password.getText().toString();
                    ;
                    accountController.loginViaPhone(phone, passwordStr, thisActivity);
                }
            }
        });


        switchLoginMethod.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (accountController.getLoginMethod() == AccountController.LOGIN_METHODS.LOGIN_WITH_EMAIL) {
                    // to phone
                    accountController.setLoginMethod(AccountController.LOGIN_METHODS.LOGIN_WITH_PHONE);
                    textInputLayout.setHint("Phone");
                    switchLoginMethod.setText("Login With Email");
                } else {
                    // to email
                    accountController.setLoginMethod(AccountController.LOGIN_METHODS.LOGIN_WITH_EMAIL);
                    textInputLayout.setHint("Email");
                    switchLoginMethod.setText("Login With Phone Number");
                }
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewFlipper.setOutAnimation(LoginActivity.this,
                        R.anim.slide_left);
                viewFlipper.setInAnimation(LoginActivity.this,
                        R.anim.slide_right);
                viewFlipper.setDisplayedChild(1);
            }
        });

        backLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewFlipper.setOutAnimation(LoginActivity.this,
                        R.anim.slide_left);
                viewFlipper.setInAnimation(LoginActivity.this,
                        R.anim.slide_right);
//                viewFlipper.showNext();
                viewFlipper.setDisplayedChild(0);
            }
        });

        forgotPasswordGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setOutAnimation(LoginActivity.this,
                        R.anim.slide_left);
                viewFlipper.setInAnimation(LoginActivity.this,
                        R.anim.slide_right);
                viewFlipper.setDisplayedChild(0);
            }
        });
        forgotPasswordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setOutAnimation(LoginActivity.this,
                        R.anim.slide_left);
                viewFlipper.setInAnimation(LoginActivity.this,
                        R.anim.slide_right);
                viewFlipper.setDisplayedChild(4);
            }
        });
        switchResetMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountController.getOtpMethod() == AccountController.SEND_OTP_METHODS.EMAIL_OTP) {
                    // to phone
                    accountController.setOtpMethod(AccountController.SEND_OTP_METHODS.PHONE_OTP);
                    resetMethodTextInput.setHint("Phone");
                    switchResetMethod.setText("Use Email");
                    forgotPasswordInfo.setText("Enter your phone number to reset your password.");
                } else {
                    // to email
                    accountController.setOtpMethod(AccountController.SEND_OTP_METHODS.EMAIL_OTP);
                    resetMethodTextInput.setHint("Email");
                    switchResetMethod.setText("Use Phone Number");
                    forgotPasswordInfo.setText("Enter your email address to reset your password.");
                }
            }
        });

        sendOTPResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resetMethod = resetMethodTextEdit.getText().toString().trim();
                if (accountController.getOtpMethod() == AccountController.SEND_OTP_METHODS.EMAIL_OTP) {
                    accountController.forgotPasswordEmail(resetMethod, LoginActivity.this);
                } else {
                    accountController.forgotPasswordPhone(resetMethod, LoginActivity.this);
                }
            }
        });

        forgotPasswordOTPVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = forgotPasswordOTPTextEdit.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                accountController.resetPassword(otp, newPassword, LoginActivity.this);
            }
        });

        forgotPasswordOTPGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setOutAnimation(LoginActivity.this,
                        R.anim.slide_left);
                viewFlipper.setInAnimation(LoginActivity.this,
                        R.anim.slide_right);
                viewFlipper.setDisplayedChild(0);
            }
        });

        forgotPasswordGetAnotherOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountController.forgotPasswordGetAnotherOTP(LoginActivity.this);
            }
        });
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isValidPhone(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            if (target.length() == 8 && (target.charAt(0) == '8' || target.charAt(0) == '9')) {
                try{
                    Integer.parseInt(target.toString());
                } catch (Exception e){
                    return false;
                }
                return true;
            }
            return false;
        }
    }


}