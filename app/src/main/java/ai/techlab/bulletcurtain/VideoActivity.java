package ai.techlab.bulletcurtain;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VideoActivity extends AppCompatActivity implements VideoListener {
    private static final String TAG = "TextureViewCanvasActivi";

    private TextureView mCurtainView;
    private Renderer mRenderer;
    private TextureView mVideoView;
    private SimpleExoPlayer player;

    private View mTopScrim;
    private View mBottomScrim;
    private ImageView mLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Bullet> bullets = new MockBulletProvider(getResources()).getBullets();
        mRenderer = new Renderer(bullets);

        setContentView(R.layout.activity_video);
        mVideoView = findViewById(R.id.video_view);

        mTopScrim = findViewById(R.id.top_scrim);
        mBottomScrim = findViewById(R.id.bottom_scrim);
        mLogo = findViewById(R.id.logo);

        mCurtainView = findViewById(R.id.canvasTextureView);
        mCurtainView.setSurfaceTextureListener(mRenderer);
        mCurtainView.setOpaque(false);

        initPlayer();
    }

    private void initPlayer() {
        player = new SimpleExoPlayer.Builder(getApplicationContext()).build();
//        player.prepare(buildMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")));
        player.prepare(buildRawMediaSource());
        player.setPlayWhenReady(true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setVideoTextureView(mVideoView);
        player.addVideoListener(this);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "namh-test");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    private MediaSource buildRawMediaSource() {
        Uri uri = RawResourceDataSource.buildRawResourceUri(R.raw.horses);

        ExtractorMediaSource audioSource = new ExtractorMediaSource(
                uri,
                new DefaultDataSourceFactory(this, "MyExoplayer"),
                new DefaultExtractorsFactory(),
                null,
                null
        );
        return audioSource;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        // Don't do this -- halt the thread in onPause() and wait for it to finish.
        mRenderer.halt();
        if (player != null) {
            player.release();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.d(TAG, "onVideoSizeChanged: ");
        int yOffset = adjustAspectRatio(mVideoView, width, height);

        // TODO better listen to pogress change from exoplayer.

        {
            // Setting up the scrim
            mTopScrim.setVisibility(View.VISIBLE);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(70)
            );
            params.setMargins(0, yOffset, 0, 0);
            mTopScrim.setLayoutParams(params);

            mBottomScrim.setVisibility(View.VISIBLE);

            FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(70)
            );
            params1.setMargins(0, yOffset + height - dpToPx(70), 0, 0);
            mBottomScrim.setLayoutParams(params1);
        }

        {
            mLogo.setVisibility(View.VISIBLE);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    dpToPx(55),
                    dpToPx(43)
            );
            params.setMargins(dpToPx(13), yOffset + height - dpToPx(43) - dpToPx(9), 0, 0);
            mLogo.setLayoutParams(params);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, yOffset, 0, 0);

        mCurtainView.setLayoutParams(params);
        mRenderer.start();
    }

    private int adjustAspectRatio(TextureView view, int videoWidth, int videoHeight) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        view.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
