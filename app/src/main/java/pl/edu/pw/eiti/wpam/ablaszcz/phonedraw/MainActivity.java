package pl.edu.pw.eiti.wpam.ablaszcz.phonedraw;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAcc;
    private TextView textView;
    private TextView textView2;
    private TextView textView3;

    private float scale = 100000.0f;
    private boolean enableCollectingData = false;
    private float[] correct = new float[3];

    private List dataX = new ArrayList<>();
    private List dataY = new ArrayList<>();
    private List dataZ = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);

        correct[0] = 0;
        correct[1] = 0;
        correct[2] = 0;
    }

    public void draw2D(View view) {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra("draw3D", false);
        intent.putExtra("correct_x", correct[0]);
        intent.putExtra("correct_y", correct[1]);
        intent.putExtra("correct_z", correct[2]);
        startActivity(intent);
    }

    public void draw3D(View view) {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra("draw3D", true);
        intent.putExtra("correct_x", correct[0]);
        intent.putExtra("correct_y", correct[1]);
        intent.putExtra("correct_z", correct[2]);
        startActivity(intent);
    }

    public void calibrate(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog newDialog = builder.create();
        newDialog.setTitle("Calibrating. Please wait...");
        newDialog.setMessage("00:10");
        newDialog.show();

        new CountDownTimer(10000, 2) {

            @Override
            public void onTick(long millisUntilFinished) {
                enableCollectingData = true;
                int milis = (int) millisUntilFinished % 1000;
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                String stringToDispay = String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + ":" + String.format("%03d", milis);
                newDialog.setMessage("\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + stringToDispay);
            }

            private float sum(List list) {
                float sum = 0;
                for (int i=0; i < list.size(); i++) {
                    sum += (float) list.get(i);
                }
                System.out.println(sum);
                System.out.println(list.size());
                return sum;
            }

            @Override
            public void onFinish() {
                enableCollectingData = false;
                correct[0] = sum(dataX) / dataX.size();
                correct[1] = sum(dataY) / dataY.size();
                correct[2] = sum(dataZ) / dataZ.size();
                dataX = dataY = dataZ = new ArrayList<>();
                newDialog.dismiss();
            }
        }.start();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(enableCollectingData) {
            dataX.add(event.values[0]);
            dataY.add(event.values[1]);
            dataZ.add(event.values[2]);
        }

        float x = Math.round((event.values[0] - correct[0]) * scale) / scale;
        float y = Math.round((event.values[1] - correct[1]) * scale) / scale;
        float z = Math.round((event.values[2] - correct[2]) * scale) / scale;
        textView.setText(String.format("%.5f", x));
        textView2.setText(String.format("%.5f", y));
        textView3.setText(String.format("%.5f", z));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}
