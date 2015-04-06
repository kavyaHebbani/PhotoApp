package in.tank.photoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Kavya
 */
public class ImageActivity extends Activity {

    private static String path;
    private File file1;

    private static Button shareBt;
    private static Button saveBt;
    private static EditText editBt;
    private static TextView cnt;

    private static Bitmap bmp = BitmapFactory.decodeFile("");
    private static Bitmap resize = BitmapFactory.decodeFile("");
    private static Bitmap textBM = BitmapFactory.decodeFile("");
    private static Bitmap merged = BitmapFactory.decodeFile("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // retrieve path of selected image
        Intent i = getIntent();
        path = i.getStringExtra("pic_path");

        // get bmp of selected image
        bmp = BitmapFactory.decodeFile(path);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        final EditImage editImage = new EditImage(this);
        //set right the orientation of image
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        bmp = editImage.rotateBitmap(bmp, orientation);

        // resize bitmap to match the device screen
        resize = editImage.getResizedBitmap(bmp);

        // Display image
        final ImageView img = (ImageView) findViewById(R.id.img);
        img.setImageBitmap(resize);

        saveBt = (Button) findViewById(R.id.saveBt);
        shareBt = (Button) findViewById(R.id.shareBt);
        editBt = (EditText) findViewById(R.id.textEdit);
        cnt = (TextView) findViewById(R.id.cnt);

        editBt.addTextChangedListener(mTextEditorWatcher);

        saveBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String text = editBt.getText().toString();
                        int width = resize.getWidth();
                        int height = resize.getHeight();

                        // combine text with image
                        textBM = editImage.textToDrawable(text, width, height);
                        merged = editImage.combineImages(resize, textBM, width, height);

                        img.post(new Runnable() {

                            @Override
                            public void run() {
                                img.setImageBitmap(merged);
                            }
                        });

                        editBt.post(new Runnable() {

                            @Override
                            public void run() {
                                editBt.setVisibility(View.GONE);
                            }
                        });

                        shareBt.post(new Runnable() {

                            @Override
                            public void run() {
                                shareBt.setEnabled(true);
                            }
                        });

                        saveBt.post(new Runnable() {

                            @Override
                            public void run() {
                                saveBt.setEnabled(false);
                            }
                        });

                        cnt.post(new Runnable() {

                            @Override
                            public void run() {
                                cnt.setVisibility(View.GONE);
                            }
                        });

                        saveImage();
                    }
                }).start();
            }
        });

        // share image
        shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM,
                        Uri.parse(file1.getAbsolutePath()));
                startActivity(Intent.createChooser(shareIntent,
                        "Share image via"));
            }
        });

    }

    final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            cnt.setText(String.valueOf(getResources().getInteger(
                    R.integer.max_length)
                    - s.length()));
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private void saveImage() {
        // Check for Memory Card space available
//        StatFs stat = new StatFs(Environment
//                .getExternalStorageDirectory().getPath());
//        long bytesAvailable = stat.getAvailableBlocksLong();
//        long bytesRequired = merged.getByteCount();
//        if (bytesRequired > bytesAvailable) {
//            Toast.makeText(getApplicationContext(),
//                    "No space in SD card", Toast.LENGTH_SHORT)
//                    .show();
//            return;
//        }

        String fName = "IMG_"
                + String.valueOf(System.currentTimeMillis())
                + ".jpg";

        // Save file in PhotoApp folder
        String fPath = Environment
                .getExternalStorageDirectory().toString()
                + "/PhotoApp/";
        file1 = new File(fPath);
        file1.mkdirs();
        file1 = new File(file1, fName);

        // Save file in Gallery
        File file2;
        if (path.equalsIgnoreCase(Environment
                .getExternalStorageDirectory().toString()
                + "/temp.jpg")) {
            file2 = new File(Environment
                    .getExternalStorageDirectory().toString()
                    + "/DCIM/Camera/", fName);
        } else {
            int end = path.length() - 4;
            char[] buf = new char[end];
            path.getChars(0, end, buf, 0);

            String actPath = new String(buf);
            actPath = actPath.concat("_Edit.jpg");
            file2 = new File(actPath);
        }

        // send broadcast that SD card is updated
        sendBroadcast(new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
                .fromFile(file1)));
        sendBroadcast(new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
                .fromFile(file2)));

        try {
            OutputStream fOut1 = new FileOutputStream(file1);
            merged.compress(Bitmap.CompressFormat.JPEG, 100,
                    fOut1);
            fOut1.flush();
            fOut1.close();

            OutputStream fOut2 = new FileOutputStream(file2);
            merged.compress(Bitmap.CompressFormat.JPEG, 100,
                    fOut2);
            fOut2.flush();
            fOut2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bmp.recycle();
        if (resize != null)
            resize.recycle();
        if (textBM != null)
            textBM.recycle();
        if (merged != null)
            merged.recycle();
        //editImage.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // Inflate the menu; this adds items to the action bar if it is
            // present.
            getMenuInflater().inflate(R.menu.main, menu);

            Intent mShareIntent = new Intent();
            mShareIntent.setAction(Intent.ACTION_SEND);
            mShareIntent.setType("text/plain");

            MenuItem shareItem = menu.findItem(R.id.share);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) shareItem
                    .getActionProvider();

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(mShareIntent);
            }

        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.folder:
                Intent intent = new Intent(getApplicationContext(),
                        FolderActivity.class);
                startActivity(intent);
                break;
            case R.id.rate_app:
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + appPackageName)));
                }
                break;

            case R.id.share:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);
                alertDialogBuilder.setTitle("Share");
                alertDialogBuilder
                        .setMessage(
                                "If you enjoy using Happyness Is, please share it.")
                        .setCancelable(false)
                        .setPositiveButton("Share",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        try {
                                            Intent intent = new Intent(
                                                    Intent.ACTION_VIEW);
                                            intent.setData(Uri
                                                    .parse("market://details?id=com.aspectcs.photoapp"));
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            Intent intent = new Intent(
                                                    Intent.ACTION_VIEW);
                                            intent.setData(Uri
                                                    .parse("https://play.google.com"));
                                            startActivity(intent);
                                        }
                                    }
                                })
                        .setNegativeButton("Close",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return false;
    }
}
