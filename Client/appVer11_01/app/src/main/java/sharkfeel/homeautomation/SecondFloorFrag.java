package sharkfeel.homeautomation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

//2층

public class SecondFloorFrag extends Fragment {

    private ListView listBathroom = null;
    private ListViewAdapter mAdapter = null;

    private static final String KEY_MY_PREFERENCE = "sensor_option";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.livingroom_fragment, container, false);

//        ((TextView)rootView.findViewById(R.id.textBathroomHum)).setText("습도 : 30%");
//        ((TextView)rootView.findViewById(R.id.textBathroomPres)).setText("기압 : 30");
//        ((TextView)rootView.findViewById(R.id.textBathroomTemp)).setText("온도 : 30도");
//        ((TextView)rootView.findViewById(R.id.textBathroomLux)).setText("밝기 : 3000");

        listBathroom = (ListView)rootView.findViewById(R.id.BathroomList);
        mAdapter = new ListViewAdapter(getContext());
        listBathroom.setAdapter(mAdapter);

        mAdapter.addItem(getResources().getDrawable(R.drawable.light_icon),
                "내부 형광등",
                getContext().getResources().getString(R.string.server3_ipv6),
                "SECOND_FLOOR_LED",
                "Switch",
                "",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.light_icon),
                "피아노 형광등",
                getContext().getResources().getString(R.string.server3_ipv6),
                "SECOND_FLOOR_LED_back",
                "Switch",
                "",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.temp_icon),
                "온도",
                getContext().getResources().getString(R.string.server1_ipv6),
                "SECOND_FLOOR_TEMP",
                "Text",
                "0℃",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.hum_icon),
                "습도",
                getContext().getResources().getString(R.string.server1_ipv6),
                "SECOND_FLOOR_HUMI",
                "Text",
                "0%",
                false);


        listBathroom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListData mData = mAdapter.mListData.get(position);
                Toast.makeText(getContext(), mData.mTitle, Toast.LENGTH_SHORT).show();
            }
        });


        return rootView;
    }
}