package xander.elasticity;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ScrollView;

import xander.elasticity.adapters.AbsListViewElasticityAdapter;
import xander.elasticity.adapters.HorizontalScrollViewElasticityAdapter;
import xander.elasticity.adapters.RecyclerViewElasticityAdapter;
import xander.elasticity.adapters.ScrollViewElasticityAdapter;
import xander.elasticity.adapters.StaticElasticityAdapter;
import xander.elasticity.adapters.ViewPagerElasticityAdapter;

/**
 * @author amit
 */
public class ElasticityHelper {

    /**
     * Set up the over-scroll effect over a specified {@link RecyclerView} view.
     * <br/>Only recycler-views using <b>native</b> Android layout managers (i.e. {@link LinearLayoutManager},
     * {@link GridLayoutManager} and {@link StaggeredGridLayoutManager}) are currently supported
     * by this convenience method.
     *
     * @param recyclerView The view.
     * @param orientation  One of {@link ORIENTATION}
     * @return The over-scroll effect 'decorator', enabling further effect configuration.
     */
    public static IElasticity setUpOverScroll(RecyclerView recyclerView, ORIENTATION orientation) {
        return setUpOverScroll(recyclerView, orientation, ElasticityBounceEffectBase.MAX_SCALE_FACTOR);
    }

    /**
     * Set up the over-scroll effect over a specified {@link RecyclerView} view.
     * <br/>Only recycler-views using <b>native</b> Android layout managers (i.e. {@link LinearLayoutManager},
     * {@link GridLayoutManager} and {@link StaggeredGridLayoutManager}) are currently supported
     * by this convenience method.
     *
     * @param recyclerView The view.
     * @param orientation  One of {@link ORIENTATION}
     * @param scaleFactor  the scale factor for view , defalte is 1.2f
     * @return The over-scroll effect 'decorator', enabling further effect configuration.
     */
    public static IElasticity setUpOverScroll(RecyclerView recyclerView, ORIENTATION orientation, float scaleFactor) {
        if (orientation == ORIENTATION.HORIZONTAL) {
            return new HorizontalElasticityBounceEffect(new RecyclerViewElasticityAdapter(recyclerView), scaleFactor);
        } else if (orientation == ORIENTATION.VERTICAL) {
            return new VerticalElasticityBounceEffect(new RecyclerViewElasticityAdapter(recyclerView), scaleFactor);
        } else {
            throw new IllegalArgumentException("orientation");
        }
    }

    public static IElasticity setUpOverScroll(ListView listView, float scaleFactor) {
        return new VerticalElasticityBounceEffect(new AbsListViewElasticityAdapter(listView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(ListView listView) {
        return setUpOverScroll(listView, ElasticityBounceEffectBase.MAX_SCALE_FACTOR);
    }

    public static IElasticity setUpOverScroll(GridView gridView, float scaleFactor) {
        return new VerticalElasticityBounceEffect(new AbsListViewElasticityAdapter(gridView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(GridView gridView) {
        return setUpOverScroll(gridView, ElasticityBounceEffectBase.MAX_SCALE_FACTOR);
    }

    public static IElasticity setUpOverScroll(ScrollView scrollView, float scaleFactor) {
        return new VerticalElasticityBounceEffect(new ScrollViewElasticityAdapter(scrollView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(ScrollView scrollView) {
        return setUpOverScroll(scrollView, ElasticityBounceEffectBase.MAX_SCALE_FACTOR);
    }

    public static IElasticity setUpOverScroll(HorizontalScrollView scrollView, float scaleFactor) {
        return new HorizontalElasticityBounceEffect(new HorizontalScrollViewElasticityAdapter(scrollView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(HorizontalScrollView scrollView) {
        return setUpOverScroll(scrollView, ElasticityBounceEffectBase.MAX_SCALE_FACTOR);
    }

    /**
     * Set up the over-scroll over a generic view, assumed to always be over-scroll ready (e.g.
     * a plain text field, image view).
     *
     * @param view        The view.
     * @param orientation One {@link ORIENTATION}
     * @return The over-scroll effect 'decorator', enabling further effect configuration.
     */
    public static IElasticity setUpStaticOverScroll(View view, ORIENTATION orientation) {
        return setUpStaticOverScroll(view, orientation, ElasticityBounceEffectBase.MAX_SCALE_FACTOR);
    }

    /**
     * Set up the over-scroll over a generic view, assumed to always be over-scroll ready (e.g.
     * a plain text field, image view).
     *
     * @param view        The view.
     * @param orientation One {@link ORIENTATION}
     * @param scaleFactor the scale factor for view , defalte is 1.2f
     * @return The over-scroll effect 'decorator', enabling further effect configuration.
     */
    public static IElasticity setUpStaticOverScroll(View view, ORIENTATION orientation, float scaleFactor) {
        if (orientation == ORIENTATION.HORIZONTAL) {
            return new HorizontalElasticityBounceEffect(new StaticElasticityAdapter(view), scaleFactor);
        } else if (orientation == ORIENTATION.VERTICAL) {
            return new VerticalElasticityBounceEffect(new StaticElasticityAdapter(view), scaleFactor);
        } else {
            throw new IllegalArgumentException("orientation");
        }
    }

    public static IElasticity setUpOverScroll(ViewPager viewPager, float scaleFactor) {
        return new HorizontalElasticityBounceEffect(new ViewPagerElasticityAdapter(viewPager), scaleFactor);
    }

    public static IElasticity setUpOverScroll(ViewPager viewPager) {
        return setUpOverScroll(viewPager, ElasticityBounceEffectBase.MAX_SCALE_FACTOR);
    }

}
