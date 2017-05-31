package xander.elasticity.demo.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import xander.elasticity.ElasticityHelper;
import xander.elasticity.demo.R;
import xander.elasticity.demo.control.DemoContentHelper;
import xander.elasticity.demo.control.DemoItem;

/**
 * @author amitd
 */
public class ListViewDemoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.listview_overscroll_demo, null, false);
        initVerticalListView(DemoContentHelper.getSpectrumItems(getResources()), (ListView) fragmentView.findViewById(R.id.list_view));
        return fragmentView;
    }

    private void initVerticalListView(List<DemoItem> content, ListView listView) {
        LayoutInflater appInflater = LayoutInflater.from(getActivity().getApplicationContext());
        ListAdapter adapter = new DemoListAdapter(appInflater, content);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(),"click " + position,Toast.LENGTH_SHORT).show();
            }
        });

        ElasticityHelper.setUpOverScroll(listView);
    }
}
