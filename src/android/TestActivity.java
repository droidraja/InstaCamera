package com.sphata.instacamera;

import android.Manifest;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.otaliastudios.cameraview.Flash;


import android.widget.TextView;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {


    private CameraView camera;
    private ViewGroup controlPanel;

    private boolean mCapturingPicture;
    private boolean mCapturingVideo;

    // To show stuff in the callback
    private Size mCaptureNativeSize;
    private long mCaptureTime;

    private TextView timerTextView;
    private View pictureControl, videoControl;
    private AppCompatImageButton recordButton, switchPictureButton, flashButton, toggleCamera;
    CountDownTimer timer;

    private String sessionName;

    private static final String TAG = "InstaCamera";


    private int maxVideoDuration;

    private SeekBar zoomSeekBar;
    private CountDownTimer zoomHideTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent extraData = this.getIntent();
        maxVideoDuration = extraData.getIntExtra("maxVideoDuration", 60);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(getLayoutResource("activity_test"));

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);
        zoomSeekBar = findViewById(getIDResource("seekBar"));
        camera = findViewById(getIDResource("camera"));
        camera.addCameraListener(new CameraListener() {
            public void onCameraOpened(CameraOptions options) {
            }

            public void onPictureTaken(byte[] jpeg) {
                onPicture(jpeg);
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                if(mCapturingVideo)
                    stopRecordingUI();
                onVideo(video);
            }

            @Override
            public void onZoomChanged(float newValue, float[] bounds, PointF[] fingers) {
                super.onZoomChanged(newValue, bounds, fingers);
                float percent = ((newValue - bounds[0]) / (bounds[1] - bounds[0])) * 100;
                zoomSeekBar.setProgress((int) Math.floor((double) percent));
                zoomSeekBar.setVisibility(View.VISIBLE);
                hideZoomBar();
            }
        });
        camera.mapGesture(Gesture.SCROLL_VERTICAL, GestureAction.ZOOM); // Pinch to zoom!


        findViewById(getIDResource("flash")).setOnClickListener(this);
        findViewById(getIDResource("capturePhoto")).setOnClickListener(this);
        findViewById(getIDResource("captureVideo")).setOnClickListener(this);
        

        toggleCamera = findViewById(getIDResource("toggleCamera"));
        timerTextView = findViewById(getIDResource("video_timer"));
        pictureControl = findViewById(getIDResource("pictureControl"));
        videoControl = findViewById(getIDResource("videoControl"));
        recordButton = findViewById(getIDResource("record_button"));
        flashButton = findViewById(getIDResource("flash"));


        recordButton.setOnClickListener(this);
        toggleCamera.setOnClickListener(this);
        switchPictureButton = findViewById(getIDResource("switchPicture"));
        switchPictureButton.setOnClickListener(this);

    }

    protected boolean isStorable() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void onVideo(File video) {
        mCapturingVideo = false;
        message("Video Recording Ended", false);
    }

    private void onPicture(byte[] jpeg) {
        savePicture(jpeg);
        mCapturingPicture = false;
    }

    private void message(String content, boolean important) {
        int length = important ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this, content, length).show();
    }

    private int getLayoutResource(String name) {
        String package_name = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(name, "layout", package_name);
    }

    private int getIDResource(String name) {
        String package_name = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(name, "id", package_name);
    }

    private int getDrawableResource(String name) {
        String package_name = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(name, "drawable", package_name);
    }


    @Override
    public void onClick(View view) {

        int flash_id = getIDResource("flash");
        int capturePhoto_id = getIDResource("capturePhoto");
        int captureVideo_id = getIDResource("captureVideo");
        int toggleCamera_id = getIDResource("toggleCamera");
        int recordVideo_id = getIDResource("record_button");
        int id = view.getId();

        if (flash_id == id) {
            handleFlashButton();
        }

        if (capturePhoto_id == id)
            capturePhoto();
        if (captureVideo_id == id)
            switchToVideoMode();
        if (toggleCamera_id == id)
            toggleCamera();
        if (recordVideo_id == id)
            handleRecordButton();
        if (view == switchPictureButton)
            switchToPictureMode();
    }

    private void capturePhoto() {
        if (mCapturingPicture) return;

        if (!isStorable()) return;

        mCapturingPicture = true;
        message("Capturing picture...", false);
        camera.capturePicture();
    }

    private void captureVideo() {
        if (mCapturingPicture || mCapturingVideo) return;
        switchToVideoMode();
        if (!isStorable()) return;

        mCapturingVideo = true;
        message("Recording started...", false);
        toggleCamera.setVisibility(View.GONE);
        switchPictureButton.setVisibility(View.GONE);
        recordButton.setImageResource(getDrawableResource("ic_record"));
        camera.startCapturingVideo(getFile("video"));
        startTimer(maxVideoDuration);

    }

    private void toggleCamera() {
        if (mCapturingPicture) return;
        switch (camera.toggleFacing()) {
            case BACK:
                message("Switched to back camera!", false);
                flashButton.setVisibility(View.VISIBLE);
                break;

            case FRONT:
                message("Switched to front camera!", false);
                flashButton.setVisibility(View.GONE);
                break;
        }
        setFlashButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stop();
        if (timer != null)
            timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !camera.isStarted()) {
            camera.start();
        }

    }


    protected void handleFlashButton() {
        if (camera.getSessionType() == SessionType.PICTURE) {
            switch (camera.getFlash()) {
                case ON:
                    camera.setFlash(Flash.OFF);
                    flashButton.setImageResource(getDrawableResource("ic_flash_off"));
                    message("Flash Off", false);
                    break;
                case OFF:
                    camera.set(Flash.ON);
                    flashButton.setImageResource(getDrawableResource("ic_flash_on"));
                    message("Flash On", false);
                    break;
                default:
                    camera.setFlash(Flash.ON);
            }
        } else {
            switch (camera.getFlash()) {
                case TORCH:
                    camera.setFlash(Flash.OFF);
                    flashButton.setImageResource(getDrawableResource("ic_flash_off"));
                    message("Flash Off", false);
                    break;
                case OFF:
                    camera.set(Flash.TORCH);
                    flashButton.setImageResource(getDrawableResource("ic_flash_on"));
                    message("Flash On", false);
                    break;
            }
        }

    }

    protected void startTimer(final int maxSeconds) {

        timer = new CountDownTimer(maxSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisUntilFinished = maxSeconds * 1000 - millisUntilFinished;
                long secondsUntilFinished = millisUntilFinished / 1000;
                long minutes = secondsUntilFinished / 60;
                secondsUntilFinished = secondsUntilFinished % 60;
                String finalText = String.format("%02d : %02d", minutes, secondsUntilFinished);
                timerTextView.setText(finalText);
            }

            @Override
            public void onFinish() {
                handleRecordButton();
            }


        };
        timer.start();
    }


    public File getSessionFolder() {
        Date currentTime = Calendar.getInstance().getTime();

        if (sessionName == null)
            sessionName = String.valueOf(currentTime.getTime());

        String sessionFolderName = "MGCamera/" + sessionName;
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File sessionFolder = new File(file, sessionFolderName);
        if (!sessionFolder.mkdirs())
            Log.e(TAG, "Folders Not Created");
        return sessionFolder;

    }


    protected File getFile(String type) {
        Date currentTime = Calendar.getInstance().getTime();
        String extensionType = (type.equals("picture")) ? ".jpeg" : ".mp4";
        File file = new File(getSessionFolder(), currentTime.getTime() + extensionType);
        return file;
    }

    protected void savePicture(byte[] pictureBytes) {

        File file = getFile("picture");

        try {
            OutputStream os = new FileOutputStream(file);
            os.write(pictureBytes);
            os.flush();
            os.close();
            Log.i(TAG, "Writing to image done");
        } catch (Exception e) {

            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onBackPressed() {
        File sessionFolder = getSessionFolder();

        File files[] = sessionFolder.listFiles();

        ArrayList<String> filesList = new ArrayList<String>();

        for (File file : files) {
            filesList.add(file.getAbsolutePath());
        }

        Intent intent = new Intent();
        intent.putExtra("files", filesList.toArray(new String[0]));
        setResult(RESULT_OK, intent);
        finish();
    }

    private void switchToVideoMode() {
        if (mCapturingPicture) return;
        pictureControl.setVisibility(View.GONE);
        timerTextView.setVisibility(View.VISIBLE);
        camera.setSessionType(SessionType.VIDEO);
        videoControl.setVisibility(View.VISIBLE);
        camera.setFlash(Flash.OFF);
        setFlashButton();
    }

    private void setFlashButton() {
        switch (camera.getFlash()) {
            case ON:
                flashButton.setImageResource(getDrawableResource("ic_flash_on"));
                break;
            case OFF:
                flashButton.setImageResource(getDrawableResource("ic_flash_off"));
                break;
            case TORCH:
                flashButton.setImageResource(getDrawableResource("ic_flash_on"));
                break;
        }
    }

    private void switchToPictureMode() {
        if (mCapturingVideo) return;
        pictureControl.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.GONE);
        camera.setSessionType(SessionType.PICTURE);
        videoControl.setVisibility(View.GONE);
        setFlashButton();
    }


    private void handleRecordButton() {

        if (mCapturingVideo) {
            stopRecordingUI();
            camera.stopCapturingVideo();
        } else {
            captureVideo();
        }

    }

    private void stopRecordingUI(){
        mCapturingVideo = false;
        recordButton.setImageResource(getDrawableResource("capture"));
        timerTextView.setText("00 : 00");
        timer.cancel();
        toggleCamera.setVisibility(View.VISIBLE);
        switchPictureButton.setVisibility(View.VISIBLE);
    }



    private void hideZoomBar() {
        if (zoomHideTimer != null)
            zoomHideTimer.cancel();
        zoomHideTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                zoomSeekBar.setVisibility(View.INVISIBLE);
            }
        };
        zoomHideTimer.start();
    }

}
