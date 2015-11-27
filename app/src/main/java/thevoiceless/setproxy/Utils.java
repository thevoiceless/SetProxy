package thevoiceless.setproxy;

import android.animation.Animator;
import android.text.Editable;

/**
 * Created by riley on 11/26/15.
 */
public class Utils {

    public static class AnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) { }

        @Override
        public void onAnimationCancel(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) { }
    }

    public static class TextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) { }
    }
}
