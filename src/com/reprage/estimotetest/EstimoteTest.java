/*
 * Copyright (c) Clinton Freeman 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.reprage.estimotetest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;

public class EstimoteTest extends Activity
{
	private static final String TAG = EstimoteTest.class.getSimpleName();
	private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
	private BeaconManager beaconManager;

	private double lower = 0.9;
	private double upper = 1.1;
	private long startTime = 0L;
	private String estimote = "F7:0C:99:01:CC:FF";

	private TextView currentRange;
	private TextView targetRange;
	private TextView elapsedTime;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final Context text = this;
		currentRange = (TextView) findViewById(R.id.currentRange);
		targetRange = (TextView) findViewById(R.id.targetRange);
		elapsedTime = (TextView) findViewById(R.id.elapsedTime);

		targetRange.setText("Target Range: " + lower + "m to " + upper + "m");

		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
				for (Beacon b : rangedBeacons) {

					if (b.getMacAddress().equals(estimote)) {
						final double dist = Utils.computeAccuracy(b);
						// Update the UI with the current distance.

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (dist > lower && dist < upper && startTime != 0L) {
									stopTimer(SystemClock.uptimeMillis());									
								}

								updateRange(dist);
							}
						});
					}
				}
			}
		});
	}

	private void stopTimer(long stopTime) {
		double t = (stopTime - startTime) / 1000.0;
		startTime = 0L;
		elapsedTime.setText("Elapsed Time: " + t + "s");
	}

	private void updateRange(double distance) {
		currentRange.setText("Current Range: " + distance);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// TODO: Test for BLE.
		// TODO: Test if Bluetooth is on.

		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.setForegroundScanPeriod(1000,5000);
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
				} catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});
	}

	public void startClick(View view) {
		startTime = SystemClock.uptimeMillis();
		elapsedTime.setText("measuring.");
	}

	@Override
	protected void onStop() {
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Log.e(TAG, "Error while stopping ranging", e);
		}

		super.onStop();
	}
}
