package com.zl.face.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static Bitmap getAssetPic(Context mContext, String fileName) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(mContext.getAssets().open(fileName));
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveBitmapFile(Bitmap bitmap) {
        String path =  Environment.getExternalStorageDirectory()
                + File.separator
                + "com.zl.face"
                + File.separator
                + "pic.jpg";
        File dir = new File(path).getParentFile();
        if (dir.exists() && !dir.isDirectory()) {
            dir.delete();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
