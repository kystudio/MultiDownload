package com.kystudio.multidownload;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MultiThreadDownload";
    private EditText et_fileUrl;
    private Button btn_download;
    private ProgressBar progressBar;
    private TextView myProgress;
    private int total;
    private boolean downloading = false;
    private URL url;
    private File file;
    private List<HashMap<String, Integer>> threadList;
    private Handler handler;
    private int length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_fileUrl = (EditText) findViewById(R.id.et_fileUrl);
        et_fileUrl.setText("http://172.28.19.115:8080/MP3/a2.mp3");
        btn_download = (Button) findViewById(R.id.btn_download);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        myProgress = (TextView) findViewById(R.id.myProgress);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == 0){
                    progressBar.setProgress(message.arg1);
                    float temp = (float) progressBar.getProgress()
                            / (float) progressBar.getMax();

                    int progress = (int) (temp * 100);
                    if (progress == 100) {
                        Toast.makeText(MainActivity.this, "下载完成！", Toast.LENGTH_LONG).show();
                    }
                    myProgress.setText("下载进度:" + progress + " %");
                    //myProgress.setText("下载进度：" + message.arg1 + " %");
                    //Log.d(TAG,message.arg1 + "-" + length);
                    if (message.arg1 >= length){
                        Toast.makeText(MainActivity.this, "下载完成！", Toast.LENGTH_SHORT).show();
                        btn_download.setText("下载");
                    }
                }
                return false;
            }
        });

        threadList = new ArrayList<>();

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downloading) {
                    downloading = false;
                    btn_download.setText("下载");
                    return;
                }
                downloading = true;
                btn_download.setText("暂停");
                if (threadList.size() == 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                url = new URL(et_fileUrl.getText().toString());
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("GET");
                                conn.setConnectTimeout(5000);
                                //conn.setRequestProperty("Accept-Encoding", "identity");
                                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36");

                                length = conn.getContentLength();

                                progressBar.setMax(length);
                                progressBar.setProgress(0);

                                if (length < 0) {
                                    Toast.makeText(MainActivity.this, "文件不存在!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                file = new File(Environment.getExternalStorageDirectory(), getFileName(et_fileUrl.getText().toString()));
                                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                                randomAccessFile.setLength(length);
                                int blockSize = length / 3;
                                for (int i = 0; i < 3; i++) {
                                    int begin = i * blockSize;
                                    int end = (i + 1) * blockSize;
                                    if (i == 2) {
                                        end = length;
                                    }

                                    HashMap<String, Integer> map = new HashMap<String, Integer>();
                                    map.put("begin", begin);
                                    map.put("end", end);
                                    map.put("finished", 0);
                                    threadList.add(map);

                                    // 创建新的线程，下载文件
                                    Thread t = new Thread(new DownloadRunnable(i, begin, end, file, url));
                                    t.start();
                                }
                            } catch (MalformedURLException e) {
                                Toast.makeText(MainActivity.this, "URL Error!", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    // 恢复下载
                    for (int i = 0; i < threadList.size(); i++) {
                        HashMap<String, Integer> map = threadList.get(i);
                        int begin = map.get("begin");
                        int end = map.get("end");
                        int finished = map.get("finished");
                        Thread t = new Thread(new DownloadRunnable(i, begin + finished, end, file, url));
                        t.start();
                    }
                }
            }
        });
    }

    private String getFileName(String url) {
        int index = url.lastIndexOf("/") + 1;

        return url.substring(index);
    }

    class DownloadRunnable implements Runnable {
        private int begin;
        private int end;
        private File file;
        private URL url;
        private int id;

        public DownloadRunnable(int id, int begin, int end, File file, URL url) {
            this.id = id;
            this.begin = begin;
            this.end = end;
            this.file = file;
            this.url = url;
        }

        @Override
        public void run() {
            try {
                if (begin > end) {
                    return;
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                //conn.setConnectTimeout(5000);
                //conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36");
                conn.setRequestProperty("Range", "bytes=" + begin + "-" + end);

                InputStream is = conn.getInputStream();

                byte[] buf = new byte[1024 * 1024];
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(begin);

                int len = 0;
                HashMap<String, Integer> map = threadList.get(id);
                while ((len = is.read(buf)) != -1 && downloading) {
                    randomAccessFile.write(buf, 0, len);
                    updateProgress(len);
                    map.put("finished", map.get("finished") + len);
                    //Log.d(TAG, "Download total length:" + total);
                }
                is.close();
                randomAccessFile.close();
                conn.disconnect();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "file not found error");
                e.printStackTrace();
            } catch (IOException e1) {
                Log.d(TAG, "error");
                e1.printStackTrace();
            }
        }
    }

    synchronized private void updateProgress(int add) {
        total += add;
        handler.obtainMessage(0, total, 0).sendToTarget();
    }
}
