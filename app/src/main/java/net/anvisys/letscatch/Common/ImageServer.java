package net.anvisys.letscatch.Common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.anvisys.letscatch.R;
import net.anvisys.letscatch.Shape.OvalImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Amit Bansal on 16-01-2017.
 */
public class ImageServer {

    private static String directoryName = "images";
    private Context mContext;

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            else
            {
                return false;
            }

        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static File getAlbumStorageDir(String albumName) {
        try {
            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
            if (!file.mkdirs()) {
                return null;
            }
            else
            {
                return file;
            }


        }
        catch (Exception ex)
        {
            return null;
        }
    }


    public static boolean SaveImageString(String strImage, String MobileNumber, Context context)
    {
        try {
            byte[] imgByte = Base64.decode(strImage, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

                File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
                File file = new File(directory, MobileNumber);

                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                return true;

        }
        catch (Exception ex)
        {
            return false;
        }

    }
    public static boolean SaveBitmapToExternal(Bitmap resizedBitmap, String FileName, Context mContext)
    {
        try
        {
            if(isExternalStorageWritable()) {
                File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File myDir = new File(root + "/LetsMeet");
                myDir.mkdirs();
                //File directory = mContext.getDir(directoryName, Context.MODE_PRIVATE);
                //File directory = getAlbumStorageDir("LetsMeet");
                File file = new File(myDir, FileName+".jpg");
               // file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();

                MediaScannerConnection.scanFile(mContext,
                        new String[]{file.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });
                return true;
            }
            else
            {return false;}
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static boolean SaveBitmapImage(Bitmap resizedBitmap, String FileName, Context mContext)
    {
        try
        {

                File directory = mContext.getDir(directoryName, Context.MODE_PRIVATE);

                File file = new File(directory, FileName);
                FileOutputStream fos = new FileOutputStream(file);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.close();
                return true;

        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static boolean DeleteBitmapImage(String MobileNumber, Context mContext)
    {
        try
        {
            File directory = mContext.getDir(directoryName, Context.MODE_PRIVATE);
            File file = new File(directory, MobileNumber);
            file.delete();

            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static Bitmap getBitmapFromString(String strImage, Context mContext )
    {
        try {
            byte[] imgByte = Base64.decode(strImage, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            return bmp;
        }
        catch (Exception ex)
        {
            int a =1;
            return GetDefaultImage(mContext);
        }
    }

    public static String getCroppedStringFromBitmap(Bitmap bitmapPicture) {
        try {
            final int COMPRESSION_QUALITY = 100;
            Bitmap bmp;
            String encodedImage;
            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
            int Height = bitmapPicture.getHeight();
            int Width = bitmapPicture.getWidth();
            int newHeight;
            int newWidth;
            if (Height > 200 && Width > 200) {
                if (Height > Width) {
                    newWidth = 200;
                    newHeight = Height / Width * 200;
                } else {

                    newHeight = 200;
                    newWidth = Width / Height * 200;
                }

                bitmapPicture=    Bitmap.createBitmap(bitmapPicture, 0, 0, newWidth, newHeight);

            }


            bitmapPicture.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayBitmapStream);

            byte[] ImageByte = byteArrayBitmapStream.toByteArray();

            encodedImage = Base64.encodeToString(ImageByte, Base64.DEFAULT);
            return encodedImage;
        }
        catch (Exception ex)
        {
            return "";
        }
    }


    public static String getStringFromBitmap(Bitmap bitmapPicture) {
         try {
             final int COMPRESSION_QUALITY = 100;

             String encodedImage;
             ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
             bitmapPicture.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayBitmapStream);

             byte[] ImageByte = byteArrayBitmapStream.toByteArray();


             encodedImage = Base64.encodeToString(ImageByte, Base64.DEFAULT);
             return encodedImage;
         }
         catch (Exception ex)
         {
             return "";
         }
    }

    public static Bitmap GetImageBitmapFromExternal(String MobileNumber, Context context)
    {
        Bitmap thumbnail = null;
        try {
            if(isExternalStorageWritable()) {
                File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File myDir = new File(root + "/LetsMeet");
                myDir.mkdirs();
                // File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
                //File directory = getAlbumStorageDir("LetsMeet");
                File file = new File(myDir, MobileNumber+".jpg");
                FileInputStream fi = new FileInputStream(file);
                thumbnail = BitmapFactory.decodeStream(fi);
            }
            else
            {
                thumbnail = GetDefaultImage(context);
            }
        }
        catch (Exception ex)
        {
            thumbnail = GetDefaultImage(context);
        }
        return thumbnail;
    }


    public static Bitmap GetImageBitmap(String MobileNumber, Context context)
    {
        Bitmap thumbnail = null;
        try {

               File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
               File file = new File(directory, MobileNumber);
                FileInputStream fi = new FileInputStream(file);
                thumbnail = BitmapFactory.decodeStream(fi);

        }
        catch (Exception ex)
        {
            thumbnail = GetDefaultImage(context);
        }
        return thumbnail;
    }

    public static Bitmap GetDefaultImage(Context context)
    {
        Bitmap defBMP = null;
        try {
            Drawable d = ResourcesCompat.getDrawable(context.getResources(),  R.drawable.user_image, null);
            defBMP = ((BitmapDrawable) d).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            defBMP.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        }
        catch (Exception ex)
        {

        }
        return defBMP;
    }

    public static Bitmap GetBitmapFromDrawable( Bitmap bmp, Context mContext)
    {
        Bitmap thumbnail = null;
        try
         {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            View mView = layoutInflater.inflate(R.layout.marker_layout, null);
            OvalImageView img = (OvalImageView) mView.findViewById(R.id.markerImage);
            img.setImageBitmap(bmp);
            mView.invalidate();
            thumbnail = createDrawableFromView(mContext, mView);
            //  Bitmap b = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
        }
        catch (Exception ex)
        {
int a =1;
        }

        return thumbnail;
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        } catch (Exception ex) {
            return null;
        }
    }
}
