package log;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Mio on 2015/11/10.
 */
public class LogParamsToFile {
    BufferedWriter bufferedWriter;
    File file;

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getSavedFilesDir(Context context, String dirName) {

        if (isExternalStorageWritable() == false)
            throw new RuntimeException();

        File file = new File(context.getExternalFilesDir(
                null), dirName);
        if (!file.mkdirs()) {
            Log.d("Log", "Directory exists");
        }
        Log.d("Log", file.getAbsolutePath());

        return file;
    }

    public LogParamsToFile(Context context, String fileName) {
        File dir = getSavedFilesDir(context, "BallGameDir");

        file = new File(dir, fileName);
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void write(String string) {
        try {
            bufferedWriter.write(string);
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile()
    {
        return file;
    }

    public void close()
    {
        try
        {
            bufferedWriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
