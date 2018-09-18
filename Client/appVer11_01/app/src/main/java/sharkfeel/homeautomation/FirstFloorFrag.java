package sharkfeel.homeautomation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//1층

public class FirstFloorFrag extends Fragment {

    private ListView listLiving = null;
    private ListViewAdapter mAdapter = null;
    private Switch aSwitch = null;

    TextView textLivingHum, textLivingPres, textLivingTemp, textLivingLux;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bedroom_fragment, container, false);

        listLiving = (ListView)rootView.findViewById(R.id.listLiving);
        mAdapter = new ListViewAdapter(getContext());
        listLiving.setAdapter(mAdapter);


//        ((TextView)rootView.findViewById(R.id.textLivingHum)).setText("습도 : 30%");
//        ((TextView)rootView.findViewById(R.id.textLivingPres)).setText("기압 : 30");
//        ((TextView)rootView.findViewById(R.id.textLivingTemp)).setText("온도 : 30도");
//        ((TextView)rootView.findViewById(R.id.textLivingLux)).setText("밝기 : 3000");

        mAdapter.addItem(getResources().getDrawable(R.drawable.light_icon),
                "형광등",
                getContext().getResources().getString(R.string.server2_ipv6),
                "FIRST_FLOOR_LED",
                "Switch",
                "",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.temp_icon),
                "온도",
                getContext().getResources().getString(R.string.server2_ipv6),
                "FIRST_FLOOR_TEMP",
                "Text",
                "0℃",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.hum_icon),
                "습도",
                getContext().getResources().getString(R.string.server2_ipv6),
                "FIRST_FLOOR_HUMI",
                "Text",
                "0%",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.dust),
                "미세먼지",
                getContext().getResources().getString(R.string.server2_ipv6),
                "FIRST_FLOOR_DUST",
                "Text",
                "",
                false);


        listLiving.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListData mData = mAdapter.mListData.get(position);
                Toast.makeText(getContext(), mData.mTitle, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;

    }

}