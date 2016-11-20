package xander.elasticity.demo.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import xander.elasticity.demo.R;
import xander.elasticity.demo.control.DemoItem;

/**
 * @author amit
 */
public class DemoGridAdapter extends DemoListAdapterBase {

    protected DemoGridAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    public DemoGridAdapter(LayoutInflater inflater, List<DemoItem> items) {
        super(inflater, items);
    }

    @Override
    protected DemoViewHolder createViewHolder(ViewGroup parent) {
        return new DemoViewHolder(R.layout.grid_item, parent, mInflater);
    }
}
