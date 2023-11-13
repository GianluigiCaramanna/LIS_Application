package app.ij.LIS_Application;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import app.ij.LIS_Application.ml.Model;
import app.ij.LIS_Application.ml.Number;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button camera, gallery, clear, delete;
    int switch_temp;
    ImageView imageView;
    TextView result,frase;
    String myText;
    Switch switch_AI;
    int imageSize = 224; //200;
    ArrayList doPhrase = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        clear = findViewById(R.id.button3);
        delete = findViewById(R.id.button4);

        result = findViewById(R.id.result);
        switch_AI = (Switch) findViewById(R.id.switch_IA);
        frase = findViewById(R.id.frase);
        imageView = findViewById(R.id.imageView);

        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);
        clear.setOnClickListener(this);
        delete.setOnClickListener(this);

        switch_AI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked) {
                   switch_temp = 0;
                   switch_AI.setText("alphabet ASL");
               }else {
                   switch_AI.setText("number ASL");
                   switch_temp = 1;
               }
            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                capturePhoto();
                break;
            case R.id.button2:
                galleryMethod();
                break;
            case R.id.button3:
                clearAll();
                break;
            case R.id.button4:
                deleteItem();
                break;
        }

    }


    public void capturePhoto() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 3);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    public void galleryMethod() {
        Intent cameraIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(cameraIntent, 1);
    }

    public void clearAll() {
        //if(doPhrase.size() >1)
        doPhrase.clear();
        frase.setText("");
    }

    public void deleteItem() {
        if(doPhrase.size() !=0 ) {
            doPhrase.remove(doPhrase.size() -1);
            composePhrase();
        }

    }


    public void classifyImage(Bitmap image){
        if(switch_temp==0) {
            try {
                Model model = Model.newInstance(getApplicationContext());

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
                byteBuffer.order(ByteOrder.nativeOrder());

                int[] intValues = new int[imageSize * imageSize];
                image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
                int pixel = 0;
                //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
                for (int i = 0; i < imageSize; i++) {
                    for (int j = 0; j < imageSize; j++) {
                        int val = intValues[pixel++]; // RGB
                        byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                        byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                        byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                    }
                }

                inputFeature0.loadBuffer(byteBuffer);

                // Runs model inference and gets result.
                Model.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                float[] confidences = outputFeature0.getFloatArray();
                // find the index of the class with the biggest confidence.
                int maxPos = 0;
                float maxConfidence = 0;
                for (int i = 0; i < confidences.length; i++) {
                    if (confidences[i] > maxConfidence) {
                        maxConfidence = confidences[i];
                        maxPos = i;
                    }
                }

                //prova txt
                InputStream inputStream = getAssets().open("classes.txt");
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                String txt = new String(buffer);



                //take classes from txt file and put in string[];
                String[] tokens = txt.split("\n");
                System.out.println("CONFIDENCE IN ALPHABET: " + maxConfidence);
                doPhrase.add(tokens[maxPos]);
                result.setText(tokens[maxPos]);
                composePhrase();

                // Releases model resources if no longer used.
                model.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                Number model = Number.newInstance(getApplicationContext());

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 200, 200, 3}, DataType.FLOAT32);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 200 * 200 * 3);
                byteBuffer.order(ByteOrder.nativeOrder());

                int[] intValues = new int[200 * 200];
                image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
                int pixel = 0;
                //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
                for (int i = 0; i < 200; i++) {
                    for (int j = 0; j < 200; j++) {
                        int val = intValues[pixel++]; // RGB
                        byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                        byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                        byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                    }
                }

                inputFeature0.loadBuffer(byteBuffer);

                // Runs model inference and gets result.
                Number.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                float[] confidences = outputFeature0.getFloatArray();
                // find the index of the class with the biggest confidence.
                int maxPos = 0;
                float maxConfidence = 0;
                for (int i = 0; i < confidences.length; i++) {
                    if (confidences[i] > maxConfidence) {
                        maxConfidence = confidences[i];
                        maxPos = i;
                    }
                }

                InputStream inputStream = getAssets().open("number.txt");
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                String txt = new String(buffer);



                //take classes from txt file and put in string[];
                //String classes = getStringFromFile();
                String[] tokens = txt.split("\n");
                System.out.println("CONFIDENCE IN NUMBER: " + maxConfidence);
                doPhrase.add(tokens[maxPos]);
                result.setText(tokens[maxPos]);
                composePhrase();

                // Releases model resources if no longer used.
                model.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){ //capture photo
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);
                if(switch_temp ==0) {
                    imageSize = 224;
                }else {
                    imageSize = 200;
                }
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }else{ //gallery
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //imageView.setRotation(90F);
                imageView.setImageBitmap(image);
                if(switch_temp ==0) {
                    imageSize = 224;
                }else {
                    imageSize = 200;
                }
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                //image = getResizedBitmap(image,224,244);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void composePhrase() {
        String phr = " ";
        for (int i=0; i<doPhrase.size(); i++) {
            if (doPhrase.get(i).equals("nothing") || doPhrase.get(i).equals("unknown") ) {
                phr += "";
            }else if (doPhrase.get(i).equals("space")) {
                phr += " ";
            }else {
                phr += doPhrase.get(i);
            }
        }
        frase.setText(phr);
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;
    }



}