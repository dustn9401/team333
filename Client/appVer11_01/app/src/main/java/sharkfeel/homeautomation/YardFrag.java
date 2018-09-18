package sharkfeel.homeautomation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//마당

public class YardFrag extends Fragment {

    private ListView listKitchen = null;
    private ListViewAdapter mAdapter = null;

    private static final String KEY_MY_PREFERENCE = "sensor_option";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.kitchen_fragment, container, false);

        listKitchen = (ListView)rootView.findViewById(R.id.KitchenList);
        mAdapter = new ListViewAdapter(getContext());
        listKitchen.setAdapter(mAdapter);

        mAdapter.addItem(getResources().getDrawable(R.drawable.light_icon),
                "정문 형광등",
                getContext().getResources().getString(R.string.server1_ipv6),
                "Yard_main_led",
                "Switch",
                "",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.light_icon),
                "창고 형광등",
                getContext().getResources().getString(R.string.server1_ipv6),
                "Yard_back_led",
                "Switch",
                "",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.temp_icon),
                "온도",
                getContext().getResources().getString(R.string.server1_ipv6),
                "Yard_TEMP",
                "Text",
                "0℃",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.hum_icon),
                "습도",
                getContext().getResources().getString(R.string.server1_ipv6),
                "Yard_HUMI",
                "Text",
                "0%",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.dust),
                "미세먼지농도",
                getContext().getResources().getString(R.string.server2_ipv6),
                "Yard_DUST",
                "Text",
                "mg/m3",
                false);

        mAdapter.addItem(getResources().getDrawable(R.drawable.siren_imege),
                "화재",
                //"coaps://[fe80::dd4b:3fe3:44fe:28ea]:5684/",
                getContext().getResources().getString(R.string.server2_ipv6),
                "Yard_FIRE",
                "Button",
                "",
                false);


        listKitchen.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListData mData = mAdapter.mListData.get(position);
                Toast.makeText(getContext(), mData.mTitle, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }
}