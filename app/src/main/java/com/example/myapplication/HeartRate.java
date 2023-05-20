package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.CountDownTimer;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class HeartRate {

    Context context;
    private TextView heartRateResult;
    private TextView timerView;
    private CountDownTimer countDownTimer;
    private final int timePeriod = 50000;
    private final int timeInterval = 50;
    private final int initialWaitTime = 5000;
    private int timerValue;
    private int count = 0;
    private int detectedDips = 0;
    private final CopyOnWriteArrayList<Long> valleys = new CopyOnWriteArrayList<>();
    private HeartRateService heartRateService;

    HeartRate(Context context, TextView resultView, TextView timerText) {
        this.context = context;
        this.heartRateResult = resultView;
        this.timerView = timerText;
    }

    void measureRate(TextureView textureView, VideoService videoService) {
        heartRateService = new HeartRateService();
        detectedDips = 0;
        timerValue = (int) (timePeriod - initialWaitTime) / 1000;
        count = 0;
        countDownTimer = new CountDownTimer(timePeriod, timeInterval) {
            @Override
            public void onTick(long l) {
                count++;
                if (initialWaitTime > count * timeInterval) {
                    if ((count * timeInterval) % 1000 == 0) {
                        timerView.setText("Gently place your finger on the camera. The recording starts in " + String.valueOf((int) ((initialWaitTime - count * timeInterval) / 1000)) + " sec");
                    }
                    return;
                }
                if ((count * timeInterval) % 1000 == 0) {
                    timerView.setText("Time left : " + String.valueOf(timerValue--) + " sec");
                }
                Thread thread = new Thread(() -> {
                    Bitmap bm = textureView.getBitmap();
                    int bmw = textureView.getWidth();
                    int bmh = textureView.getHeight();
                    int noOfPixels = bmw * bmh;
                    int reading = 0;
                    int[] pixels = new int[noOfPixels];

                    bm.getPixels(pixels, 0, bmw, 0, 0, bmw, bmh);
                    for (int i = 0; i < noOfPixels; i++) {
                        reading += (pixels[i] >> 16) & 0xff;
                    }
                    heartRateService.add(reading);

                    if (detectDip()) {
                        detectedDips = detectedDips + 1;
                        valleys.add(heartRateService.getLastTimestamp().getTime());
                    }
                });
                thread.start();
            }

            @Override
            public void onFinish() {
                CopyOnWriteArrayList<PixelValues<Float>> stdValues = heartRateService.getStdValues();
                if (valleys.size() == 0) {
                    System.out.println("Place the finger properly");
                    Toast.makeText(context, "Unable to measure heart rate. Place the finger properly", Toast.LENGTH_SHORT).show();
                    return;
                }
                float pulse = 60f * (detectedDips - 1) / (Math.max(1, (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f));
                System.out.println("PULSE = "+pulse);
                CovidSymptomsMainPage.symptoms.heartRate = pulse;
                videoService.stopVideo();
                CovidSymptomsMainPage.heartRate.stop();
                heartRateResult.setText("Heart rate :"+Float.toString(pulse));
                timerView.setText("");
            }
        };
        countDownTimer.start();
            }

    private boolean detectDip() {
        final int windowSize = 13;
        CopyOnWriteArrayList<PixelValues<Integer>> subList = heartRateService.getLastStdValues(windowSize);
        if (subList.size() < windowSize) {
            return false;
        } else {
            Integer referenceValue = subList.get((int) Math.ceil(windowSize / 2f)).reading;

            for (PixelValues<Integer> measurement : subList) {
                if (measurement.reading < referenceValue) return false;
            }

            return (!subList.get((int) Math.ceil(windowSize / 2f)).reading.equals(
                    subList.get((int) Math.ceil(windowSize / 2f) - 1).reading));
        }
    }

    void stop() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
class VideoService {

    private final Context context;
    private String id;
    private CameraDevice camDev;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder requestBuilder;

    VideoService(Context context) {
        this.context = context;
    }

    void startVideo(Surface surface) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            id = Objects.requireNonNull(cameraManager).getCameraIdList()[0];
        } catch (CameraAccessException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            Log.e("Camera", "No access to the camera", e);
            Toast.makeText(context, "No access to the camera", Toast.LENGTH_SHORT).show();
        }

        try {
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "No permission to access camera", Toast.LENGTH_SHORT).show();
                return;
            }
            if(id == null) {
                return;
            }

            Objects.requireNonNull(cameraManager).openCamera(id, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camDevice) {
                    camDev = camDevice;

                    CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {

                                requestBuilder = camDev.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                requestBuilder.addTarget(surface);
                                requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);

                                HandlerThread thread = new HandlerThread("CameraView");
                                thread.start();

                                captureSession.setRepeatingRequest(requestBuilder.build(), null, null);

                            } catch (CameraAccessException e) {
                                if (e.getMessage() != null) {
                                    Log.println(Log.ERROR, "camera", e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.println(Log.ERROR, "camera", "Failed to configure session");
                        }
                    };

                    try {
                        camDev.createCaptureSession(Collections.singletonList(surface), stateCallback, null);

                    } catch (CameraAccessException e) {
                        if (e.getMessage() != null) {
                            Log.println(Log.ERROR, "camera", e.getMessage());
                        }
                    }
                }


                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {

                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {

                }
            }, null);
        } catch (CameraAccessException | SecurityException e) {
            if (e.getMessage() != null) {
                Log.println(Log.ERROR, "camera", e.getMessage());
                Toast.makeText(context, "Camera not available", Toast.LENGTH_LONG).show();
            }
        }
    }

    void stopVideo() {
        try {
            Toast.makeText(context, "Camera recording stopped. You can now remove the finger.", Toast.LENGTH_LONG).show();
            camDev.close();
        } catch (Exception e) {
            Log.println(Log.ERROR, "camera", "Cannot close camera" + e.getMessage());
        }
    }
}

