package xander.elasticity.adapters;

import android.view.View;

import xander.elasticity.HorizontalElasticityBounceEffect;
import xander.elasticity.VerticalElasticityBounceEffect;

/**
 * A static adapter for views that are ALWAYS over-scroll-able (e.g. image view).
 *
 * @author amit
 *
 * @see HorizontalElasticityBounceEffect
 * @see VerticalElasticityBounceEffect
 */
public class StaticElasticityAdapter implements IElasticityAdapter {

    protected final View mView;

    public StaticElasticityAdapter(View view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return true;
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return true;
    }
}
