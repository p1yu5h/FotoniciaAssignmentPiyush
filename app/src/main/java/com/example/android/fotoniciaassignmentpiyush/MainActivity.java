package com.example.android.fotoniciaassignmentpiyush;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
    Button save, selectImages, share;

    private int xDelta;
    private int yDelta;

    private static final int SELECT_PICTURE = 100;
    private static int image_set = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        element1 = findViewById(R.id.element1);
        element2 = findViewById(R.id.element2);
        base = findViewById(R.id.base_layout);
        relativeLayout = findViewById(R.id.relative_layout  );

        //resizeAndScale(element1, 350,350,471,282);
        //resizeAndScale(element2,350,350,1914,1137);

        save = findViewById(R.id.button);

        element1.setOnTouchListener(onTouchListener());
        element2.setOnTouchListener(onTouchListener());

        selectImages = findViewById(R.id.select_images);
        share = findViewById(R.id.share);

        selectImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });



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

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "Look at my awesome picture";
                File imageFile = new File(Environment.getExternalStorageDirectory()+"/piyushtest1.png");
                Log.d("hellll", Environment.getExternalStorageDirectory()+"/piyushtest1.png");
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".com.example.android.fotoniciaassignmentpiyush.provider", imageFile);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share images..."));
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

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getPathFromURI(selectedImageUri);
                            Log.i("piyush", "Image Path : " + path);
                            // Set the image in ImageView
                            if(image_set == 0){
                            findViewById(R.id.element1).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.element1)).setImageURI(selectedImageUri);
                                }
                            });
                            image_set = 1;
                            }else {
                                findViewById(R.id.element2).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ImageView) findViewById(R.id.element2)).setImageURI(selectedImageUri);
                                    }
                                });
                            image_set = 0;
                            }



                        }
                    }
                }
            }
        }).start();

    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

}
