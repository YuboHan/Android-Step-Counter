package ca.uwaterloo.lab3_203_09;

import java.util.Arrays;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	TextView tv;

	LinearLayout l;
	SensorManager sensorManager;
	SensorEventListener eventListener;

	TextView linearAccelerationTextView;
	TextView orientationTextView;
	Sensor linearAccelerationSensor;
	Sensor accelerationSensor;
	Sensor magneticSensor;
	Sensor rotationSensor;
	LineGraphView accelerationGraph;

	MapView mapView;

	Button btnReset;
	int[] reset = { 0 };
	int[] resetMaxMin = { 0 };

	// Testing variables
	Button btnResetMaxMin;

	long timerValue;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mapView.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item)
				|| mapView.onContextItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		mapView = new MapView(getApplicationContext(), 1000, 500, 30, 30);
		registerForContextMenu(mapView);

		l = (LinearLayout) findViewById(R.id.layout1);
		l.setOrientation(LinearLayout.VERTICAL);
		accelerationGraph = new LineGraphView(getApplicationContext(), 500,
				Arrays.asList("x", "y", "z"));
		linearAccelerationTextView = new TextView(getApplicationContext());
		orientationTextView = new TextView(getApplicationContext());
		btnReset = new Button(getApplicationContext());
		btnReset.setText("Reset Button");
		btnResetMaxMin = new Button(getApplicationContext());
		btnResetMaxMin.setText("Reset MaxMin");

		NavigationalMap map = MapLoader.loadMap(getExternalFilesDir(null),
				"Lab-room-peninsula.svg");
		mapView.setMap(map);

		tv = (TextView) findViewById(R.id.label1);
		tv.setText(Environment.getExternalStorageState());
		l.addView(mapView);
		l.addView(linearAccelerationTextView);
		l.addView(orientationTextView);
		l.addView(btnReset);
		l.addView(btnResetMaxMin);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		linearAccelerationSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		magneticSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerationSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		rotationSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

		eventListener = new StepCounter(linearAccelerationTextView,
				orientationTextView, accelerationGraph, reset, resetMaxMin);

		sensorManager.registerListener(eventListener, accelerationSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(eventListener, magneticSensor,
				SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(eventListener, linearAccelerationSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(eventListener, rotationSensor,
				SensorManager.SENSOR_DELAY_FASTEST);

		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reset[0] = 0;
			}
		});

		btnResetMaxMin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetMaxMin[0] = 0;
			}
		});

	}
}