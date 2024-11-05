package com.example.medapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medapp.ml.ModelUnquant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


public class MainActivity extends AppCompatActivity {

    Button camera, gallery;
    ImageView imageView;
    TextView result;
    String label;
    EditText inputText;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
    }

    public void classifyImage(Bitmap image){
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f /255));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f /255));
                    byteBuffer.putFloat((val & 0xFF) * (1.f /255));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
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
            String[] classes = {"AloeVera", "Amla", "Amruta-Balli","Arali","Ashoka","Ashwagandha","Avacado","Bamboo","Basale","Betel","Betel-Nut","Brahmi",
            "Castor","Curry-Leaf","Doddapatre","Ekka","Ganike","Guava","Geranium","Henna","Hibiscus","Honge","Insulin","Jasmine","Lemon","Lemongrass","Mango",
            "Mint","Nagadali","Neem","Nithyapushpa","Noori","Papaya","Pepper","Pomegranate","Raktachandini","Rose","Sapota","Tulasi","Wood-Sorel"};
            result.setText(classes[maxPos]);
            label = classes[maxPos];

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }else{
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Method to show the description dialog
    public void showDescriptionDialog(View view) {
        // Replace "labelToRead" with the actual label you want to read
        String labelToRead = label;
        String descriptionFilePath = getDescriptionFilePath(labelToRead);
        Log.d("DescriptionApp", "Description file path: " + descriptionFilePath);

        if (descriptionFilePath != null) {
            String description = readTextFile(descriptionFilePath);

            if (description != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Description")
                        .setMessage(description)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do something when the OK button is clicked
                                dialog.dismiss(); // Dismiss the dialog
                            }
                        })
                        .show();
            } else {
                // Handle the case when reading the text file fails
                Log.e("DescriptionApp", "Failed to read the text file");
            }
        } else {
            // Handle the case when the label is not found or file path is null
            Log.e("DescriptionApp", "Label not found or file path is null");
            // You can show a toast or a message to the user if needed
        }
    }

    // Method to get the path of the description text file from the JSON file
    private String getDescriptionFilePath(String label) {
        AssetManager assetManager = getAssets();

        try {
            InputStream jsonStream = assetManager.open("description.json");
            int jsonSize = jsonStream.available();
            byte[] jsonBuffer = new byte[jsonSize];
            jsonStream.read(jsonBuffer);
            jsonStream.close();

            String jsonString = new String(jsonBuffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);

            if (jsonObject.has("labels")) {
                JSONArray labelsArray = jsonObject.getJSONArray("labels");

                for (int i = 0; i < labelsArray.length(); i++) {
                    JSONObject labelObject = labelsArray.getJSONObject(i);

                    if (labelObject.has("name") && labelObject.getString("name").equals(label)) {
                        if (labelObject.has("description_file")) {
                            return labelObject.getString("description_file");
                        }
                    }
                }
            }

            // Handle the case when the label or description_file field is not found in the JSON object
            return null;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            // Log the exception
            Log.e("DescriptionApp", "Exception: " + e.getMessage());
            // Handle exceptions
            return null;
        }
    }

    // Method to read the contents of a text file
    private String readTextFile(String filePath) {
        AssetManager assetManager = getAssets();

        try {
            InputStream textStream = assetManager.open(filePath);
            int textSize = textStream.available();
            byte[] textBuffer = new byte[textSize];
            textStream.read(textBuffer);
            textStream.close();

            return new String(textBuffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            // Log the exception
            Log.e("DescriptionApp", "Exception: " + e.getMessage());
            // Handle exceptions
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void buttonCallGeminiAPI(View view) {
        inputText = (EditText) findViewById(R.id.inputEditText);
        String prompt = inputText.getText().toString();
        // For text-only input, use the gemini-pro model
        GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-pro",
                // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                /* apiKey */ "API_KEY");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                showAlertDialog("Generated Text", resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                showAlertDialog("Error", t.toString());
            }
        }, this.getMainExecutor());
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing, or handle the OK button click if needed
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
