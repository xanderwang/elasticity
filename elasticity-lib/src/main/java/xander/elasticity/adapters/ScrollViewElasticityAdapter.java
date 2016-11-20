package xander.elasticity.adapters;

import android.view.View;
import android.widget.ScrollView;

import xander.elasticity.HorizontalElasticityBounceEffect;
import xander.elasticity.VerticalElasticityBounceEffect;

/**
 * An adapter that enables over-scrolling over a {@link ScrollView}.
 * <br/>Seeing that {@link ScrollView} only supports vertical scrolling, this adapter
 * should only be used with a {@link VerticalElasticityBounceEffect}. For horizontal
 * over-scrolling, use {@link HorizontalScrollViewElasticityAdapter} in conjunction with
 * a {@link android.widget.HorizontalScrollView}.
 *
 * @author amit
 *
 * @see HorizontalElasticityBounceEffect
 * @see VerticalElasticityBounceEffect
 */
public class ScrollViewElasticityAdapter implements IElasticityAdapter {

    protected final ScrollView mView;

    public ScrollViewElasticityAdapter(ScrollView view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return !mView.canScrollVertically(-1);
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return !mView.canScrollVertically(1);
    }
}
