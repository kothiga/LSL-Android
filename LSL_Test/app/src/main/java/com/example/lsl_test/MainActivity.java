package com.example.lsl_test;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import edu.ucsd.sccn.LSL;

public class MainActivity extends AppCompatActivity {

    //-- Set some private variables for state control.
    private boolean streaming;
    private String stream_type;
    private double cycleValue = 0.0;

    //-- Get a reference to the buttons and on screen components.
    private Button streamButton;
    private Button sineButton;
    private Button sawButton;
    private TextInputEditText addressBar;
    private TextView numberBox;
    private TextView lslClock;


    void showMessage(String str) {
        final String finalString = str;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                numberBox.setText(finalString);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-- Should realistically design around using Handler and Thread Loops.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                LSL.StreamInfo   info   = new LSL.StreamInfo("stream:o", "EEG", 1, 20, LSL.ChannelFormat.float32, "myuid4563");
                LSL.StreamOutlet outlet = null;
                try {
                    outlet = new LSL.StreamOutlet(info);
                } catch (IOException ex) {
                    showMessage("Unable to open LSL outlet. Have you added <uses-permission android:name=\\\"android.permission.INTERNET\\\" /> to your manifest file?");
                    return;
                }

                while (true) {
                    lslClock.setText(String.valueOf(LSL.local_clock()));
                    try {

                        if (streaming) {

                            //-- update the cycle value with a magic number.
                            cycleValue += 0.094;

                            //-- Set the current number according to the function type.
                            double currentNumber;
                            if (stream_type.equals("SINE")) {
                                currentNumber = Math.sin(cycleValue);
                            } else if (stream_type.equals("SAW")) {
                                currentNumber = cycleValue - Math.floor(cycleValue);
                            } else {
                                currentNumber = cycleValue;
                            }

                            showMessage(String.valueOf(currentNumber));

                            float[] sample = new float[1];
                            sample[0] = (float) currentNumber;
                            outlet.push_sample(sample);
                        }

                        //-- Sleep the current thread for a bit.
                        Thread.sleep(50);

                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(),"Exception caught in AsyncTask", Toast.LENGTH_SHORT).show();
                        showMessage(ex.getMessage());
                        outlet.close();
                        info.destroy();
                    }
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        //-- Get a reference to the text boxes.
        addressBar = findViewById(R.id.address_bar);
        numberBox  = findViewById(R.id.textView);
        lslClock   = findViewById(R.id.LslClock);

        //-- Get a reference to the buttons.
        streamButton = findViewById(R.id.button_start);
        sineButton   = findViewById(R.id.button_sine);
        sawButton    = findViewById(R.id.button_saw);


        //-- Set an initial state for the button state.
        sineButton.setBackgroundColor(Color.rgb(152,21,238));
        stream_type = "SINE";
        streaming   = false;


        //-- Set a behavior for the start button.
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //-- Flip the streaming flag.
                streaming = !streaming;

                //-- Get some text for the toast message.
                String currentText = (streaming)
                        ? getString(R.string.start_button_text_start)
                        : getString(R.string.start_button_text_stop);

                currentText = currentText.toLowerCase() + "ing stream on stream:o";
                Toast.makeText(getApplicationContext(),currentText, Toast.LENGTH_SHORT).show();

                //-- Update the button text based on flag.
                if (streaming) {

                    streamButton.setText(R.string.start_button_text_stop);

                    //-- Prevent the address bar from being edited while streaming.
                    addressBar.setFocusable(false);
                    addressBar.setClickable(false);
                    addressBar.setTextIsSelectable(false);

                } else {
                    streamButton.setText(R.string.start_button_text_start);
                    addressBar.setFocusable(true);
                    addressBar.setClickable(true);
                    addressBar.setTextIsSelectable(true);
                }
            }
        });


        //-- Set a behavior for the sine button.
        sineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentText = getString(R.string.sine_button_text);
                Toast.makeText(getApplicationContext(),currentText + " Pressed...", Toast.LENGTH_SHORT).show();

                stream_type = "SINE";
                sineButton.setBackgroundColor(Color.rgb(152,21,238));
                sawButton.setBackgroundColor(Color.LTGRAY);
            }
        });


        //-- Set a behavior for the saw button.
        sawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentText = getString(R.string.saw_button_text);
                Toast.makeText(getApplicationContext(),currentText + " Pressed...", Toast.LENGTH_SHORT).show();

                stream_type = "SAW";
                sineButton.setBackgroundColor(Color.LTGRAY);
                sawButton.setBackgroundColor(Color.rgb(152,21,238));
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}