class HeartRateService {

            private final CopyOnWriteArrayList<PixelValues<Integer>> readings = new CopyOnWriteArrayList<>();
            private int minimum = 2147483647;
            private int maximum = -2147483648;
            private final int rollingAverageSize = 4;

            void add(int reading) {
                PixelValues<Integer> measurementWithDate = new PixelValues<>(new Date(), reading);
                readings.add(measurementWithDate);
                if (reading < minimum) minimum = reading;
                if (reading > maximum) maximum = reading;
            }

            CopyOnWriteArrayList<PixelValues<Float>> getStdValues() {
                CopyOnWriteArrayList<PixelValues<Float>> stdValues = new CopyOnWriteArrayList<>();

                for (int i = 0; i < readings.size(); i++) {
                    int sum = 0;
                    for (int rollingAverageCounter = 0; rollingAverageCounter < rollingAverageSize; rollingAverageCounter++) {
                        sum += readings.get(Math.max(0, i - rollingAverageCounter)).reading;
                    }

                    PixelValues<Float> stdValue =
                            new PixelValues<>(
                                    readings.get(i).timestamp,
                                    ((float)sum / rollingAverageSize - minimum ) / (maximum - minimum));
                    stdValues.add(stdValue);
                }

                return stdValues;
            }

            @SuppressWarnings("SameParameterValue")
            CopyOnWriteArrayList<PixelValues<Integer>> getLastStdValues(int count) {
                if (count < readings.size()) {
                    return  new CopyOnWriteArrayList<>(readings.subList(readings.size() - 1 - count, readings.size() - 1));
                } else {
                    return readings;
                }
            }

            Date getLastTimestamp() {
                return readings.get(readings.size() - 1).timestamp;
            }
        }

        class PixelValues<T> {

            final Date timestamp;
            final T reading;

            PixelValues(Date timestamp, T reading) {
                this.timestamp = timestamp;
                this.reading = reading;
            }

        }

