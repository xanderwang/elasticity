package xander.elasticity.demo.view;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;

import xander.elasticity.ElasticityHelper;
import xander.elasticity.ORIENTATION;
import xander.elasticity.demo.R;

/**
 * @author amitd
 */
public class MiscViewsDemoFragment extends Fragment {

    private static final String CHRONO_TIME_SAVE_ID = "chronoTime";

    private Chronometer mChrono;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.misc_overscroll_demo, null, false);

        View textView = fragmentView.findViewById(R.id.demo_text);
        ElasticityHelper.setUpStaticOverScroll(textView, ORIENTATION.HORIZONTAL);

        View imageView = fragmentView.findViewById(R.id.demo_image);
        ElasticityHelper.setUpStaticOverScroll(imageView, ORIENTATION.VERTICAL);

        mChrono = (Chronometer) fragmentView.findViewById(R.id.demo_chronometer);
        if (savedInstanceState != null) {
            mChrono.setBase(savedInstanceState.getLong(CHRONO_TIME_SAVE_ID));
        }
        ElasticityHelper.setUpStaticOverScroll(mChrono, ORIENTATION.HORIZONTAL);
        mChrono.start();

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(CHRONO_TIME_SAVE_ID, mChrono.getBase());

        super.onSaveInstanceState(outState);
    }
}
