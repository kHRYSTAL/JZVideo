package cn.jzvd.demo.CustomJzvd;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import cn.jzvd.JzvdStd;

/**
 * usage:
 * author: kHRYSTAL
 * create time: 2020/11/30
 * update time:
 * email: 723526676@qq.com
 */

public class JzvdStdScreenShot extends JzvdStd {

    private SupportScreenShotListener supportScreenShotListener;

    public JzvdStdScreenShot(Context context) {
        super(context);
    }

    public JzvdStdScreenShot(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSupportScreenShotListener(SupportScreenShotListener listener) {
        this.supportScreenShotListener = listener;
    }

    @Override
    public void onStateNormal() {
        super.onStateNormal();
        if (supportScreenShotListener != null) {
            supportScreenShotListener.canScreenShot(false);
        }
    }

    @Override
    public void onStatePreparing() {
        super.onStatePreparing();
        if (supportScreenShotListener != null) {
            supportScreenShotListener.canScreenShot(false);
        }
    }

    @Override
    public void onStatePause() {
        super.onStatePause();
        if (supportScreenShotListener != null) {
            supportScreenShotListener.canScreenShot(true);
        }
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        if (supportScreenShotListener != null) {
            supportScreenShotListener.canScreenShot(true);
        }
    }

    public interface SupportScreenShotListener {
        public void canScreenShot(boolean can);
    }

    @Override
    public void onStateError() {
        super.onStateError();
        if (supportScreenShotListener != null) {
            supportScreenShotListener.canScreenShot(false);
        }
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        if (supportScreenShotListener != null) {
            supportScreenShotListener.canScreenShot(false);
        }
    }

    public Bitmap getVideoScreenShot() {
        if (textureView != null && (state == STATE_PLAYING || state == STATE_PAUSE)) {
            Bitmap screenShot = textureView.getBitmap();
            return screenShot;
        }
        return null;
    }
}