//        txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        view.setTransform(txform);

        return yoff;
    }

    /**
     * Handles Canvas rendering and SurfaceTexture callbacks.
     * <p>
     * We don't create a Looper, so the SurfaceTexture-by-way-of-TextureView callbacks
     * happen on the UI thread.
     */
    private static class Renderer extends Thread implements TextureView.SurfaceTextureListener {
        private Object mLock = new Object();        // guards mSurfaceTexture, mDone
        private SurfaceTexture mSurfaceTexture;
        private boolean mDone;

        private int mWidth;     // from SurfaceTexture
        private int mHeight;

        // each bullet should be measured before
        private List<Bullet> mBullets = new CopyOnWriteArrayList<>();
        int mScreenWidth;

        public Renderer(List<Bullet> bullets) {
            super("TextureViewCanvas Renderer");
            mBullets.addAll(bullets);
            mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        }

        public void pushBullet(Bullet bullet) {
            mBullets.add(bullet);
        }

        @Override
        public void run() {
            while (true) {
                SurfaceTexture surfaceTexture = null;

                // Latch the SurfaceTexture when it becomes available.  We have to wait for
                // the TextureView to create it.
                synchronized (mLock) {
                    while (!mDone && (surfaceTexture = mSurfaceTexture) == null) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ie) {
                            throw new RuntimeException(ie);     // not expected
                        }
                    }
                    if (mDone) {
                        break;
                    }
                }
                Log.d(TAG, "Got surfaceTexture=" + surfaceTexture);

                // Render frames until we're told to stop or the SurfaceTexture is destroyed.
                doAnimation();
            }

            Log.d(TAG, "Renderer thread exiting");
        }

        /**
         * Draws updates as fast as the system will allow.
         * <p>
         * In 4.4, with the synchronous buffer queue queue, the frame rate will be limited.
         * In previous (and future) releases, with the async queue, many of the frames we
         * render may be dropped.
         * <p>
         * The correct thing to do here is use Choreographer to schedule frame updates off
         * of vsync, but that's not nearly as much fun.
         */
        private void doAnimation() {
            final int BLOCK_WIDTH = 80;
            int xpos = -BLOCK_WIDTH / 2;

            // Create a Surface for the SurfaceTexture.
            Surface surface;
            synchronized (mLock) {
                SurfaceTexture surfaceTexture = mSurfaceTexture;
                if (surfaceTexture == null) {
                    Log.d(TAG, "ST null on entry");
                    return;
                }
                surface = new Surface(surfaceTexture);
            }

            while (true) {
                Canvas canvas;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    canvas = surface.lockHardwareCanvas();
                } else {
                    canvas = surface.lockCanvas(null);
                }
                if (canvas == null) {
                    Log.d(TAG, "lockCanvas() failed");
                    break;
                }
                try {
                    // just curious
                    if (canvas.getWidth() != mWidth || canvas.getHeight() != mHeight) {
                        Log.d(TAG, "WEIRD: width/height mismatch");
                    }

                    // Draw the entire window.  If the dirty rect is set we should actually
                    // just be drawing into the area covered by it -- the system lets us draw
                    // whatever we want, then overwrites the areas outside the dirty rect with
                    // the previous contents.  So we've got a lot of overdraw here.
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    for (int i = 0; i < mBullets.size(); i++) {
                        Bullet bullet = mBullets.get(i);
                        if (bullet.x + bullet.measuredWidth >= 0
                                && bullet.x < mScreenWidth) {
                            canvas.drawText(bullet.text, bullet.x, bullet.y, bullet.textPaint);
                        }
                        bullet.x -= Bullet.BulletSpeed.getSpeed(bullet.speed);
                    }
                } finally {
                    // Publish the frame.  If we overrun the consumer, frames will be dropped,
                    // so on a sufficiently fast device the animation will run at faster than
                    // the display refresh rate.
                    //
                    // If the SurfaceTexture has been destroyed, this will throw an exception.
                    try {
                        surface.unlockCanvasAndPost(canvas);
                    } catch (IllegalArgumentException iae) {
                        Log.d(TAG, "unlockCanvasAndPost failed: " + iae.getMessage());
                        break;
                    }
                }

                try {
                    Thread.sleep(MockBulletProvider.DRAW_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            surface.release();
        }

        /**
         * Tells the thread to stop running.
         */
        public void halt() {
            synchronized (mLock) {
                mDone = true;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable(" + width + "x" + height + ")");
            mWidth = width;
            mHeight = height;
            synchronized (mLock) {
                mSurfaceTexture = st;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged(" + width + "x" + height + ")");
            mWidth = width;
            mHeight = height;
        }

        @Override   // will be called on UI thread
        public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
            Log.d(TAG, "onSurfaceTextureDestroyed");

            synchronized (mLock) {
                mSurfaceTexture = null;
            }
            return true;
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureUpdated(SurfaceTexture st) {
            //Log.d(TAG, "onSurfaceTextureUpdated");
        }
    }
}
