package com.example.simpledl;

import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.dansudz.simpledl.MainActivity;
import com.dansudz.simpledl.PythonDownloader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class PythonDownloaderTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void downloadVideo_mainActivity() {
        Map<String, Object> options = new HashMap<>();
        options.put("outtmpl", "%(title)s.%(ext)s");

        String location = activityRule.getActivity().DOWNLOAD_LOCATION;
        String file = location + "/TEST VIDEO.mp4";

        PythonDownloader.updateActivity(activityRule.getActivity());
        PythonDownloader.startDownload("https://www.youtube.com/watch?v=C0DPdy98e4c", options);

        File f = new File(file);
        Assert.assertTrue(f.exists());
        f.delete();
    }
}
