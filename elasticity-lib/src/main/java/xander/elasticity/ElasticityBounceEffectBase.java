package xander.elasticity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.util.Log;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import xander.elasticity.adapters.IElasticityAdapter;
import xander.elasticity.adapters.RecyclerViewElasticityAdapter;

import static xander.elasticity.IElasticityState.*;


/**
 * A standalone view decorator adding over-scroll with a smooth bounce-back effect to (potentially) any view -
 * provided that an appropriate {@link IElasticityAdapter} implementation exists / can be written
 * for that view type (e.g. {@link RecyclerViewElasticityAdapter}).
 * <p>
 * <p>Design-wise, being a standalone class, this decorator powerfully provides the ability to add
 * the over-scroll effect over any view without adjusting the view's implementation. In essence, this
 * eliminates the need to repeatedly implement the effect per each view type (list-view,
 * recycler-view, image-view, etc.). Therefore, using it is highly recommended compared to other
 * more intrusive solutions.</p>
 * <p>
 * <p>Note that this class is abstract, having {@link HorizontalElasticityBounceEffect} and
 * {@link VerticalElasticityBounceEffect} providing concrete implementations that are
 * view-orientation specific.</p>
 * <p>
 * <hr width="97%"/>
 * <h2>Implementation Notes</h2>
 * <p>
 * <p>At it's core, the class simply registers itself as a touch-listener over the decorated view and
 * intercepts touch events as needed.</p>
 * <p>
 * <p>Internally, it delegates the over-scrolling calculations onto 3 state-based classes:
 * <ol>
 * <li><b>Idle state</b> - monitors view state and touch events to intercept over-scrolling initiation
 * (in which case it hands control over to the Over-scrolling state).</li>
 * <li><b>Over-scrolling state</b> - handles motion events to apply the over-scroll effect as users
 * interact with the view.</li>
 * <li><b>Bounce-back state</b> - runs the bounce-back animation, all-the-while blocking all
 * touch events till the animation completes (in which case it hands control back to the idle
 * state).</li>
 * </ol>
 * </p>
 *
 * @author amit
 * @see RecyclerViewElasticityAdapter
 * @see IElasticityAdapter
 */
public abstract class ElasticityBounceEffectBase implements IElasticity, View.OnTouchListener {

    public static final String TAG = "OverScrollDecor";

    public static final float DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD = 3f;
    public static final float DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK = 1f;
    public static final float DEFAULT_DECELERATE_FACTOR = -2f;

    public static final float MAX_SCALE_FACTOR = 1.2f;

    private float maxScaleFactor = MAX_SCALE_FACTOR;

    protected static final int MAX_BOUNCE_BACK_DURATION_MS = 1000;
    protected static final int MIN_BOUNCE_BACK_DURATION_MS = 500;

    // 状态变换的时候，记下方向和其他信息
    protected final OverScrollStartAttributes mOverScrollStartAttr = new OverScrollStartAttributes();
    protected final IElasticityAdapter mViewAdapter;

    protected final IdleState mIdleState;
    protected final OverScrollingState mOverScrollingState;
    protected final BounceBackState mBounceBackState;
    protected IDecoratorState mCurrentState;

    protected IElasticityStateListener mStateListener = new ElasticityListenerStubs.ElasticityStateListenerStub();
    protected IElasticityUpdateListener mUpdateListener = new ElasticityListenerStubs.ElasticityUpdateListenerStub();

    /**
     * When in over-scroll mode, keep track of dragging velocity to provide a smooth slow-down
     * for the bounce-back effect.
     */
    protected float mVelocity;

    /**
     * Motion attributes: keeps data describing current motion event.
     * <br/>Orientation agnostic: subclasses provide either horizontal or vertical
     * initialization of the agnostic attributes.
     */
    protected abstract static class MotionAttributes {
        public float mAbsOffset;
        public float mDeltaOffset;
        public boolean mDir; // True = 'forward', false = 'backwards'.

        protected abstract boolean init(View view, MotionEvent event);
    }

    protected static class OverScrollStartAttributes {
        protected int mPointerId;
        protected float mAbsOffset;
        protected boolean mDir; // True = 'forward', false = 'backwards'.
    }

    protected abstract static class AnimationAttributes {
        public Property<View, Float> mProperty;
        public float mAbsOffset;
        public float mMaxOffset;

        protected abstract void init(View view);
    }

    /**
     * Interface of decorator-state delegation classes. Defines states as handles of two fundamental
     * touch events: actual movement, up/cancel.
     */
    protected interface IDecoratorState {

