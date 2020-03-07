package com.dansudz.simpledl;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OptionsDialogFragment.OptionsDialogListener {
    public boolean NOTIFICATIONS_ARE_SENDING = true;
    public int apk_download_progress = 0;
    public boolean IS_APK_DOWNLOADING = false;
    public double APK_SIZE = 0.0;
    public String LATEST_APK_NAME = " ";
    public String LAST_LINE_2 = "dskaldklkty4wjk234210-";
    public String DOWNLOAD_LOCATION = "/sdcard/Download";
    public String LAST_LINE = "2;31l;ldsa--5k32k;ldsa";
    public int DOWNLOAD_LOCATION_REQUEST_CODE = 20;
    public int IS_DOWNLOADER_RUNNING = 0;
    public String user_input;
    public String user_input_for_customexecution = " ";
    public static final String CHANNEL_1_ID = "channel1";
    private int STORAGE_PERMISSION_CODE = 1;
    protected Python py;
    public AsyncTask task;
    private HashMap<String, Object> options = new HashMap<>();
    private String optionsCli = "";
    private int PROGRESS_NOTIFICATION = 1;
    private String GITHUB_URL = "https://github.com/lprc/simple-dl/releases/latest/download/app-debug.apk";
    private String UPDATE_DOWNLOAD_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.pathSeparator + "simple_dl_latest.apk";
    private ProgressDialog mProgressDialog;

    public HashMap<String, Object> getOptions() {
        return options;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();
                String path = FileUtil.getFullPathFromTreeUri(uri, this);

                System.out.println(path);
                DOWNLOAD_LOCATION = path;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        PythonDownloader.updateActivity(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); //makes keyboard not mess up UI

        createNotificationChannels(); //Sets notification channel for pushing download progress
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //final ProgressBar apkdownload_bar = (ProgressBar) findViewById(R.id.apk_download);


        final TextView textViewToChange = (TextView) findViewById(R.id.logtobedisplayed);
        textViewToChange.setText(
                "Full error log can be viewed in the downloads folder");

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        }

        /*
        final Handler download_latest_release = new Handler(); //updates the console window
        download_latest_release.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (IS_APK_DOWNLOADING) {

                        apkdownload_bar.setVisibility(View.VISIBLE);
                        apkdownload_bar.setMax(100);

                        if (APK_SIZE == 0.0) {
                            py = Python.getInstance();
                            PyObject apk_latest_size = py.getModule("download_latest_apk");
                            APK_SIZE = (double) (apk_latest_size.callAttr("return_latest_apk_size").toJava(float.class)) / (1024 * 1024); //call python module to get apk size
                        }

                        File latest_apk_file = new File("/storage/emulated/0/Download/" + LATEST_APK_NAME);
                        double CURR_APK_SIZE = (double) latest_apk_file.length() / (1024 * 1024);

                        //System.out.println(CURR_APK_SIZE);
                        //System.out.println(APK_SIZE);

                        if (APK_SIZE == 0.0) {
                            apkdownload_bar.setProgress(0);
                        } else {
                            apk_download_progress = (int) (Math.ceil((CURR_APK_SIZE / APK_SIZE) * 100));
                            apkdownload_bar.setProgress(apk_download_progress);
                        }
                        if (apk_download_progress == 100 || apk_download_progress == 99 && !IS_APK_DOWNLOADING) {
                            Toast fail_download_apk = Toast.makeText(getApplicationContext(),
                                    "Latest APK downloaded, head over to your downloads folder to install it", Toast.LENGTH_LONG);
                            fail_download_apk.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                            fail_download_apk.show();
                        }
                    } else {
                        apkdownload_bar.setVisibility(View.INVISIBLE);

                    }
                    download_latest_release.postDelayed(this, 5); // set time here to refresh textView)
                }
                catch (Exception e) {
                    IS_APK_DOWNLOADING = false;

                }
            }

        });
        */


        final Handler handler = new Handler(); //updates the console window
        handler.post(new Runnable() {
            @Override
            public void run() {

                py = Python.getInstance();
                PyObject download_prog = py.getModule("download_video");

                final TextView console_text_window = (TextView) findViewById(R.id.actualllog);
                console_text_window.setMovementMethod(new ScrollingMovementMethod());


                File log_file = new File("/storage/emulated/0/Download/logger.txt");


                if (IS_DOWNLOADER_RUNNING == 1) {
                    if (tail2(log_file, 1) != null && tail2(log_file,1) != " " && tail2(log_file,1) != "") {
                        LAST_LINE_2 = tail2(log_file, 1);
                    }

                   // System.out.println("last_line_2");
                 //   System.out.println(LAST_LINE_2);
                  //  System.out.println("Last_line");
                 //   System.out.println(LAST_LINE);
                    if (LAST_LINE!= null && LAST_LINE_2 != null && LAST_LINE != LAST_LINE_2 && LAST_LINE_2 != "" && LAST_LINE_2 != "\n") {
                        if ( LAST_LINE_2.contains(LAST_LINE) || LAST_LINE.contains(LAST_LINE_2)) {
                        }
                        else {
                            if (LAST_LINE_2 != "dskaldklkty4wjk234210-" && LAST_LINE != "2;31l;ldsa--5k32k;ldsa") {

                                console_text_window.append(LAST_LINE_2);
                                sendonChannel(LAST_LINE_2, (int)download_prog.get("progress").toFloat());


                                if (LAST_LINE_2.contains("\n")) {
                                } else {
                                    console_text_window.append("\n");
                                }
                                System.out.println(LAST_LINE_2);
                            }
                        }
                    }

                    if (tail2(log_file, 1) != null && tail2(log_file, 1) != "" && tail2(log_file,1) != " ") {
                        LAST_LINE = tail2(log_file, 1);
                    }

                }
                handler.postDelayed(this, 50); // set time here to refresh textView)
            }
        });

        /*
        Button update_app = findViewById(R.id.update_app);
        update_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast start_apk_dowload = Toast.makeText(getApplicationContext(),
                            "Downloading latest APK from Github to your downloads folder, progress bar is at the top", Toast.LENGTH_LONG);
                    start_apk_dowload.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    start_apk_dowload.show();


                    IS_APK_DOWNLOADING = true;

                    py = Python.getInstance();
                    PyObject return_latest_apk_name = py.getModule("download_latest_apk");
                    LATEST_APK_NAME = return_latest_apk_name.callAttr("return_name_latest_apk").toJava(String.class); //call python module to get apk
                    System.out.println(LATEST_APK_NAME);


                    task = new download_latest_apk().execute();
                }
                catch (Exception e) {
                    Toast fail_download_apk = Toast.makeText(getApplicationContext(),
                            "Error fetching apk \n You can: \n 1)Grant write access \n 2)Check internet connection \n 3)Check Github for notices", Toast.LENGTH_LONG);
                    fail_download_apk.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    fail_download_apk.show();
                    IS_APK_DOWNLOADING = false;
                }
            }
        });
        */

        Button Run_with_arguments = findViewById(R.id.cli_arguments);
        Run_with_arguments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    final EditText cli_text_command = new EditText(MainActivity.this);
                    cli_text_command.setHint("-v -f best https://www.examplelink.com");
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Download Arguments")
                            .setMessage("Enter download arguments just as you would on a command line, including the link.")
                            .setView(cli_text_command)
                            .setPositiveButton("Execute", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    user_input_for_customexecution = cli_text_command.getText().toString();
                                    System.out.println(user_input_for_customexecution);

                                    try {
                                        new Custom_Python_downloader().execute();
                                    }
                                    catch (Exception e) {
                                        Toast custom_args = Toast.makeText(getApplicationContext(),
                                                "Downloader encountered an issue \n 1)Check Storage Permission \n 2)Check internet connection \n 3) File a bug report", Toast.LENGTH_LONG);
                                        custom_args.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                                        custom_args.show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                } else {
                    Toast storage_toast = Toast.makeText(getApplicationContext(),
                            "You need write permission for this action", Toast.LENGTH_LONG);
                    storage_toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    storage_toast.show();
                }
            }
        });

        Button download_location = findViewById(R.id.set_directory);
        download_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose directory"), DOWNLOAD_LOCATION_REQUEST_CODE);
            }
        });

        Button video_cancel_download = findViewById(R.id.cancel_video_download);
        video_cancel_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nomanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nomanager.cancelAll();
                ProcessPhoenix.triggerRebirth(MainActivity.this); //kill's app and reloads it, canceling all downloads

            }
        });

        Button video_download_button = findViewById(R.id.download_video_button);
        video_download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this, "Starting Download...", Toast.LENGTH_SHORT).show();
                    task = new PythonDownloader().execute(((EditText) findViewById(R.id.user_url_input)).getText().toString());

                } else {
                    Toast storage_toast = Toast.makeText(getApplicationContext(),
                            "You need write permission for this action", Toast.LENGTH_LONG);
                    storage_toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    storage_toast.show();
                }
            }
        });

        Button options_button = findViewById(R.id.options_button);
        options_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create Options dialog and supply current chosen options
                // Results are processed in onDialogPositiveClick and onDialogNegativeClick
                OptionsDialogFragment dialog = new OptionsDialogFragment();

                Bundle opts = new Bundle();
                for (Map.Entry<String, Object> entry : options.entrySet()) {
                    opts.putString(entry.getKey(), entry.getValue().toString());
                }
                dialog.setArguments(opts);

                dialog.show(getSupportFragmentManager(), "optionsdialog");
            }
        });

        Button updateButton = findViewById(R.id.btnUpdate);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete old apk if exists
                File f = new File(UPDATE_DOWNLOAD_LOCATION);
                if(f.exists()) {
                    f.delete();
                }

                // instantiate it within the onCreate method
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("Downloading update...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);

                // download latest release
                final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                downloadTask.execute(GITHUB_URL);
            }
        });
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to store videos")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    public String tail2(File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler =
                    new java.io.RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength - 1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
        }
    }

    /**
     * Creates a notification channel
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel1.setDescription("Download information");
            NotificationManager manager = getSystemService(NotificationManager.class);
            channel1.enableVibration(false);
            channel1.setVibrationPattern(new long[]{0});
            manager.createNotificationChannel(channel1);
        }
    }

    /**
     * Updates the notification that shows the download progress
     * @param notificationstring content of the notification
     * @param progress current progress in percent
     */
    public void sendonChannel(String notificationstring, int progress) {
        String title = getResources().getString(R.string.notificationTitle);
        SpannableString content = new SpannableString(notificationstring);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_stat_download_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content).setBigContentTitle(title))
                .setProgress(100, progress, false)
                .setOngoing(true); // Again, THIS is the important line

        NotificationManager nomanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent appActivityIntent = new Intent(this, MainActivity.class);

        PendingIntent contentAppActivityIntent =
                PendingIntent.getActivity(
                        this,  // calling from Activity
                        0,
                        appActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(contentAppActivityIntent);

        nomanager.notify(PROGRESS_NOTIFICATION, notification.build());
    }

    /**
     * Cancels the current progress notification and sends a dismissable notification
     * that the download is finished
     */
    public void notifyFinished() {
        NotificationManager nomanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_stat_download_notification)
                .setContentTitle("Download finished")
                .setProgress(0,0,false);

        nomanager.notify(PROGRESS_NOTIFICATION, notification.build());
    }

    /**
     * Is called when options-dialog is dismissed with click on OK
     * @param dialog the dialog instance
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        this.options = ((OptionsDialogFragment)dialog).getOptions();
        this.optionsCli = ((OptionsDialogFragment)dialog).getOptionsAsCliString();
    }

    /**
     * Is called when options-dialog is dismissed with click on Cancel
     * @param dialog the dialog instance
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    public class Custom_Python_downloader extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            //final EditText cli_text_command = new EditText(MainActivity.this);
            //user_input_for_customexecution = cli_text_command.getText().toString();
        }

        protected Bitmap doInBackground(Void... params) {

            py = Python.getInstance();
            PyObject download_prog_with_args = py.getModule("download_video_with_args");

            //acquire wakelock for download
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            wakeLock.acquire();

            IS_DOWNLOADER_RUNNING = 1; // downloader about to start, push download status
            System.out.println(user_input_for_customexecution);
            download_prog_with_args.callAttr("run_custom_arguments", user_input_for_customexecution); //call youtube-dl python module


            wakeLock.release();
            //realease wakelock after download has completed or has thrown an error

            //wait for ui update to catch up
            SystemClock.sleep(600);
            notifyFinished();
            IS_DOWNLOADER_RUNNING = 0;
            return null;
        }
    }

    public class download_latest_apk extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {

        }

        protected Bitmap doInBackground(Void... params) {

            py = Python.getInstance();
            PyObject download_latest_apk = py.getModule("download_latest_apk");


            //acquire wakelock for download
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            wakeLock.acquire();

            download_latest_apk.callAttr("download_latest_apk"); //call python module to get apk

            wakeLock.release();
            //realease wakelock after download has completed or has thrown an error
            IS_APK_DOWNLOADING = false;
            return null;
        }
    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(UPDATE_DOWNLOAD_LOCATION);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else {
                // install update
                /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();

                    try {
                        int sessionId = packageInstaller.createSession(new PackageInstaller
                                .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
                        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
                        // fake intent
                        IntentSender statusReceiver = null;
                        Intent intent = new Intent(context, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                1337111117, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        session.commit(pendingIntent.getIntentSender());
                        session.close();
                    } catch (IOException e) {
                        Toast.makeText(context,"error installing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {*/
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    File file = new File(UPDATE_DOWNLOAD_LOCATION);
                    Uri apkURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                    intent.setDataAndType(apkURI, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION); // without this flag android returned a intent error!
                    getApplicationContext().startActivity(intent);
                    Toast.makeText(context,"Installing...", Toast.LENGTH_SHORT).show();
                //}
                //Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
            }
        }
    }

}



