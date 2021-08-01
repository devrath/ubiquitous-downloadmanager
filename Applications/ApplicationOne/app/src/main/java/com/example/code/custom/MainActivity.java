package com.example.code.custom;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.code.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("Range")
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    public static final String imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";
    String downloadPath = "";

    List<DownloadModel> downloadModels=new ArrayList<>();

    View rootView;
    Button rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setOnClickListeners();
        setFilePath();

        findViewById(R.id.chkIfFileExistsId).setOnClickListener(v -> togglePauseResume());
    }

    private void setOnClickListeners() {
        rootView = findViewById(R.id.downloadWidgetId);
        rootView = findViewById(R.id.downloadWidgetId);
    }

    private void setFilePath() {
        downloadPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
                .concat(File.separator)
                .concat("Devrath").concat(File.separator);
    }

    public class DownloadStatusTask extends AsyncTask<String,String,String> {

        DownloadModel downloadModel;
        public DownloadStatusTask(DownloadModel downloadModel){
            this.downloadModel=downloadModel;
        }

        @Override
        protected String doInBackground(String... strings) {
            downloadFileProcess(strings[0]);
            return null;
        }

        private void downloadFileProcess(String downloadId) {
            DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            boolean downloading=true;
            while (downloading){
                DownloadManager.Query query=new DownloadManager.Query();
                query.setFilterById(Long.parseLong(downloadId));
                Cursor cursor=downloadManager.query(query);
                cursor.moveToFirst();

                int bytes_downloaded=cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int total_size=cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                if(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))==DownloadManager.STATUS_SUCCESSFUL){
                    downloading=false;
                }


                int progress= (int) ((bytes_downloaded*100L)/total_size);
                String status=getStatusMessage(cursor);
                publishProgress(String.valueOf(progress), String.valueOf(bytes_downloaded),status);
                cursor.close();
            }

        }

        @Override
        protected void onProgressUpdate(final String... values) {
            super.onProgressUpdate(values);

            downloadModel.setFile_size(bytesIntoHumanReadable(Long.parseLong(values[1])));
            downloadModel.setProgress(values[0]);
            if (!downloadModel.getStatus().equalsIgnoreCase("PAUSE") && !downloadModel.getStatus().equalsIgnoreCase("RESUME")) {
                downloadModel.setStatus(values[2]);
            }
            downloadAdapter.changeItem(downloadModel.getDownloadId());

        }
    }


    private String getStatusMessage(Cursor cursor) {
        String msg="-";
        switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))){
            case DownloadManager.STATUS_FAILED:
                msg="Failed";
                break;
            case DownloadManager.STATUS_PAUSED:
                msg= "Paused";
                break;
            case DownloadManager.STATUS_RUNNING:
                msg= "Running";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                msg= "Completed";
                break;
            case DownloadManager.STATUS_PENDING:
                msg= "Pending";
                break;
            default:
                msg="Unknown";
                break;
        }
        return msg;
    }

    private void downloadFile(String url) {
        String filename= URLUtil.guessFileName(url,null,null);

        File file=new File(downloadPath,filename);

        DownloadManager.Request request=null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            request=new DownloadManager.Request(Uri.parse(url))
                    .setTitle(filename)
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(Uri.fromFile(file))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);
        }else{
            request=new DownloadManager.Request(Uri.parse(url))
                    .setTitle(filename)
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(Uri.fromFile(file))
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);
        }

        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId=downloadManager.enqueue(request);

        int nextId=1;

        final DownloadModel downloadModel=new DownloadModel();
        downloadModel.setId(nextId);
        downloadModel.setStatus("Downloading");
        downloadModel.setTitle(filename);
        downloadModel.setFile_size("0");
        downloadModel.setProgress("0");
        downloadModel.setIs_paused(false);
        downloadModel.setDownloadId(downloadId);
        downloadModel.setFile_path("");
        downloadModels.add(downloadModel);
        downloadAdapter.notifyItemInserted(downloadModels.size()-1);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(downloadModel);
            }
        });


        DownloadStatusTask downloadStatusTask=new DownloadStatusTask(downloadModel);
        runTask(downloadStatusTask,""+downloadId);


    }

    private String bytesIntoHumanReadable(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }

    private void togglePauseResume() {
        DownloadModel downloadModel = getDownloadObject();
        if(getDownloadObject().isIs_paused()){
            downloadModel.setIs_paused(false);
            downloadViewHolder.pause_resume.setText("PAUSE");
            downloadModel.setStatus("RESUME");
            downloadViewHolder.file_status.setText("Running");
            if(!resumeDownload(downloadModel)){
                Toast.makeText(context, "Failed to Resume", Toast.LENGTH_SHORT).show();
            }
            notifyItemChanged(position);
        }
        else {
            downloadModel.setIs_paused(true);
            downloadViewHolder.pause_resume.setText("RESUME");
            downloadModel.setStatus("PAUSE");
            downloadViewHolder.file_status.setText("PAUSE");
            if(!pauseDownload(downloadModel)){
                Toast.makeText(context, "Failed to Pause", Toast.LENGTH_SHORT).show();
            }
            notifyItemChanged(position);
        }
    }

    private DownloadModel getDownloadObject() {
        return downloadModels.get(0);
    }


}
