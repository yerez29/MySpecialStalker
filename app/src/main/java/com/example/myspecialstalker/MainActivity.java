package com.example.myspecialstalker;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;


import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_CODE = 1;
    private static final String NOT_READY_MSG = "Please enter a valid Israeli phone number and a text message to send it!";
    private static final String READY_MSG = "App is ready to send SMS!";
    private static final int MIN_VALID_PHONE_NUMBER_LENGTH = 10;
    private static final int MAX_VALID_PHONE_NUMBER_LENGTH = 13;
    private static final String EMPTY_MSG = "";
    private static final String CURRENT_PHONE_NUMBER = "current phone number";
    private static final String CURRENT_TEXT_MESSAGE = "current text message";
    private static final String IS_CURRENT_PHONE_VALID = "is current phone valid";
    private static final String IS_CURRENT_TEXT_MESSAGE_VALID = "is current text message valid";
    private static final String ISRAELI_PHONE_REGEX = "^((\\+|00)?972\\-?|0)(([23489]|[57]\\d)\\-?\\d{7})$";
    private static final Pattern phonePattern = Pattern.compile(ISRAELI_PHONE_REGEX);
    private static boolean isPhoneValid = false;
    private static boolean isMessageValid = true;
    private static String currentPhoneNumber;
    private static String currentTextMessage;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.PROCESS_OUTGOING_CALLS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS,
                            Manifest.permission.SEND_SMS},
                    PERMISSIONS_CODE);
        }
        else
        {
            setContentView(R.layout.activity_main);
            afterPermissionsGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSIONS_CODE:
            {
                if (grantResults.length == 3
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                {
                    setContentView(R.layout.activity_main);
                    afterPermissionsGranted();
                }
                else
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                                    Manifest.permission.SEND_SMS}, PERMISSIONS_CODE);
                }
            }
        }
    }

    protected void afterPermissionsGranted()
    {
        final EditText phoneNumber = findViewById(R.id.phone_number);
        final EditText textMsg = findViewById(R.id.text_message);
        final TextView inst = findViewById(R.id.instructions);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        String current_phone_number = sp.getString(CURRENT_PHONE_NUMBER, EMPTY_MSG);
        String current_text_message = sp.getString(CURRENT_TEXT_MESSAGE, EMPTY_MSG);
        currentPhoneNumber = current_phone_number;
        currentTextMessage = current_text_message;
        boolean cur_phone = sp.getBoolean(IS_CURRENT_PHONE_VALID, false);
        boolean cur_msg = sp.getBoolean(IS_CURRENT_TEXT_MESSAGE_VALID, true);
        isPhoneValid = cur_phone;
        isMessageValid = cur_msg;
        phoneNumber.setText(current_phone_number);
        textMsg.setText(current_text_message);
        if (cur_phone && cur_msg)
        {
            inst.setText(READY_MSG);
        }
        else
        {
            inst.setText(NOT_READY_MSG);
        }
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (MIN_VALID_PHONE_NUMBER_LENGTH <= s.length() && s.length() <= MAX_VALID_PHONE_NUMBER_LENGTH && phonePattern.matcher(s).matches())
                {
                    isPhoneValid = true;
                    if (isMessageValid)
                    {
                        inst.setText(READY_MSG);
                    }
                    else
                    {
                        inst.setText(NOT_READY_MSG);
                    }
                }
                else
                {
                    inst.setText(NOT_READY_MSG);
                    isPhoneValid = false;
                }
                currentPhoneNumber = s.toString();
                editor.putString(CURRENT_PHONE_NUMBER, s.toString());
                editor.putBoolean(IS_CURRENT_PHONE_VALID, isPhoneValid);
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String msg = textMsg.getText().toString();
                if (msg.equals(EMPTY_MSG))
                {
                    isMessageValid = false;
                    inst.setText(NOT_READY_MSG);
                }
                else
                {
                    isMessageValid = true;
                    if (isPhoneValid)
                    {
                        inst.setText(READY_MSG);
                    }
                    else
                    {
                        inst.setText(NOT_READY_MSG);
                    }
                }
                currentTextMessage = s.toString();
                editor.putString(CURRENT_TEXT_MESSAGE, s.toString());
                editor.putBoolean(IS_CURRENT_TEXT_MESSAGE_VALID, isMessageValid);
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
    }

    protected static boolean isReadyToSend()
    {
        return isMessageValid && isPhoneValid;
    }

    protected static String getCurrentPhoneNumber()
    {
        return currentPhoneNumber;
    }

    protected static String getCurrentTextMessage()
    {
        return currentTextMessage;
    }
}
