package in.tank.photoapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by Kavya
 */
public class OpenImage {
    private Context context;
    private String path;
    private Intent data;

    public OpenImage(Context context, int option, Intent data) {
        this.context = context;
        this.data = data;

        if (option == 1) {
            GalImage();
        } else {
            CamImage();
        }
    }

    private void GalImage() {
        Uri selImg = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(selImg,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        path = cursor.getString(columnIndex);
        displayImage();

        cursor.close();
    }

    private void CamImage() {
        File f = new File(Environment.getExternalStorageDirectory()
                .toString());
        for (File temp : f.listFiles()) {
            if (temp.getName().equals("temp.jpg")) {
                f = temp;
                break;
            }
        }
        path = f.getAbsolutePath();
        displayImage();
    }

    protected void displayImage() {
        Intent i = new Intent(context, ImageActivity.class);
        i.putExtra("pic_path", path);
        context.startActivity(i);
    }
}


