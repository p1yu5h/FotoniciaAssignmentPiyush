package com.example.android.fotoniciaassignmentpiyush;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView element1, element2, base;
    RelativeLayout relativeLayout;
    Bitmap image;
    Button save;

    private int xDelta;
    private int yDelta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        element1 = findViewById(R.id.element1);
        element2 = findViewById(R.id.element2);
        base = findViewById(R.id.base_layout);
        relativeLayout = findViewById(R.id.relative_layout  );

        //resizeAndScale(element1, 350,350,471,282);
        //resizeAndScale(element2,350,350,1914,1137);

        save = findViewById(R.id.button);

        element1.setOnTouchListener(onTouchListener());
        element2.setOnTouchListener(onTouchListener());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        base.requestLayout();
                        int baseHeight = base.getHeight();
                        image = viewToBitmap(relativeLayout, baseHeight);
                        writeToStorage(image);

                    }
                }, 1000);

            }
        });



    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        base.setImageAlpha(150);
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                                view.getLayoutParams();

                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                                .getLayoutParams();
                        layoutParams.leftMargin = x - xDelta;
                        layoutParams.topMargin = y - yDelta;
                        layoutParams.rightMargin = 0;
                            layoutParams.bottomMargin = 0;
                        view.setLayoutParams(layoutParams);
                        break;

                    case MotionEvent.ACTION_UP:
                        base.setImageAlpha(255);
                }
                relativeLayout.invalidate();
                return true;
            }
        };
    }

    void resizeAndScale(ImageView v, int w, int h, int x, int y){
        v.requestLayout();
        v.getLayoutParams().height = h;
        v.getLayoutParams().width = w;
        v.setX(x);
        v.setY(y);
    }


    public Bitmap viewToBitmap(View view, int height) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 3600, 2400, true);
        return resizedBitmap;
    }

    public void writeToStorage(Bitmap bitmap){
        try {
            File dir = new File(Environment.getExternalStorageDirectory()+"/piyush/");
            dir.mkdirs();

            File file =new File(dir+ "test1.png");

            FileOutputStream output = new FileOutputStream(file );
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
            Log.d("hello",file.toString());
            Toast.makeText(getApplicationContext(),"File saved to "+ file.toString(),Toast.LENGTH_LONG ).show();

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
                    + file.toString())));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }
    }
}
