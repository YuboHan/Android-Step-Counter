package ca.uwaterloo.lab3_203_09;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class StepCounter implements SensorEventListener {
	TextView tv;
	TextView orientationTextView;
	LineGraphView graph;
	int[] reset;
	float threshold;
	float limit;
	float stepCounter;
	float localMax;
	float localMin;
	float stepDifference;
	float state;
	// 1 = going up
	// 2 = reached LocalMax, going down
	// 3 = reached LocalMin, going up
	// 4 = calculations
	// 0 = rest

	int[] resetMaxMin;
	float max;
	float min;
	long startTimer;
	long endTimer;
	float smoothedAccel;
	float[] gravity;
	float[] geomagnetic;
	float[] orientation = new float[3];
	float[] smoothedAccelFloat = new float[1];
	float[] directionArray = new float[2];

	float[] positionFromRest = new float[2];

	public StepCounter(TextView tv, TextView orientationTextView,
			LineGraphView graph, int[] reset, int[] resetMaxMin) {
		this.tv = tv;
		this.orientationTextView = orientationTextView;
		this.graph = graph;
		this.reset = reset;
		this.resetMaxMin = resetMaxMin;
	}

	private void resetState() {
		state = 0;
		localMax = localMin = 0;
	}

	private void walkingDirection(float theta, float[] direction) {
		double angle = theta;
		if (direction.length != 2) {
			return;
		}
		direction[0] = (float) Math.cos(angle);
		direction[1] = (float) Math.sin(angle);
		return;
	}

	private String directionFacing(float theta) {
		if (theta < (float) -2.618) {
			return "South";
		} else if (theta < (float) -2.094) {
			return "South-West";
		} else if (theta < (float) -1.047) {
			return "West";
		} else if (theta < (float) -0.524) {
			return "North-West";
		} else if (theta < (float) 0.524) {
			return "North";
		} else if (theta < (float) 1.047) {
			return "North-East";
		} else if (theta < (float) 2.094) {
			return "East";
		} else if (theta < (float) 2.618) {
			return "South-East";
		} else {
			return "South";
		}
	}

	@Override
	public void onAccuracyChanged(Sensor se, int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent se) {

		if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			if (reset[0] == 0) { // Set all initial variables
				stepCounter = 0;
				reset[0] = 1;
				smoothedAccel = 0;
				localMax = localMin = 0;
				threshold = 1;
				limit = 5;
				positionFromRest[0] = 0;
				positionFromRest[1] = 0;
			}
			smoothedAccel += (se.values[2] - smoothedAccel) / 5;
			smoothedAccelFloat[0] = smoothedAccel;
			if (resetMaxMin[0] == 0) { // ResetMaxMin button pressed
				max = min = 0;
				resetMaxMin[0] = 1;
			}
			if (smoothedAccel < min) { // Finding local min
				min = smoothedAccel;
			}
			if (smoothedAccel > max) { // Finding local max
				max = smoothedAccel;
			}
			if (smoothedAccel > threshold && state == 0) {
				state = 1;
				startTimer = System.currentTimeMillis();
			}
			if (state == 1) {
				if (smoothedAccel > localMax) {
					localMax = smoothedAccel;
				}
				if (smoothedAccel > limit) {
					resetState();
				} else if (smoothedAccel < 0 - threshold
						&& localMax > smoothedAccel) {
					state = 2;
				}
			}
			if (state == 2) {
				if (smoothedAccel < localMin) {
					localMin = smoothedAccel;
				}
				if (smoothedAccel < 0 - limit) {
					resetState();
				} else if (smoothedAccel > 0 - threshold
						&& localMin < smoothedAccel) {
					state = 3;
					endTimer = System.currentTimeMillis();
				}
			}
			if (state == 3) {
				state = 0;
				stepDifference = endTimer - startTimer;
				if (stepDifference > 200) {
					stepCounter++;
					positionFromRest[0] = positionFromRest[0]
							+ directionArray[0];
					positionFromRest[1] = positionFromRest[1]
							+ directionArray[1];
				}
			}
			graph.addPoint(smoothedAccelFloat);
			tv.setText(String
					.format("Steps: %.0f\nCurrent: %.2f\nMax: %.2f\nMin: %.2f\nTime: %.0f",
							stepCounter, smoothedAccel, max, min,
							stepDifference));
		}
		if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			gravity = se.values;
		}
		if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			geomagnetic = se.values;
			if (gravity != null && geomagnetic != null) {
				float R[] = new float[9];
				float I[] = new float[9];
				SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
				SensorManager.getOrientation(R, orientation);

				walkingDirection(orientation[0], directionArray);
			}
		}

		// orientationTextView.setText(String.format("x = %.2f\n y = %.2f\n",
		// directionArray[0], directionArray[1])+
		// directionFacing(orientation[0]));
		orientationTextView.setText(String.format(
				"Position from rest:\nx = %.2f m\n y = %.2f m\nDirection facing: ",
				positionFromRest[0], positionFromRest[1])
				+ directionFacing(orientation[0]));
	}
}
