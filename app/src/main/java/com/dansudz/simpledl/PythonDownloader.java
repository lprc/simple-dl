package com.dansudz.simpledl;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.lang.ref.WeakReference;
import java.util.Map;

import static android.content.Context.POWER_SERVICE;

public class PythonDownloader extends AsyncTask<String, Void, Void> {

    private static WeakReference<MainActivity> mActivityRef;
    public static void updateActivity(MainActivity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    private PowerManager.WakeLock wakeLock;

    public static void startDownload(String url, Map<String, Object> options) {
        Python py = Python.getInstance();
        PyObject download_prog = py.getModule("download_video");

        PyObject opts = py.getBuiltins().callAttr("dict");
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            opts.callAttr("update", new Kwarg(entry.getKey(), entry.getValue()));
        }
        download_prog.callAttr("download_youtube", url, mActivityRef.get().DOWNLOAD_LOCATION, opts); //call youtube-dl python module
    }

    @Override
    protected void onPreExecute() {
        //user_input = ((EditText) findViewById(R.id.user_url_input)).getText().toString();
    }

    protected Void doInBackground(String... params) {
        String url = params[0];

        //acquire wakelock for download
        PowerManager powerManager = (PowerManager) mActivityRef.get().getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.acquire();

        mActivityRef.get().IS_DOWNLOADER_RUNNING = 1; // downloader about to start, push download status

        // create python dict from options HashMap manually since apparently
        // chaquopy doesn't do it automatically when providing as argument
        startDownload(url, mActivityRef.get().getOptions());
        wakeLock.release();
        //realease wakelock after download has completed or has thrown an error

        //wait for ui update to catch up
        SystemClock.sleep(600);

        mActivityRef.get().notifyFinished();
        mActivityRef.get().IS_DOWNLOADER_RUNNING = 0;
        return null;
    }
}