        /**
         * Handle a motion (touch) event.
         *
         * @param event The event from onTouch.
         * @return Return value for onTouch.
         */
        boolean handleMoveTouchEvent(MotionEvent event);

        /**
         * Handle up / touch-cancel events.
         *
         * @param event The event from onTouch.
         * @return Return value for onTouch.
         */
        boolean handleUpOrCancelTouchEvent(MotionEvent event);

        /**
         * Handle a transition onto this state, as it becomes 'current' state.
         *
         * @param fromState
         */
        void handleEntryTransition(IDecoratorState fromState);

        /**
         * The client-perspective ID of the state associated with this (internal) one. ID's
         * are as specified in {@link IElasticityState}.
         *
         * @return The ID, e.g. {@link IElasticityState#STATE_IDLE}.
         */
        int getStateId();
    }

    /**
     * Idle state: monitors move events, trying to figure out whether over-scrolling should be
     * initiated (i.e. when scrolled further when the view is at one of its displayable ends).
     * <br/>When such is the case, it hands over control to the over-scrolling state.
     */
    protected class IdleState implements IDecoratorState {

        final MotionAttributes mMoveAttr;

        public IdleState() {
            mMoveAttr = createMotionAttributes();
        }

        @Override
        public int getStateId() {
            return STATE_IDLE;
        }

        @Override
        public boolean handleMoveTouchEvent(MotionEvent event) {

            final View view = mViewAdapter.getView();
            if (!mMoveAttr.init(view, event)) {
                return false;
            }

            // Has over-scrolling officially started?
            if ((mViewAdapter.isInAbsoluteStart() && mMoveAttr.mDir) ||
                    (mViewAdapter.isInAbsoluteEnd() && !mMoveAttr.mDir)) {

                // Save initial over-scroll attributes for future reference.
                mOverScrollStartAttr.mPointerId = event.getPointerId(0);
                mOverScrollStartAttr.mAbsOffset = mMoveAttr.mAbsOffset;
                mOverScrollStartAttr.mDir = mMoveAttr.mDir;

                issueStateTransition(mOverScrollingState);
                return mOverScrollingState.handleMoveTouchEvent(event);
            }

            return false;
        }

        @Override
        public boolean handleUpOrCancelTouchEvent(MotionEvent event) {
            return false;
        }

        @Override
        public void handleEntryTransition(IDecoratorState fromState) {
            mStateListener.onOverScrollStateChange(ElasticityBounceEffectBase.this, fromState.getStateId(), this.getStateId());
        }
    }

    /**
     * Handles the actual over-scrolling: thus translating the view according to configuration
     * and user interactions, dynamically.
     * <p>
     * <br/><br/>The state is exited - thus completing over-scroll handling, in one of two cases:
     * <br/>When user lets go of the view, it transitions control to the bounce-back state.
     * <br/>When user moves the view back onto a potential 'under-scroll' state, it abruptly
     * transitions control to the idle-state, so as to return touch-events management to the
     * normal over-scroll-less environment (thus preventing under-scrolling and potentially regaining
     * regular scrolling).
     */
    protected class OverScrollingState implements IDecoratorState {

        protected final float mTouchDragRatioFwd;
        protected final float mTouchDragRatioBck;

        final MotionAttributes mMoveAttr;
        int mCurrDragState;

        public OverScrollingState(float touchDragRatioFwd, float touchDragRatioBck) {
            mMoveAttr = createMotionAttributes();
            mTouchDragRatioFwd = touchDragRatioFwd;
            mTouchDragRatioBck = touchDragRatioBck;
        }

        @Override
        public int getStateId() {
            // This is really a single class that implements 2 states, so our ID depends on what
            // it was during the last invocation.
            return mCurrDragState;
        }

        @Override
        public boolean handleMoveTouchEvent(MotionEvent event) {

            // Switching 'pointers' (e.g. fingers) on-the-fly isn't supported -- abort over-scroll
            // smoothly using the default bounce-back animation in this case.
            if (mOverScrollStartAttr.mPointerId != event.getPointerId(0)) {
                issueStateTransition(mBounceBackState);
                return true;
            }

            final View view = mViewAdapter.getView();
            if (!mMoveAttr.init(view, event)) {
                // Keep intercepting the touch event as long as we're still over-scrolling...
                return true;
            }

            float deltaOffset = mMoveAttr.mDeltaOffset / (mMoveAttr.mDir == mOverScrollStartAttr.mDir ? mTouchDragRatioFwd : mTouchDragRatioBck);
            float newOffset = mMoveAttr.mAbsOffset + deltaOffset;

            // If moved in counter direction onto a potential under-scroll state -- don't. Instead, abort
            // over-scrolling abruptly, thus returning control to which-ever touch handlers there
            // are waiting (e.g. regular scroller handlers).
            if ((mOverScrollStartAttr.mDir && !mMoveAttr.mDir && (newOffset <= mOverScrollStartAttr.mAbsOffset)) ||
                    (!mOverScrollStartAttr.mDir && mMoveAttr.mDir && (newOffset >= mOverScrollStartAttr.mAbsOffset))) {
                translateViewAndEvent(view, mOverScrollStartAttr.mDir, mOverScrollStartAttr.mAbsOffset, event);
                mUpdateListener.onOverScrollUpdate(ElasticityBounceEffectBase.this, mCurrDragState, 0);

                issueStateTransition(mIdleState);
                return true;
            }

            if (view.getParent() != null) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            }

