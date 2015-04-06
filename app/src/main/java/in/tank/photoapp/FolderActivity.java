package in.tank.photoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

/**
 * Created by Kavya
 */
public class FolderActivity extends Activity {
    private final Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    MyPagerAdapter myPagerAdapter;
    Cursor cursor;
    CursorLoader cursorLoader;

    int imageQuality = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        String where = "_data LIKE '"
                + Environment.getExternalStorageDirectory().toString()
                + "/PhotoApp/%" + "'";

        cursorLoader = new CursorLoader(this, sourceUri, null, where,
                null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
        cursorLoader.waitForLoader();
        cursor = cursorLoader.loadInBackground();


        myPagerAdapter = new MyPagerAdapter(this, cursor);
        ViewPager gallery = (ViewPager) findViewById(R.id.gallery);
        gallery.setAdapter(myPagerAdapter);
    }

    public class MyPagerAdapter extends PagerAdapter {
        Cursor myCursor;
        Context myContext;

        MyPagerAdapter(Context myContext, Cursor myCursor) {
            this.myContext = myContext;
            this.myCursor = myCursor;
        }

        @Override
        public int getCount() {
            Log.e("msg", "count:" + myCursor.getCount());
            return myCursor.getCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView image = new ImageView(myContext);

            myCursor.moveToPosition(position);
            int myID = myCursor.getInt(myCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String[] srcColumns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

            CursorLoader sourceCursorLoader = new CursorLoader(myContext,
                    sourceUri, srcColumns, MediaStore.Images.Media._ID + "="
                    + myID, null, null);
            sourceCursorLoader.waitForLoader();
            final Cursor sourceCursor = sourceCursorLoader.loadInBackground();

            if (sourceCursor.moveToFirst()) {
                try {
                    int srcColumnIndex = sourceCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA);
                    final String srcPath = sourceCursor.getString(srcColumnIndex);
                    final BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                    bmpFactoryOptions.inPreferredConfig = Config.RGB_565;
                    bmpFactoryOptions.inDither = true;

                    // First decode with inJustDecodeBounds=true to check dimensions
                    bmpFactoryOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(srcPath, bmpFactoryOptions);

                    // Calculate inSampleSize
                    bmpFactoryOptions.inSampleSize = calculateInSampleSize(
                            bmpFactoryOptions, imageQuality * 100,
                            imageQuality * 100);

                    // Decode bitmap with inSampleSize set
                    bmpFactoryOptions.inJustDecodeBounds = false;
                    image.setImageBitmap(BitmapFactory.decodeFile(srcPath, bmpFactoryOptions));

                    System.gc();
                } catch (Exception e) {
                    if (imageQuality > 1) {
                        imageQuality--;
                    }
                    System.gc();
                    e.printStackTrace();
                }
            }

            container.addView(image);
            return image;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
                                "If you enjoy using PhotoApp, please share it.")
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
                finish();
                return true;
        }
        return false;
    }
}
