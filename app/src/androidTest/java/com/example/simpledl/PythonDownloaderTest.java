package com.example.simpledl;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import com.dansudz.simpledl.MainActivity;
import com.dansudz.simpledl.PythonDownloader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class PythonDownloaderTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK);

    @Test
    public void downloadVideo_mainActivity() {
        Assert.assertEquals(ContextCompat.checkSelfPermission(ApplicationProvider.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED);
        Assert.assertEquals(ContextCompat.checkSelfPermission(ApplicationProvider.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED);

        Map<String, Object> options = new HashMap<>();
        options.put("outtmpl", "%(title)s.%(ext)s");

        String location = ApplicationProvider.getApplicationContext().getFilesDir().getAbsolutePath();
        System.out.println("Saving to: " + location);
        File file = new File(location,"TEST VIDEO.mp4");

        PythonDownloader.updateActivity(activityRule.getActivity());
        PythonDownloader.startDownload("http://www.youtube.com/watch?v=C0DPdy98e4c", location, options);

        Assert.assertTrue(file.exists());
        file.delete();
    }
}