            long dt = event.getEventTime() - event.getHistoricalEventTime(0);
            if (dt > 0) { // Sometimes (though rarely) dt==0 cause originally timing is in nanos, but is presented in millis.
                mVelocity = deltaOffset / dt;
            }

            translateView(view, mOverScrollStartAttr.mDir, newOffset);
            mUpdateListener.onOverScrollUpdate(ElasticityBounceEffectBase.this, mCurrDragState, newOffset);

            return true;
        }

        @Override
        public boolean handleUpOrCancelTouchEvent(MotionEvent event) {
            issueStateTransition(mBounceBackState);
            return true;
        }

        @Override
        public void handleEntryTransition(IDecoratorState fromState) {
            mCurrDragState = (mOverScrollStartAttr.mDir ? STATE_DRAG_START_SIDE : STATE_DRAG_END_SIDE);
            mStateListener.onOverScrollStateChange(ElasticityBounceEffectBase.this, fromState.getStateId(), this.getStateId());
        }
    }

    /**
     * When entered, starts the bounce-back animation.
     * <br/>Upon animation completion, transitions control onto the idle state; Does so by
     * registering itself as an animation listener.
     * <br/>In the meantime, blocks (intercepts) all touch events.
     */
    protected class BounceBackState implements IDecoratorState, Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

        protected final Interpolator mBounceBackInterpolator = new BounceInterpolator();
        protected final float mDecelerateFactor;
        protected final float mDoubleDecelerateFactor;

        protected final AnimationAttributes mAnimAttributes;

        public BounceBackState(float decelerateFactor) {
            mDecelerateFactor = decelerateFactor;
            mDoubleDecelerateFactor = 2f * decelerateFactor;

            mAnimAttributes = createAnimationAttributes();
        }

        @Override
        public int getStateId() {
            return STATE_BOUNCE_BACK;
        }

        @Override
        public void handleEntryTransition(IDecoratorState fromState) {
            mStateListener.onOverScrollStateChange(ElasticityBounceEffectBase.this, fromState.getStateId(), this.getStateId());
            Animator bounceBackAnim = createAnimator();
            bounceBackAnim.addListener(this);
            bounceBackAnim.start();
        }

        @Override
        public boolean handleMoveTouchEvent(MotionEvent event) {
            // Flush all touches down the drain till animation is over.
            return true;
        }

        @Override
        public boolean handleUpOrCancelTouchEvent(MotionEvent event) {
            // Flush all touches down the drain till animation is over.
            return true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            issueStateTransition(mIdleState);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            translateView(mViewAdapter.getView(), mOverScrollStartAttr.mDir, (Float) animation.getAnimatedValue());
            mUpdateListener.onOverScrollUpdate(ElasticityBounceEffectBase.this, STATE_BOUNCE_BACK, (Float) animation.getAnimatedValue());
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            issueStateTransition(mIdleState);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        protected Animator createAnimator() {
            final View view = mViewAdapter.getView();
            mAnimAttributes.init(view);

            // Set up a low-duration slow-down animation IN the drag direction.

            // Exception: If wasn't dragging in 'forward' direction (or velocity=0 -- i.e. not dragging at all),
            // skip slow-down anim directly to the bounce-back.
            if (mVelocity == 0f || (mVelocity < 0 && mOverScrollStartAttr.mDir) || (mVelocity > 0 && !mOverScrollStartAttr.mDir)) {
                return createBounceBackAnimator(mAnimAttributes.mAbsOffset, 0);
            }

            // dt = (Vt - Vo) / a; Vt=0 ==> dt = -Vo / a
            float slowdownDuration = -mVelocity / mDecelerateFactor;
            slowdownDuration = (slowdownDuration < 0 ? 0 : slowdownDuration); // Happens in counter-direction dragging

            // dx = (Vt^2 - Vo^2) / 2a; Vt=0 ==> dx = -Vo^2 / 2a
            float slowdownDistance = -mVelocity * mVelocity / mDoubleDecelerateFactor;
            float slowdownEndOffset = mAnimAttributes.mAbsOffset + slowdownDistance;

            ValueAnimator slowdownAnim = createSlowdownAnimator((int) slowdownDuration, mAnimAttributes.mAbsOffset, slowdownEndOffset);

            // Set up the bounce back animation, bringing the view back into the original, pre-overscroll position (translation=0).
            ValueAnimator bounceBackAnim = createBounceBackAnimator(slowdownEndOffset, 0);

            // Play the 2 animations as a sequence.
            AnimatorSet wholeAnim = new AnimatorSet();
            wholeAnim.playSequentially(slowdownAnim, bounceBackAnim);
            return wholeAnim;
        }

        protected ValueAnimator createSlowdownAnimator(int slowdownDuration, float slowdownStartOffset, float slowdownEndOffset) {
            ValueAnimator slowdownAnim = ValueAnimator.ofFloat(slowdownStartOffset, slowdownEndOffset);
            slowdownAnim.setDuration(slowdownDuration);
            slowdownAnim.setInterpolator(new DecelerateInterpolator());
            slowdownAnim.addUpdateListener(this);
            return slowdownAnim;
        }

        protected ValueAnimator createBounceBackAnimator(float startOffset, float endOffset) {
            // Duration is proportional to the view's size.
            float bounceBackDuration = (Math.abs(startOffset) / mAnimAttributes.mMaxOffset) * MAX_BOUNCE_BACK_DURATION_MS;
            ValueAnimator bounceBackAnim = ValueAnimator.ofFloat(startOffset, endOffset);
            bounceBackAnim.setDuration(Math.max((int) bounceBackDuration, MIN_BOUNCE_BACK_DURATION_MS));
            bounceBackAnim.setInterpolator(mBounceBackInterpolator);
            bounceBackAnim.addUpdateListener(this);
            return bounceBackAnim;
        }
    }

    public ElasticityBounceEffectBase(IElasticityAdapter viewAdapter, float decelerateFactor, float scaleFactor ,float touchDragRatioFwd, float touchDragRatioBck) {
        mViewAdapter = viewAdapter;

        mIdleState = new IdleState();
        mOverScrollingState = new OverScrollingState(touchDragRatioFwd, touchDragRatioBck);
        mBounceBackState = new BounceBackState(decelerateFactor);

        mCurrentState = mIdleState;

        maxScaleFactor = scaleFactor;

        attach();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                return mCurrentState.handleMoveTouchEvent(event);

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                return mCurrentState.handleUpOrCancelTouchEvent(event);
        }

        return false;
    }

    @Override
    public void setOverScrollStateListener(IElasticityStateListener listener) {
        mStateListener = (listener != null ? listener : new ElasticityListenerStubs.ElasticityStateListenerStub());
    }

    @Override
    public void setOverScrollUpdateListener(IElasticityUpdateListener listener) {
        mUpdateListener = (listener != null ? listener : new ElasticityListenerStubs.ElasticityUpdateListenerStub());
    }

    @Override
    public int getCurrentState() {
        return mCurrentState.getStateId();
    }

    @Override
    public View getView() {
        return mViewAdapter.getView();
    }

    protected void issueStateTransition(IDecoratorState state) {
        IDecoratorState oldState = mCurrentState;
        mCurrentState = state;
        mCurrentState.handleEntryTransition(oldState);
    }

    protected void attach() {
        getView().setOnTouchListener(this);
        getView().setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    public void detach() {
        if (mCurrentState != mIdleState) {
            Log.w(TAG, "Decorator detached while over-scroll is in effect. You might want to add a precondition of that getCurrentState()==STATE_IDLE, first.");
        }
        getView().setOnTouchListener(null);
        getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    }

    protected abstract MotionAttributes createMotionAttributes();

    protected abstract AnimationAttributes createAnimationAttributes();

    protected abstract void translateView(View view, boolean dir, float offset);

    protected abstract void translateViewAndEvent(View view, boolean dir, float offset, MotionEvent event);

    protected float getViewOffset(View view) {
        if (view.getTag(R.id.offsetValue) != null) {
            return (float) view.getTag(R.id.offsetValue);
        }
        return 0;
    }

    protected void setViewOffset(View view, float offset) {
        view.setTag(R.id.offsetValue, offset);
    }

    protected float getMaxScaleFactor() {
        return maxScaleFactor;
    }

}
