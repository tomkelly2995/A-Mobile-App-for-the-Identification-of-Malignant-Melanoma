package com.example.mymelanomaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;


import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.TensorFlowLite;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.*;

public class MainActivity extends AppCompatActivity {

    ImageView cover;
    ImageView imageView2;
    FloatingActionButton fab;
    Interpreter tflite;
    TextView text;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cover = findViewById(R.id.coverImg);
        ExtendedFloatingActionButton fab = (ExtendedFloatingActionButton) findViewById(R.id.floatingActionButton);
        text = findViewById(R.id.textView);


        try {
            tflite = new Interpreter(loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.Companion.with(MainActivity.this)
                        /*.crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)*/
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                System.out.println("________URI: "+uri);
                cover.setImageURI(uri);
                
                // Add null check for drawable
                if (cover.getDrawable() instanceof BitmapDrawable) {
                    BitmapDrawable drawable = (BitmapDrawable) cover.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();

                    ImageProcessor imageProcessor =
                            new ImageProcessor.Builder()
                                    .add(new ResizeOp(200, 150, ResizeOp.ResizeMethod.BILINEAR))
                                    .add(new NormalizeOp(0.0f, 255.0f))
                                    .build();

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);

                    // Analysis code for every frame
                    // Preprocess the image


                    tensorImage.load(bitmap);
                    tensorImage = imageProcessor.process(tensorImage);


                    float result = doInference(tensorImage);
                    System.out.println("________RESULT________ :   "+result);
                    if (result > 0.5) {
                        text.setText("Result: Melanoma, "+df.format(result*100)+"%");
                    }else{
                        text.setText("Result: Non Melanoma, "+df.format((1-result)*100)+"%");
                    }
                }
            }
        }
    }


    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor=this.getAssets().openFd("model_2.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    private float doInference(TensorImage tensorImage) {
        float[][] output=new float[1][2];

        tflite.run(tensorImage.getBuffer(),output);
        return output[0][1];

    }



}