package xander.elasticity.demo.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import xander.elasticity.demo.R;
import xander.elasticity.demo.control.DemoItem;

/**
 * @author amit
 */
public class DemoListAdapter extends DemoListAdapterBase {

    protected DemoListAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    public DemoListAdapter(LayoutInflater inflater, List<DemoItem> items) {
        super(inflater, items);
    }

    @Override
    protected DemoViewHolder createViewHolder(ViewGroup parent) {
        return new DemoViewHolder(R.layout.vertical_list_item, parent, mInflater);
    }
}
