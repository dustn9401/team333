package sharkfeel.homeautomation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/*
    아래 코드에서 사용할 ArrayAdapter의 생성자는 3개의 파라미터를 가진다.

    ArrayAdapter(Context context, int resource, T[] objects)
    context : 안드로이드 시스템에서 제공되는 어플리케이션 전역 환경 정보에 대한 인터페이스. (Activity를 통해 사용 가능)
    resource : View로 매핑될 Resource Id. "android.R.layout.simple_list_item_1"은 TextView 위젯으로 구성된 ListView 아이템 리소스 Id.
    objects : 배열로 선언된 사용자 데이터.
*/

public class RegionDBListAdapter extends BaseAdapter {

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<RegionDBListItem> mArrayList = new ArrayList<>();

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public RegionDBListItem getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.region_db_list_item, parent, false);
        }

        // 'listview_custom'에 정의된 위젯에 대한 참조 획득
        //  ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_img);
        //  만약 이미지 추가할시.. 위에 코드추가

        TextView textViewStep1 = (TextView) convertView.findViewById(R.id.textViewStep1);
        TextView textViewStep2 = (TextView) convertView.findViewById(R.id.textViewStep2);
        TextView textViewStep3 = (TextView) convertView.findViewById(R.id.textViewStep3);
        TextView textViewCoordinateX = (TextView) convertView.findViewById(R.id.textViewCoordinateX);
        TextView textViewCoordinateY = (TextView) convertView.findViewById(R.id.textViewCoordinateY);

        /* 각 리스트에 뿌려줄 아이템을 받아오는데 mMyItem 재활용 */
        RegionDBListItem regionDBListItem = getItem(position);

        /* 각 위젯에 세팅된 아이템을 뿌려준다 */
//      iv_img.setImageDrawable(regionDBListItem.getIcon());
        textViewStep1.setText(regionDBListItem.getStep1());
        textViewStep2.setText(regionDBListItem.getStep2());
        textViewStep3.setText(regionDBListItem.getStep3());
        textViewCoordinateX.setText(regionDBListItem.getCoordinate_x());
        textViewCoordinateY.setText(regionDBListItem.getCoordinate_y());

        /* (위젯에 대한 이벤트리스너를 지정하고 싶다면 여기에 작성하면된다..)  */

        return convertView;
    }

    // 아이템 데이터 추가를 위한 함수. 리스트 아이템 바뀌면 이부분도 수정해줄것.
    public void addItem(String step1, String step2, String step3,
                        String coordinate_x, String coordinate_y) {

        RegionDBListItem regionDBListItem = new RegionDBListItem();

        //  MyItem에 아이템을 setting한다.
        //  regionDBListItem.setIcon(img); (이미지 추가시..)
        regionDBListItem.setStep1(step1);
        regionDBListItem.setStep2(step2);
        regionDBListItem.setStep3(step3);
        regionDBListItem.setCoordinate_x(coordinate_x);
        regionDBListItem.setCoordinate_y(coordinate_y);

        /* mItems에 MyItem을 추가한다. */
        mArrayList.add(regionDBListItem);
    }

    public void allRemove() {
        mArrayList.clear();
    }
}