package com.example.android.fotoniciaassignmentpiyush;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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
    ProgressBar progressBar;
    ImageButton clearall;
    TextView helpText;


    private static final int SELECT_PICTURE = 100;
    private static int image_set = 0;

    float scalediff;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        element1 = findViewById(R.id.element1);
        element2 = findViewById(R.id.element2);
        base = findViewById(R.id.base_layout);
        relativeLayout = findViewById(R.id.relative_layout);
        progressBar = findViewById(R.id.progressbar);
        clearall = findViewById(R.id.clearall);
        helpText = findViewById(R.id.help_text);

        Glide.with(this).load(R.drawable.base_layout).into(base);
        Glide.with(this).load(R.drawable.placeholder_img).into(element1);
        Glide.with(this).load(R.drawable.placeholder_img).into(element2);

        save = findViewById(R.id.save_button);

        element1.setOnTouchListener(onTouchListener());
        element2.setOnTouchListener(onTouchListener());

        selectImages = findViewById(R.id.select_images_button);
        share = findViewById(R.id.share_button);

        clearall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialog().show();
            }
        });

        selectImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpText.setVisibility(View.GONE);
                openImageChooser();
                save.setEnabled(true);
                share.setEnabled(true);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToStorage();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToStorage();

                String text = "Hi, have a look at this picture I created.";
                File imageFile = new File(Environment.getExternalStorageDirectory() + "/piyush/test.png");
                Log.d("image", Environment.getExternalStorageDirectory() + "/piyush/test.png");
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

    public void saveToStorage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                clearall.setVisibility(View.GONE);
            }
        });

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

    public AlertDialog createAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Start over?");
        alertDialogBuilder.setMessage("Would you like to start over?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                element1.setVisibility(View.INVISIBLE);
                                element2.setVisibility(View.INVISIBLE);
                                clearall.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return (alertDialogBuilder.create());
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {

            RelativeLayout.LayoutParams params;
            int startwidth;
            int startheight;
            float dx = 0, dy = 0, x = 0, y = 0;
            float angle = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final ImageView view = (ImageView) v;

                ((BitmapDrawable) view.getDrawable()).setAntiAlias(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        base.setImageAlpha(150);
                        params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        startwidth = params.width;
                        startheight = params.height;
                        dx = event.getRawX() - params.leftMargin;
                        dy = event.getRawY() - params.topMargin;
                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            mode = ZOOM;
                        }

                        d = rotation(event);

                        break;
                    case MotionEvent.ACTION_UP:
                        base.setImageAlpha(255);
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {

                            x = event.getRawX();
                            y = event.getRawY();

                            params.leftMargin = (int) (x - dx);
                            params.topMargin = (int) (y - dy);

                            params.rightMargin = 0;
                            params.bottomMargin = 0;
                            params.rightMargin = params.leftMargin + (5 * params.width);
                            params.bottomMargin = params.topMargin + (10 * params.height);

                            view.setLayoutParams(params);

                        } else if (mode == ZOOM) {

                            if (event.getPointerCount() == 2) {

                                newRot = rotation(event);
                                float r = newRot - d;
                                angle = r;

                                x = event.getRawX();
                                y = event.getRawY();

                                float newDist = spacing(event);
                                if (newDist > 10f) {
                                    float scale = newDist / oldDist * view.getScaleX();
                                    if (scale > 0.6) {
                                        scalediff = scale;
                                        view.setScaleX(scale);
                                        view.setScaleY(scale);

                                    }
                                }

                                view.animate().rotationBy(angle).setDuration(0).setInterpolator(new LinearInterpolator()).start();

                                x = event.getRawX();
                                y = event.getRawY();

                                params.leftMargin = (int) ((x - dx) + scalediff);
                                params.topMargin = (int) ((y - dy) + scalediff);

                                params.rightMargin = 0;
                                params.bottomMargin = 0;
                                params.rightMargin = params.leftMargin + (5 * params.width);
                                params.bottomMargin = params.topMargin + (10 * params.height);

                                view.setLayoutParams(params);


                            }
                        }
                        break;
                }

                return true;

            }
        };
    }


    public Bitmap viewToBitmap(View view, int height) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return Bitmap.createScaledBitmap(bitmap, 3600, 2400, true);
    }

    public void writeToStorage(Bitmap bitmap) {


        try {
            File dir = new File(Environment.getExternalStorageDirectory() + "/piyush/");
            dir.mkdirs();

            File file = new File(dir + "/test.png");

            FileOutputStream output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
            Log.d("hello", file.toString());
            Toast.makeText(getApplicationContext(), "File saved to " + file.toString(), Toast.LENGTH_LONG).show();

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.toString())));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                    clearall.setVisibility(View.VISIBLE);
                }
            });
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
                            if (image_set == 0) {
                                findViewById(R.id.element1).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(getApplicationContext()).load(selectedImageUri).into(element1);
                                    }
                                });
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        element1.setVisibility(View.VISIBLE);
                                        clearall.setVisibility(View.VISIBLE);

                                    }
                                });
                                image_set = 1;
                            } else {
                                findViewById(R.id.element2).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(getApplicationContext()).load(selectedImageUri).into(element2);
                                    }
                                });
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        element2.setVisibility(View.VISIBLE);
                                        clearall.setVisibility(View.VISIBLE);
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

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

}
