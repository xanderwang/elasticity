package xander.elasticity.adapters;

import android.view.View;
import android.widget.AbsListView;

import xander.elasticity.HorizontalElasticityBounceEffect;
import xander.elasticity.VerticalElasticityBounceEffect;

/**
 * An adapter to enable over-scrolling over object of {@link AbsListView}, namely {@link
 * android.widget.ListView} and it's extensions, and {@link android.widget.GridView}.
 *
 * @author amit
 *
 * @see HorizontalElasticityBounceEffect
 * @see VerticalElasticityBounceEffect
 */
public class AbsListViewElasticityAdapter implements IElasticityAdapter {

    protected final AbsListView mView;

    public AbsListViewElasticityAdapter(AbsListView view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return mView.getChildCount() > 0 && !canScrollListUp();
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return mView.getChildCount() > 0 && !canScrollListDown();
    }

    private boolean canScrollListUp() {
        // Ported from AbsListView#canScrollList() which isn't compatible to all API levels
        final int firstTop = mView.getChildAt(0).getTop();
        final int firstPosition = mView.getFirstVisiblePosition();
        return firstPosition > 0 || firstTop < mView.getListPaddingTop();
    }

    private boolean canScrollListDown() {
        // Ported from AbsListView#canScrollList() which isn't compatible to all API levels
        final int childCount = mView.getChildCount();
        final int itemsCount = mView.getCount();
        final int firstPosition = mView.getFirstVisiblePosition();
        final int lastPosition = firstPosition + childCount;
        final int lastBottom = mView.getChildAt(childCount - 1).getBottom();
        return lastPosition < itemsCount || lastBottom > mView.getHeight() - mView.getListPaddingBottom();
    }
}
