package cn.jzvd.demo.Tab_1_Basic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import cn.jzvd.demo.CustomJzvd.JzvdStdScreenShot;
import cn.jzvd.demo.R;
import me.khrystal.gifbuilder.encoder.GIFEncoder;

/**
 * usage: 截取gif功能演示
 * author: kHRYSTAL
 * create time: 2020/11/30
 * update time:
 * email: 723526676@qq.com
 */

public class ScreenShotGifActivity extends AppCompatActivity implements View.OnClickListener {

    JzvdStdScreenShot jzvdStd;
    ImageView ivGif;
    Button startScreenShot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(getString(R.string.screenshot_gif));
        setContentView(R.layout.activity_screenshot_gif);

        jzvdStd = findViewById(R.id.videoplayer);
        ivGif = findViewById(R.id.ivGif);
        startScreenShot = findViewById(R.id.startScreenShot);

        startScreenShot.setOnClickListener(this);

        jzvdStd.setUp("http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4"
                , "饺子很保守", JzvdStd.SCREEN_NORMAL);

        Glide.with(this)
                .load("http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png")
                .into(jzvdStd.posterImageView);

        jzvdStd.setSupportScreenShotListener(can -> startScreenShot.setVisibility(can ? View.VISIBLE : View.GONE));
    }


    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startScreenShot(View view) {

    }

    private int frameRate = 100;
    private int length = 5;
    private static final int WHAT_SCREENSHOTS = 103;
    private int count = 0;
    List<Bitmap> bitmaps = new ArrayList<>();
    List<String> bitmapPaths = new ArrayList<>();
    private MyHandler handler;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startScreenShot:
                bitmaps.clear();
                bitmapPaths.clear();
                Toast.makeText(this, "开始截图", Toast.LENGTH_SHORT).show();
                if (handler == null) {
                    handler = new MyHandler();
                }
                handler.sendEmptyMessage(WHAT_SCREENSHOTS);
                break;
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == WHAT_SCREENSHOTS) {
                if (count >= 1000 / frameRate * length) {
                    count = 0;
                    Toast.makeText(ScreenShotGifActivity.this, "截图结束，开始转换成gif", Toast.LENGTH_SHORT).show();
                    new MyThread().start();
                    return;
                }
                Bitmap bitmap = jzvdStd.getVideoScreenShot();
                String path = getExternalCacheDir() + File.separator + String.valueOf(count + 1) + ".jpg";
                bitmapPaths.add(path);
                compressSize(bitmap, path, 440, 80);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                //转化为RGB_565，使用bitmap.copy(Bitmap.Config.RGB_565, false)无效
                Bitmap bmp = BitmapFactory.decodeFile(path, options);
                //压缩后再添加
                bitmaps.add(bmp);
                count++;
                sendEmptyMessageDelayed(WHAT_SCREENSHOTS, frameRate);
            }
        }
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            List<String> fileParts = new ArrayList<>();
            ExecutorService service = Executors.newCachedThreadPool();
            final CountDownLatch countDownLatch = new CountDownLatch(bitmaps.size());
            for (int i = 0; i < bitmaps.size(); i++) {
                final int n = i;
                final String fileName = getExternalCacheDir() + File.separator + (n + 1) + ".partgif";
                fileParts.add(fileName);
                Runnable runnable = () -> {
                    GIFEncoder encoder = new GIFEncoder();
                    encoder.setFrameRate(1000 / frameRate / 1.4f);
                    Log.e(ScreenShotGifActivity.class.getSimpleName(), "总共" + bitmaps.size() + "帧，正在添加第" + (n + 1) + "帧");
                    if (n == 0) {
                        encoder.addFirstFrame(fileName, bitmaps.get(n));
                    } else if (n == bitmaps.size() - 1) {
                        encoder.addLastFrame(fileName, bitmaps.get(n));
                    } else {
                        encoder.addFrame(fileName, bitmaps.get(n));
                    }
                    countDownLatch.countDown();
                };
                service.execute(runnable);
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(() -> Toast.makeText(ScreenShotGifActivity.this, "gif初始化成功，准备合并", Toast.LENGTH_SHORT).show());
            SequenceInputStream sequenceInputStream = null;
            FileOutputStream fos = null;
            try {
                Vector<InputStream> streams = new Vector<InputStream>();
                for (String filePath : fileParts) {
                    InputStream inputStream = new FileInputStream(filePath);
                    streams.add(inputStream);
                }
                sequenceInputStream = new SequenceInputStream(streams.elements());
                File file = new File(getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".gif");
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                // byteread表示一次读取到buffers中的数量。
                while ((len = sequenceInputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.close();
                sequenceInputStream.close();
                handler.post(() -> {
                    Toast.makeText(ScreenShotGifActivity.this, "gif制作完成", Toast.LENGTH_SHORT).show();
                    Glide.with(ScreenShotGifActivity.this)
                            .load(file).into(ivGif);
                });
                for (String filePath : fileParts) {
                    File f = new File(filePath);
                    if (f.exists()) {
                        f.delete();
                    }
                }
                fileParts.clear();
                for (String bitmapPath : bitmapPaths) {
                    File f = new File(bitmapPath);
                    if (f.exists()) {
                        f.delete();
                    }
                }
                bitmapPaths.clear();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (sequenceInputStream != null) {
                    try {
                        sequenceInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void compressSize(Bitmap bitmap, String toFile, int targetWidth, int quality) {
        try {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            int targetHeight = bitmapHeight * targetWidth / bitmapWidth;
            Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
            File myCaptureFile = new File(toFile);
            FileOutputStream out = new FileOutputStream(myCaptureFile);
            if (resizeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (!resizeBitmap.isRecycled()) {
                resizeBitmap.recycle();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
