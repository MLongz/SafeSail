package no.hsn.sailsafe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hakonst on 04.05.16.
 */
public class splash_screen extends Activity {
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        StartAnimations();

        Timer timer = new Timer(); // Create a new timer.
        timer.schedule(new TimerTask() { // Creates a new timer task. A task that can be scheduled for one-time or repeated execution by a Timer.
            public void run() {

                Intent intent = new Intent(splash_screen.this, MainActivity.class); //TODO sett inn neste activity
                finish(); //Closing this activity before open next
                startActivity(intent);
            }
        }, 4000); //Set timer to 4sec
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.blure); //Retrieving Blure.XML that create blure affect.
        anim.reset();
        LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay); //Retrieving LinearLayout.
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.move_icon); //Retrieving move_icon.xml
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.logo);
        iv.clearAnimation();
        iv.startAnimation(anim);

    }
}
