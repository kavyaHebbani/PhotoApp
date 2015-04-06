package in.tank.photoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;

import java.io.File;

/**
 * Created by Kavya
 */
public class MainActivity extends Activity {
    private static int REQ_LOAD_IMAGE = 1;
    private static int REQ_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button galBt = (Button) findViewById(R.id.GalleryBt);
        Button camBt = (Button) findViewById(R.id.CamBt);

        galBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQ_LOAD_IMAGE);
            }
        });

        camBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String FileName = "temp.jpg";
                String fPath = android.os.Environment
                        .getExternalStorageDirectory().toString();
                File f = new File(fPath, FileName);

                // open camera and store the file temporarily
                Intent i2 = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                i2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                i2.putExtra(MediaStore.EXTRA_MEDIA_TITLE, FileName);
                startActivityForResult(i2, REQ_CAMERA);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            new OpenImage(MainActivity.this, requestCode, data);
        } else{
            Log.e("msg", "onActivityResult: Error");
        }
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
                Intent intent = new Intent(getApplicationContext(), FolderActivity.class);
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
                                "If you enjoy using Happiness Is, please share it.")
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
        }
        return false;
    }
}