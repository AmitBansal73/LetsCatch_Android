package net.anvisys.letscatch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.UTILITY;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SendImageActivity extends AppCompatActivity {

    ImageView selectedImage;
    TextView sendText;
    String imgString;
    int REQUEST_SEND_IMAGE=2;
    Bitmap imgBMP;
    String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);
        selectedImage = (ImageView)findViewById(R.id.selectedImage);
        sendText =(TextView)findViewById(R.id.sendButton);

        try {
            Intent intent = getIntent();
            Uri imageURI = Uri.parse(intent.getStringExtra("image_uri"));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            InputStream image_stream = getContentResolver().openInputStream(imageURI);

            imgBMP= BitmapFactory.decodeStream(image_stream,null,options);
            int count = imgBMP.getByteCount();
            int height = imgBMP.getHeight();
            int width = imgBMP.getWidth();
            int newHeight =1, newWidth =1;
            if(height>1280 || width>1280)
            {
                if(height>width)
                {
                    newHeight = 1280;
                    newWidth = (width*newHeight/height);
                }
                else
                {
                    newWidth = 1280;
                    newHeight =(height*newWidth/width);
                }

                imgBMP =  Bitmap.createScaledBitmap(imgBMP,newWidth, newHeight,true);
                int newCount = imgBMP.getByteCount();
            }
            //imgString = ImageServer.getCompressedStringFromBitmap(imgBMP);
            //imgBMP = ImageServer.getBitmapFromString(imgString, getApplicationContext());
            Name = "temp_" + UTILITY.CurrentLocalTimeString();
            ImageServer.SaveBitmapImage(imgBMP,Name,getApplicationContext());
            selectedImage.setImageBitmap(imgBMP);
        }
        catch (FileNotFoundException fEx)
        {

        }

        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (imgBMP != null) {
                        Intent intent = new Intent();
                        intent.putExtra("ImageName", Name);
                        setResult(REQUEST_SEND_IMAGE, intent);
                        SendImageActivity.this.finish();
                    }
                }
                catch (Exception ex)
                {
                    UTILITY.HandleException(getApplicationContext(),"SendImage",ex.toString());
                }

            }
        });

    }
}
