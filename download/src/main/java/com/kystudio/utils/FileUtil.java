package com.kystudio.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * 文件工具类
 */
public class FileUtil {
    /**
     * 开始消息提示常量
     */
    public static final int startDownloadMeg = 1;

    /**
     * 更新消息提示常量
     */
    public static final int updateDownloadMeg = 2;

    /**
     * 完成消息提示常量
     */
    public static final int endDownloadMeg = 3;

    /**
     * 检验SDcard状态
     *
     * @return boolean
     */
    public static boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 保存文件文件到目录
     *
     * @param context
     * @return 文件保存的目录
     */
    public static String setMkdir(Context context) {
        String filePath;
        if (checkSDCard()) {
            filePath = Environment.getExternalStorageDirectory().toString();
        } else {
            filePath = context.getCacheDir().getAbsolutePath();
        }
        File file = new File(filePath);
        if (!file.exists()) {
            boolean b = file.mkdirs();
            Log.d("file", "文件不存在  创建文件    " + b);
        } else {
            Log.d("file", "文件存在");
        }
        return filePath;
    }

    /**
     * 得到文件的名称
     *
     * @return
     * @throws IOException
     */
    public static String getFileName(String url) {
        String name = null;
        try {
            name = url.substring(url.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}