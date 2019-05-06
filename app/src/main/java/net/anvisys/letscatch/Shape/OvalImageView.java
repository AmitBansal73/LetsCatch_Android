package net.anvisys.letscatch.Shape;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Amit Bansal on 07-01-2017.
 */
public class OvalImageView extends ImageView {

    public OvalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {

            Drawable drawable = getDrawable();

            if (drawable == null) {
                return;
            }

            if (getWidth() == 0 || getHeight() == 0) {
                return;
            }
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

            int w = getWidth(), h = getHeight();

            Bitmap roundBitmap = getOvalCroppedBitmap(bitmap, w, h);
            canvas.drawBitmap(roundBitmap, 0, 0, null);
        }
        catch (Exception ex)
        {
            int a =1;
        }

    }

    public static Bitmap getOvalCroppedBitmap(Bitmap bitmap, int width,int height) {
        Bitmap finalBitmap;
        Bitmap output=null;
        try {
            if (bitmap.getWidth() != width || bitmap.getHeight() != height)
                finalBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            else
                finalBitmap = bitmap;
           output = Bitmap.createBitmap(finalBitmap.getWidth(), finalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, finalBitmap.getWidth(), finalBitmap.getHeight());

            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.parseColor("#BAB399"));
            RectF oval = new RectF(0, 0, finalBitmap.getWidth(), finalBitmap.getHeight());
            // RectF oval = new RectF(0, 0, 130, 150);
            canvas.drawOval(oval, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(finalBitmap, rect, oval, paint);
        }
        catch (Exception ex)
        {
            int a =1;
        }
        return output;
    }

}
