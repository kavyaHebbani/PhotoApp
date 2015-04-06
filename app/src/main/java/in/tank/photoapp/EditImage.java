package in.tank.photoapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by Kavya
 */
public class EditImage {
    private Context context;
    private Bitmap resize;
    private Bitmap textBM;
    private Bitmap merged;

    EditImage(Context context){
        this.context = context;
    }

    public Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            return Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    // resize image to fit screen
    public Bitmap getResizedBitmap(Bitmap bm) {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        int newWidth = displaymetrics.widthPixels;

        int width1 = bm.getWidth();
        int height1 = bm.getHeight();
        float scale = ((float) newWidth) / width1;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        resize = Bitmap.createBitmap(bm, 0, 0, width1, height1, matrix, false);
        matrix.reset();

        return resize;
    }

    // Convert text to Bitmap
    public Bitmap textToDrawable(String text, int width, int height) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(42);
        paint.setTextAlign(Paint.Align.CENTER);

        textBM = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(textBM);

        int total = context.getResources().getInteger(R.integer.max_length);

        if (text.length() > total / 2) {
            String txt1 = String.copyValueOf(text.toCharArray(), 0, total / 2);
            canvas.drawText(txt1, width / 2, 50 * height / 100, paint);
            int length = text.length() - total / 2 - 1;

            txt1 = String
                    .copyValueOf(text.toCharArray(), total / 2 + 1, length);
            canvas.drawText(txt1, width / 2, 64 * height / 100, paint);
        } else {
            canvas.drawText(text, width / 2, 60 * height / 100, paint);
        }

        return textBM;
    }

    // combine image and text
    public Bitmap combineImages(Bitmap background, Bitmap foreground,
                                 int width, int height) {
        merged = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(merged);
        background = Bitmap.createScaledBitmap(background, width, height, true);
        comboImage.drawBitmap(background, 0, 0, null);
        comboImage.drawBitmap(foreground, 0, 0, null);
        return merged;
    }

    public void recycleBM(){
        resize.recycle();
        textBM.recycle();
        merged.recycle();
    }
}
