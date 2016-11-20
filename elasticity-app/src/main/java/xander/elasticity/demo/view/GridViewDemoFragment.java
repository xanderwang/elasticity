package xander.elasticity.demo.view;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;

import java.util.List;

import xander.elasticity.ElasticityHelper;
import xander.elasticity.demo.R;
import xander.elasticity.demo.control.DemoContentHelper;
import xander.elasticity.demo.control.DemoItem;

/**
 * @author amitd
 */
public class GridViewDemoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.gridview_overscroll_demo, null, false);
        initVerticalGridView(DemoContentHelper.getSpectrumItems(getResources()), (GridView) fragmentView.findViewById(R.id.grid_view));
        return fragmentView;
    }

    private void initVerticalGridView(List<DemoItem> content, GridView gridView) {
        LayoutInflater appInflater = LayoutInflater.from(getActivity().getApplicationContext());
        ListAdapter adapter = new DemoGridAdapter(appInflater, content);
        gridView.setAdapter(adapter);

        ElasticityHelper.setUpOverScroll(gridView);
    }
}
