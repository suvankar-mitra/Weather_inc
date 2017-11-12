package es.esy.iamsuvankar.weatherinc;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by suvankar on 12/11/17.
 */

public class FontManager {
    private Typeface mTypefaceJosefinRegular;
    private Typeface mTypefaceJosefinBold;
    public FontManager(Context context) {
        //Initializing custom font
        AssetManager am = context.getAssets();
        mTypefaceJosefinRegular = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "JosefinSans-Regular.ttf"));
        mTypefaceJosefinBold = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "JosefinSans-Bold.ttf"));
    }

    public void setTypefaceJosefinRegular(TextView mViewElement) {
        if(mViewElement == null) return;
        mViewElement.setTypeface(mTypefaceJosefinRegular);
    }
    public void setTypefaceJosefinRegular(TextView... mViewElement) {
        if(mViewElement == null) return;
        for(TextView tv:mViewElement) {
            tv.setTypeface(mTypefaceJosefinRegular);
        }
    }
    public void setTypefaceJosefinBold(TextView mViewElement) {
        if(mViewElement == null) return;
        mViewElement.setTypeface(mTypefaceJosefinBold);
    }
    public void setTypefaceJosefinBold(TextView... mViewElement) {
        if(mViewElement == null) return;
        for(TextView tv:mViewElement) {
            tv.setTypeface(mTypefaceJosefinBold);
        }
    }
}
