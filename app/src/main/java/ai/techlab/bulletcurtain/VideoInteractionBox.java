package ai.techlab.bulletcurtain;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class VideoInteractionBox extends LinearLayout {
    public VideoInteractionBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.video_interaction_box, this, true);
    }
}
