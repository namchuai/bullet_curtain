package ai.techlab.bulletcurtain;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockBulletProvider {
    public static final int DRAW_RATE = 16;
    public final int mScreenWidth;
    public static final int MARGIN_BETWEEN_BULLET = 46; // TODO convert from dp to pixel
    public static final int VIDEO_TIME_IN_MILLIS = 1000;
    private static final int TEXT_SIZE = 48; // TODO

    private static final int TRACK_PADDING = 16;

    private static final int[] colors = {
            Color.BLACK,
//            Color.DKGRAY,
//            Color.GRAY,
//            Color.LTGRAY,
//            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA
    };

    private Resources resources;

    public MockBulletProvider(Resources resources) {
        this.resources = resources;
        mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private static final String[] comments = {
            "Chúc mừng bạn đã vào khu dân cư may mắn nhất năm",
            "Page mất hút lâu vãi. Search thì vẫn có mà k thấy tương tác đâu :)))))",
            "Chờ page mãi",
            "Fage quay lại thật rồi",
            "Tuyệt vời",
            "tính bán đi, đang tính bán thêm torreira gom tiền mua aouar",
            "Martinez muốn đi để đc bắt chính nhiều hơn nên ko kí hợp đồng mới chứ ko phải Ars muốn bán.",
            "Hợp lí",
            "đây mới là bản hợp đồng chất lượng nhất hè này nhé !",
            "Ad lặn lâu thế",
            "Hợp đồng thành công nhất đây rồi!",
            "Ơn zời! Page đây rồi!!",
            "Đánh mất truyền thống bán đội trưởng rồi, ai đó trả lại Arsenal trước đây cho tôi đi",
    };

    public List<Bullet> getBullets() {
        int count = 3000;
        List<Bullet> bullets = new ArrayList<>();

        List<Bullet> lowTrack = new ArrayList<>();
        List<Bullet> midTrack = new ArrayList<>();
        List<Bullet> highTrack = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Bullet bullet;
            if (i % 3 == 0) {
                lowTrack.add(bullet = new Bullet.Builder(String.valueOf(i)
                        , comments[new Random(System.currentTimeMillis()).nextInt(comments.length - 1)]
                        , new Random(System.currentTimeMillis()).nextInt(VIDEO_TIME_IN_MILLIS))
                        .withSpeed(Bullet.BulletSpeed.LOW)
                        .withTextPaint(provideRandomTextPaint())
                        .build());
            } else if (i % 3 == 1) {
                midTrack.add(bullet = new Bullet.Builder(String.valueOf(i)
                        , comments[new Random(System.currentTimeMillis()).nextInt(comments.length - 1)]
                        , new Random(System.currentTimeMillis()).nextInt(VIDEO_TIME_IN_MILLIS))
                        .withSpeed(Bullet.BulletSpeed.MEDIUM)
                        .withTextPaint(provideRandomTextPaint())
                        .build());
            } else {
                highTrack.add(bullet = new Bullet.Builder(String.valueOf(i)
                        , comments[new Random(System.currentTimeMillis()).nextInt(comments.length - 1)]
                        , new Random(System.currentTimeMillis()).nextInt(VIDEO_TIME_IN_MILLIS))
                        .withSpeed(Bullet.BulletSpeed.HIGH)
                        .withTextPaint(provideRandomTextPaint())
                        .build());
            }
            bullet.measureSelf();

            bullet.y = (int) bullet.measuredHeight * (bullet.speed == Bullet.BulletSpeed.LOW ? 1 : bullet.speed == Bullet.BulletSpeed.MEDIUM ? 2 : 3)
                    + TRACK_PADDING * (bullet.speed == Bullet.BulletSpeed.LOW ? 0 : bullet.speed == Bullet.BulletSpeed.MEDIUM ? 1 : 2);
        }

        // calculate x based on time for each track
        calculateXPos(lowTrack);
        calculateXPos(midTrack);
        calculateXPos(highTrack);

        bullets.addAll(lowTrack);
        bullets.addAll(midTrack);
        bullets.addAll(highTrack);

        return bullets;
    }

    private TextPaint provideRandomTextPaint() {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setLinearText(true);
        textPaint.setTypeface(resources.getFont(R.font.nunitosans_regular));
        textPaint.setColor(colors[new Random(System.currentTimeMillis()).nextInt(colors.length - 1)]);
        return textPaint;
    }

    private void calculateXPos(List<Bullet> bullets) {
        int padding = mScreenWidth;
        for (int i = 0; i < bullets.size(); i++) {
            // firstly we calculate the prefer x by its time
            int preferX = (int) (bullets.get(i).timeStampDisplay * Bullet.BulletSpeed.getSpeed(bullets.get(i).speed));
            if (padding > preferX) {
                bullets.get(i).x = padding;
                padding += bullets.get(i).measuredWidth;
            } else {
                bullets.get(i).x = preferX;
                padding += preferX + bullets.get(i).measuredWidth;
            }
            padding += MARGIN_BETWEEN_BULLET;
        }
    }
}
