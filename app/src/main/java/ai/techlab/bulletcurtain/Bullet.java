package ai.techlab.bulletcurtain;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.Nullable;

public final class Bullet {
    /**
     * Bullet speed will affect which track the bullet is being drawn.
     */
    public enum BulletSpeed {
        LOW,
        MEDIUM,
        HIGH;

        public static int getSpeed(BulletSpeed bulletSpeed) {
            switch (bulletSpeed) {
                case LOW:
                    return 2;
                case MEDIUM:
                    return 3;
                case HIGH:
                    return 4;
                default:
                    return 0;
            }
        }
    }

    /**
     * This used to identify a bullet. This should be equals to
     * comment id. Two bullet are considered equal if they have
     * same bulletId.
     */
    public String bulletId;

    /**
     * For custom font drawing
     */
    public TextPaint textPaint;

    /**
     * pixel / milliseconds
     */
    public BulletSpeed speed;

    /**
     * Current position of this bullet
     */
    public int x = -1;
    public int y = -1;

    /**
     * Store the measured text length so we don't need
     * to re-measured it again.
     * <p>
     * -1 means that undefined yet
     */
    public float measuredWidth = -1;
    public float measuredHeight = -1;

    public Paint mPaint;

    /**
     * Text to display
     */
    public String text;

    /**
     * Corresponding to time stamp of video. Measured
     * in milliseconds. This is the time when the Bullet is being
     * drawn.
     */
    public int timeStampDisplay;

    /**
     * Indicate that if a bullet is exceed its out of the screen and
     * should be removed from the list.
     */
    public volatile boolean isFinished;

    private Bullet() {
    }

    public String currentPos() {
        return text + "-" + "(" + x + ":" + y + ")";
    }

    public void measureSelf() {
        int width = Math.round(textPaint.measureText(text));
        StaticLayout staticLayout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL
                , 1.0f, 0, false);
        int height = staticLayout.getHeight();
        measuredWidth = width;
        measuredHeight = height;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Bullet)) {
            return false;
        }
        return this.bulletId.equals(((Bullet) obj).bulletId);
    }

    public static class Builder {
        private final String id;
        private final String comment;
        private final int timeToDisplay;

        private TextPaint textPaint;
        private BulletSpeed speed;

        public Builder(String id, String comment, int timeToDisplay) {
            this.id = id;
            this.comment = comment;
            this.timeToDisplay = timeToDisplay;
        }

        /**
         * If speed is not specified, BulletSpeed.HIGH will be used.
         *
         * @param speed
         */
        public Builder withSpeed(BulletSpeed speed) {
            this.speed = speed;
            return this;
        }

        public Builder withTextPaint(TextPaint textPaint) {
            this.textPaint = textPaint;
            return this;
        }

        public Bullet build() {
            if (textPaint == null) {
                textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setTextSize(46); // TODO
                textPaint.setColor(Color.BLUE);
                textPaint.setLinearText(true);
            }
            if (speed == null) {
                speed = BulletSpeed.LOW;
            }

            Bullet bullet = new Bullet();
            bullet.bulletId = id;
            bullet.text = comment;
            bullet.timeStampDisplay = timeToDisplay;
            bullet.textPaint = textPaint;
            bullet.speed = speed;

            return bullet;
        }
    }
}
