package xander.elasticity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import xander.elasticity.adapters.IElasticityAdapter;

/**
 * A concrete implementation of {@link ElasticityBounceEffectBase} for a horizontal orientation.
 *
 * @author amit
 */
public class HorizontalElasticityBounceEffect extends ElasticityBounceEffectBase {

    protected class MotionAttributesHorizontal extends MotionAttributes {

        public boolean init(View view, MotionEvent event) {

            // We must have history available to calc the dx. Normally it's there - if it isn't temporarily,
            // we declare the event 'invalid' and expect it in consequent events.
            if (event.getHistorySize() == 0) {
                return false;
            }

            // Allow for counter-orientation-direction operations (e.g. item swiping) to run fluently.
            final float dx = event.getX(0) - event.getHistoricalX(0, 0);
            final float dy = event.getY(0) - event.getHistoricalY(0, 0);
            if (Math.abs(dx) < Math.abs(dy)) {
                return false;
            }

            if( dx == 0.f ) { // just click event
                return false;
            }

//            mAbsOffset = view.getTranslationX();
            mAbsOffset = getViewOffset(view);
            mDeltaOffset = dx;
            mDir = mDeltaOffset > 0;
            Log.d("wxy-motion", String.format("mAbsOffset %s mDeltaOffset %s", mAbsOffset, mDeltaOffset));
            Log.d("wxy-motion","mDir = " + mDir);
            return true;
        }
    }

    protected class AnimationAttributesHorizontal extends AnimationAttributes {

        public AnimationAttributesHorizontal() {
            mProperty = View.TRANSLATION_X;
//            mProperty = View.SCALE_X;
        }

        @Override
        protected void init(View view) {
//            mAbsOffset = view.getTranslationX();
            mAbsOffset = getViewOffset(view);
            mMaxOffset = view.getWidth();
        }
    }

    /**
     * C'tor, creating the effect with default arguments:
     * <br/>Touch-drag ratio in 'forward' direction will be set to DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD.
     * <br/>Touch-drag ratio in 'backwards' direction will be set to DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK.
     * <br/>Deceleration factor (for the bounce-back effect) will be set to DEFAULT_DECELERATE_FACTOR.
     *
     * @param viewAdapter The view's encapsulation.
     */
    public HorizontalElasticityBounceEffect(IElasticityAdapter viewAdapter) {
        this(viewAdapter, DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD, DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK, DEFAULT_DECELERATE_FACTOR, MAX_SCALE_FACTOR);
    }

    public HorizontalElasticityBounceEffect(IElasticityAdapter viewAdapter, float scaleFactor) {
        this(viewAdapter, DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD, DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK, DEFAULT_DECELERATE_FACTOR, scaleFactor);
    }

    /**
     * C'tor, creating the effect with explicit arguments.
     *
     * @param viewAdapter       The view's encapsulation.
     * @param touchDragRatioFwd Ratio of touch distance to actual drag distance when in 'forward' direction.
     * @param touchDragRatioBck Ratio of touch distance to actual drag distance when in 'backward'
     *                          direction (opposite to initial one).
     * @param decelerateFactor  Deceleration factor used when decelerating the motion to create the
     *                          bounce-back effect.
     */
    public HorizontalElasticityBounceEffect(IElasticityAdapter viewAdapter, float touchDragRatioFwd, float touchDragRatioBck,
                                            float decelerateFactor, float scaleFactor) {
        super(viewAdapter, decelerateFactor, scaleFactor, touchDragRatioFwd, touchDragRatioBck);
    }

    @Override
    protected MotionAttributes createMotionAttributes() {
        return new MotionAttributesHorizontal();
    }

    @Override
    protected AnimationAttributes createAnimationAttributes() {
        return new AnimationAttributesHorizontal();
    }

    @Override
    protected void translateView(View view, boolean dir, float offset) {
        Log.d("wxy-motion", String.format("translateView setTag %s", offset));
        setViewOffset(view, offset);
        view.setPivotY(0.f);
        if (dir) {
            Log.d("wxy-motion", String.format("setPivotX setTag %s", 0));
            view.setPivotX(0.f);
        } else {
            Log.d("wxy-motion", String.format("setPivotX setTag %s", view.getMeasuredWidth()));
            view.setPivotX(view.getMeasuredWidth());
        }
        view.setScaleX(Math.min(getMaxScaleFactor(), (1.f + Math.abs(offset) / view.getWidth())));
        view.postInvalidate();
//        view.setTranslationX(offset);
    }

    @Override
    protected void translateViewAndEvent(View view, boolean dir, float offset, MotionEvent event) {
        Log.d("wxy-motion", String.format("translateViewAndEvent setTag %s", offset));
        translateView(view, dir, offset);

//        view.setTranslationX(offset);
        event.offsetLocation(offset - event.getX(0), 0f);
    }
}
