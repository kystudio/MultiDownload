package com.kystudio.multidownload;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.kystudio.thread.MultiThreadDownload;
import com.kystudio.utils.FileUtil;

import java.io.File;

public class MainActivity extends Activity implements OnClickListener {
    protected static final String TAG = "MainActivity";
    private Button button;
    private ProgressBar pb;
    private String url = "http://gdown.baidu.com/data/wisegame/3c00add7144d3915/kugouyinle.apk";
    //	private String url = "http://t3.baidu.com/it/u=3906296129,2906209892&fm=24&gp=0.jpg";
    private MultiThreadDownload mMultiThreadDownload = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(this);
        pb = (ProgressBar) this.findViewById(R.id.pb);

        mMultiThreadDownload = new MultiThreadDownload(handler, url, FileUtil.setMkdir(this) + File.separator);
    }

    @Override
    public void onClick(View v) {
        mMultiThreadDownload.start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FileUtil.startDownloadMeg:
                    pb.setMax(mMultiThreadDownload.getFileSize());   //开始
                    break;
                case FileUtil.updateDownloadMeg:
                    if (!mMultiThreadDownload.isCompleted())   //下载
                    {
                        Log.d(TAG, "已下载：" + mMultiThreadDownload.getDownloadSize());
                        pb.setProgress(mMultiThreadDownload.getDownloadSize());
                        button.setText("下载速度：" + mMultiThreadDownload.getDownloadSpeed() + "k/秒       下载百分比" + mMultiThreadDownload.getDownloadPercent() + "%");
                    } else {
                        button.setText("下载完成");
                    }
                    break;
                case FileUtil.endDownloadMeg:
//                    Toast.makeText(MainActivity.this, "下载完成,马上安装", Toast.LENGTH_SHORT).show();
//
//				    /*apk安装界面跳转*/
//                    String filename = FileUtil.getFileName(url);
//                    String str = "/myfile/" + filename;
//                    String fileName = Environment.getExternalStorageDirectory() + str;
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
//                    startActivity(intent);

                    break;
            }
            super.handleMessage(msg);
        }
    };
}