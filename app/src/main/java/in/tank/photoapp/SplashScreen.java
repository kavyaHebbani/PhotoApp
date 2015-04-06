package in.tank.photoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Kavya
 */
public class SplashScreen extends Activity {

    // Splash Screen Timer
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
