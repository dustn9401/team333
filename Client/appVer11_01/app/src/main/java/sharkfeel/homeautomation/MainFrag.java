package sharkfeel.homeautomation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainFrag extends Fragment {

    private ListView listBedRoom = null;
    private ListViewAdapter mAdapter = null;

    //  미세먼지용 API KEY
    private String API_ACCESS_KEY = "EGxpfu5KGwRMDKxawVs%2F2c6zebu52ADlXM6k%2FroKmvLOsjBitGFrUB7eGXmYjzapHIida7HTe5TbrDRywVTzdw%3D%3D";

    private static final String KEY_MY_PREFERENCE = "sensor_option";
    private static final String KEY_Bedroom_Light_Name = "Bedroom_Light_Name";
    private static final String KEY_Bedroom_Light_IP = "Bedroom_Light_IP";
    private static final String KEY_Bedroom_Window = "Bedroom_Window";

    //  프로그래스 다이얼로그 관련 다이얼로그, 쓰레드
    private ProgressDialog mProgressDialog;
    private BackgroundThread mBackThread;

    //////////////////////////////////////////////////////////////////////////////////////
    //  날씨데이터관련
    RegionDBAdapter dbAdapter;          //  RegionDataBase 어댑터
    RegionDBListAdapter regionDBListAdapter;    //  RegionDBList 어댑터
    /*
    GPS를 통해 위도, 경도를 얻은 후 getAddress(위도, 경도)를 통해
    주소를 얻음, 그곳에서 얻어진 주소로 기상청에서 제공하는 지역별
    x, y좌표를 select 해서 해당 x,y 좌표로 날씨데이터를 얻음
     */

    public static int TO_GRID = 0;
    public static int TO_GPS = 1;

    private TextView txtpm10Value;
    private TextView txtpm25Value;
    private TextView txtpm10State;
    private TextView txtpm25State;

    private TextView txtMainAddress;
    private TextView txtCurrentTemp;
    private TextView txtCurrentState;
    private TextView txtHighTemp;
    private TextView txtLowTemp;
    private ImageView imgRefresh;
    private ImageView imgMainWeatherState;
    private ImageView imgWeatherState[];
    private TextView txtWeatherTime[];
    private TextView txtWeatherTemp[];

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    private static final String KEY_REGIONDB_ID = "regiondb_id";
    private static final String KEY_STEP1 = "step1";
    private static final String KEY_STEP2 = "step2";
    private static final String KEY_STEP3 = "step3";
    private static final String KEY_COORDINATE_X = "coordinate_x";
    private static final String KEY_COORDINATE_Y = "coordinate_y";
    private static final int INDEX_LATITUDE = 1;        //  DB이용시 latitude          (위도)인덱스
    private static final int INDEX_LONGITUDE = 2;       //  DB이용시 longitude         (경도)인덱스
    private static final int INDEX_COORDINATE_X = 3;    //  DB이용시 coordinate_x     (기상청x좌표)인덱스
    private static final int INDEX_COORDINATE_Y = 4;    //  DB이용시 coordinate_y     (기상청y좌표)인덱스
    private static final int INDEX_DOMICILE = 5;        //  DB이용시 domicile         (주소)인덱스

    private static final String TAG = "RegionDBAdapter";

    private Cursor mCursor;

    // GPSTracker class
    private GpsInfo gps;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.main_fragment, container, false);

        imgWeatherState = new ImageView[5];
        txtWeatherTime = new TextView[5];
        txtWeatherTemp = new TextView[5];

        //  미세먼지(pm10), 초미세먼지(pm25)
        txtpm10Value = (TextView)rootView.findViewById(R.id.txt_pm10Value);
        txtpm25Value = (TextView)rootView.findViewById(R.id.txt_pm25Value);
        txtpm10State = (TextView)rootView.findViewById(R.id.txt_pm10State);
        txtpm25State = (TextView)rootView.findViewById(R.id.txt_pm25State);

        ////////////////////////////////////////////////////////////////////////////////////
        //  날씨데이터
        //  위젯과 멤버변수 참조 획득
        txtMainAddress = (TextView)rootView.findViewById(R.id.txt_main_address);
        txtCurrentTemp = (TextView)rootView.findViewById(R.id.txt_current_temp);
        txtCurrentState = (TextView)rootView.findViewById(R.id.txt_current_state);
        txtHighTemp = (TextView)rootView.findViewById(R.id.high_temp);
        txtLowTemp = (TextView)rootView.findViewById(R.id.low_temp);
        imgRefresh = (ImageView)rootView.findViewById(R.id.img_refresh);
        imgMainWeatherState = (ImageView)rootView.findViewById(R.id.img_main_weather_state);
        imgWeatherState[0] = (ImageView)rootView.findViewById(R.id.img_weather_state_01);
        imgWeatherState[1] = (ImageView)rootView.findViewById(R.id.img_weather_state_02);
        imgWeatherState[2] = (ImageView)rootView.findViewById(R.id.img_weather_state_03);
        imgWeatherState[3] = (ImageView)rootView.findViewById(R.id.img_weather_state_04);
        imgWeatherState[4] = (ImageView)rootView.findViewById(R.id.img_weather_state_05);
        txtWeatherTime[0] = (TextView)rootView.findViewById(R.id.main_weather_time_01);
        txtWeatherTime[1] = (TextView)rootView.findViewById(R.id.main_weather_time_02);
        txtWeatherTime[2] = (TextView)rootView.findViewById(R.id.main_weather_time_03);
        txtWeatherTime[3] = (TextView)rootView.findViewById(R.id.main_weather_time_04);
        txtWeatherTime[4] = (TextView)rootView.findViewById(R.id.main_weather_time_05);
        txtWeatherTemp[0] = (TextView)rootView.findViewById(R.id.txt_weather_temp_01);
        txtWeatherTemp[1] = (TextView)rootView.findViewById(R.id.txt_weather_temp_02);
        txtWeatherTemp[2] = (TextView)rootView.findViewById(R.id.txt_weather_temp_03);
        txtWeatherTemp[3] = (TextView)rootView.findViewById(R.id.txt_weather_temp_04);
        txtWeatherTemp[4] = (TextView)rootView.findViewById(R.id.txt_weather_temp_05);

        // 위에가 실제 적용파트

        //  RegionDBAdapter 사용설정
        dbAdapter = new RegionDBAdapter(getContext());
        dbAdapter.open();
        mCursor = dbAdapter.fetchAllNotes();
        getActivity().startManagingCursor(mCursor);

        //  DBListAdapter 생성
        regionDBListAdapter = new RegionDBListAdapter();

        /*
        //  DBListAdapter 데이터입력
        for(int i=0; i<5000; i++) {
            regionDBListAdapter.addItem("test"+ i, "test"+ i, "test"+ i, "test"+ i, "test"+ i);
        }

        //  리스트뷰에 DBListAdapter 등록 (위에서 추가한 아이템이 들어간 어댑터 등록해줌)
        mListView.setAdapter(regionDBListAdapter);
        위에는 테스트용 데이터 입력, 등록
        */

        //  현재 위치를 얻고,
        //  DB에 위도, 경도, 기상청x좌표, 기상청y좌표, 주소를 저장함
        //  아래함수에서 권한요청하는 callPermission()도 같이 실행됨
        callPermission();  // 권한 요청을 해야 함
        if(isPermission) {
            //  권한이 얻어졌을 경우에만 날씨, 미세먼지 데이터 앱실행시마다 업데이트
            getLocationAndSaveData();
            mProgressDialog = ProgressDialog.show(getActivity(),
                    "데이터를 얻는 중입니다.", "잠시만 기다려주세요...");
            mBackThread = new BackgroundThread();
            mBackThread.setRunning(true);
            mBackThread.start();
            getLocationAndSaveData();
        }

        // 가져온 파싱되지 않은 모든 데이터는 String형으로
        // 반환시킨 후 domParsing() 에서 해당 데이터를 이용해 파싱함
        // 현재 domParsing()은 getLocationAndSaveData()에 들어가 있음.
        // domParsing();

        //  GPS에서 좌표 읽어오는 코드
        //  lat; // 위도  latitude
        //  lon; // 경도  longitude

        // GPS 정보를 보여주기 위한 이벤트 클래스 등록

        //  날씨 오른쪽상단에 새로고침 버튼 누를시 발동
        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog = ProgressDialog.show(getActivity(),
                        "데이터를 얻는 중입니다.", "잠시만 기다려주세요...");
                mBackThread = new BackgroundThread();
                mBackThread.setRunning(true);
                mBackThread.start();
                getLocationAndSaveData();
            }
        });

        //  날씨끝


        listBedRoom = (ListView)rootView.findViewById(R.id.bedroomList);
        mAdapter = new ListViewAdapter(getContext());
        listBedRoom.setAdapter(mAdapter);


        listBedRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment frag = new FirstFloorFrag();
                FragmentManager fmanager = getFragmentManager();
                FragmentTransaction ftrans = fmanager.beginTransaction();
                ftrans.attach(frag);
                ftrans.commit();
            }

        });
        return rootView;
    }

/*
    public void ServerMenuIPSetting(int pos){
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInDialog = inflater.inflate(
                R.layout.dlg_sensor_ip_set, null);

        final android.app.AlertDialog dlgIPServer = new android.app.AlertDialog.Builder(
                getActivity()).setView(viewInDialog).create();
        dlgIPServer.setTitle("센서 IP 설정");

        final EditText editSensorName = (EditText)viewInDialog.findViewById(R.id.editSensorName);
        final EditText editSensorServerIP = (EditText)viewInDialog.findViewById(R.id.editSensorServerIP);

        editSensorName.setText(mAdapter.mListData.get(pos).mTitle);
        editSensorServerIP.setText(mAdapter.mListData.get(pos).mServerIP);

        Button btnOK = (Button)viewInDialog.findViewById(R.id.btnSensorSetting);
        Button btnCancel = (Button)viewInDialog.findViewById(R.id.btnSensorCancel);

        //확인버튼
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //optionFileWrite(KEY_Bedroom_Light_Name, editSensorName.getText().toString());
                //optionFileWrite(KEY_Bedroom_Window, editSensorServerIP.getText().toString());
                dlgIPServer.dismiss();
            }
        });
        //취소버튼
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgIPServer.dismiss();
            }
        });

        dlgIPServer.show();
    }

    private void optionFileWrite(String kindOption, String strValue) {
        //파일저장
        SharedPreferences prefs = getActivity().getSharedPreferences(kindOption, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_MY_PREFERENCE, strValue);
        editor.commit();
    }

    public String optionFileRead(String kindOption) {
        //파일읽어오기
        SharedPreferences prefs = getActivity().getSharedPreferences(kindOption, MODE_PRIVATE);
        String value = prefs.getString(KEY_MY_PREFERENCE, "coap://[2005::ba27:ebff:fe48:52b4]");
        return value;
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

            getLocationAndSaveData();//앱설치후 맨처음에 부르면 에러생겨서 처음에 추가로 1번 더 불러줌

            mProgressDialog = ProgressDialog.show(getActivity(),
                    "데이터를 얻는 중입니다.", "잠시만 기다려주세요...");
            mBackThread = new BackgroundThread();
            mBackThread.setRunning(true);
            mBackThread.start();

            getLocationAndSaveData();

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
        else {
            isAccessCoarseLocation = true;
            getActivity().moveTaskToBack(true);
            getActivity().finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    /**
     * 위도,경도로 주소구하기
     * @param lat
     * @param lng
     * @return 주소
     */

    public static String getAddress(Context mContext, double lat, double lng) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        return nowAddress;
    }

    //  프로그램 종료시 수행
    @Override
    public void onStop() {
        super.onStop();
    }

    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    //  기상청x, y좌표 얻는 수학식
    private LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y )
    {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //


        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();

        if (mode == TO_GRID) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = alat * RADDEG;
            rs.lng = alon * RADDEG;
        }
        return rs;
    }

    class LatXLngY
    {
        public double lat;
        public double lng;

        public double x;
        public double y;

    }

    private void getLocationAndSaveData() {

        gps = new GpsInfo(getContext());
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            //  gps가 좌표를 읽어오는데 성공했을 경우 위도를 얻어옴
            double latitude = gps.getLatitude();

            //  gps가 좌표를 읽어오는데 성공했을 경우 경도를 얻어옴
            double longitude = gps.getLongitude();

            //  기상청 X, Y 좌표 얻기
            LatXLngY tmp = convertGRID_GPS(TO_GRID, latitude, longitude);

            //  tm X, Y좌표 얻기
            GeoPoint in_pt=new GeoPoint(longitude, latitude);   //  경도, 위도넣기
            GeoPoint tm_pt=GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);//tmX,Y좌표얻기
            String weatherX=Integer.toString((int)(tmp.x));
            String weatherY=Integer.toString((int)(tmp.y));
            String EditweatherX="기상청 x좌표 : " + weatherX;
            String EditweatherY="기상청 y좌표 : " + weatherY;

            String str_address = getAddress(getContext(), latitude, longitude);

            String split_address[] = str_address.split(" ");

            txtMainAddress.setText(split_address[2] + " " + split_address[3]);
            //  얻은 주소데이터 문자열자르기 + main 주소에 등록

            //  DB에 데이터 추가하기 전에 현재 DB사용목적은 임시로 저장했다가
            //  다시 삭제하는 용도이므로 이미 있는 데이터를 먼저 삭제해줌.
            mCursor = dbAdapter.fetchAllNotes();    //  현재 데이터 목록에서의 커서 얻어옴
            for(int count=mCursor.getCount(); count>=0; count--) {
                //  dbAdapter에 등록되있는 레코드를 맨끝에서부터 전부다 지워나감
                dbAdapter.deleteNote();
            }

            domParsing(weatherX, weatherY);
            String addr=domParsing(tm_pt);
            domParsing(addr);

            /*
              dbAdapter에 등록된 모든 레코드를 가져와서 커서에 등록시킴
              레코드가 3줄일시
              mCursor = dbAdapter.fetchAllNotes();
              int count=mCursor.getCount();
              아래코드 실행후 count는 3이됨.

              dbAdapter.createNote(Double.toString(latitude), Double.toString(longitude), weatherX, weatherY, str_address);
              dbAdapter.createNote(Double.toString(latitude), Double.toString(longitude), "테스트X", "테스트Y", str_address);
              mCursor = dbAdapter.fetchAllNotes();
              int count=mCursor.getCount();
              mCursor.moveToPosition(1);
              String t1=mCursor.getString(3);   t1 : 테스트X
              String t2=mCursor.getString(4);   t2 : 테스트Y
            */


            /*
             ★★★★★ 데이터베이스 이용시 항상 커서를 꼭 이동시킨 후에 getText()할 것.
             그렇지 않으면 무조건 터짐. 데이터 얻을때마다 항상 moveTo~함수를 이용해서
             얻고자하는 위치를 설정할것. 커서의 위치는 배열처럼 0부터 시작함.
             아래에서도 테스트 해봤지만 데이터(레코드)(행)이 1줄만 들어가 있을 때
             mCursor.moveToPosition(1); 을 하면 레코드 범위를 벗어난 곳으로 커서가
             이동하게 되고, 데이터가 있지도않은 null값에서 getString();을 하므로
             바로 프로그램이 터지게 되있음.
             커서의 범위는 0부터시작, getString에서의 인덱스범위는 1부터 시작함.
             getString에서 인덱스 0은 뭔지 몰겠음..
             T  a   k   e
             B  r   i   n   g
             이래있을경우
             mCursor.moveToPosition(1);
             mCursor.getString(2) 의 값은 'r' 가 뽑히게 됨.

            */

            /*
            디버깅시 테스트용도
            String name = txtLat.getText().toString();
            String tel = txtLon.getText().toString();
            String temp=mCursor.getString(0);
            String temp1=mCursor.getString(1);
            String temp2=mCursor.getString(2);
            String temp3=mCursor.getString(3);
            String temp4=mCursor.getString(4);
            String temp5=mCursor.getString(5);

            mCursor.moveToPosition(count-1);
            count=mCursor.getCount();

            mCursor.moveToFirst();
            count=mCursor.getCount();

            mCursor.moveToLast();
            count=mCursor.getCount();

            */

            //  db.execSQL("INSERT INTO contacts VALUES (null, '" + name + "', '" + tel + "');");
            //  SQL 문으로 할시 위에처럼..

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }

    //  가져온 파싱되지 않은 모든 데이터는 String형으로 반환시킨 후
    //  domParsing() 에서 해당 데이터를 이용해 파싱함
    private void domParsing(String weatherX, String weatherY) {
        try {
            String html = getXMLData(weatherX, weatherY);
            //DOM 파싱.
            ByteArrayInputStream bai = new ByteArrayInputStream(html.getBytes());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //dbf.setIgnoringElementContentWhitespace(true);//화이트스패이스 생략
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document parse = builder.parse(bai);//DOM 파서
            //태그 검색
            NodeList datas = parse.getElementsByTagName("data");
            //String result = "data태그 수 =" + datas.getLength()+"\n";
            String result = "";
            //17개의 data태그를 순차로 접근

            //필요한 정보들을 담을 변수 생성
            String day = "";
            String hour = "";
            String sky = "";
            String ws = ""; // 풍속
            String reh = "";// 습도
            Integer temp_now = 0;
            Integer temp_low = 999;
            Integer temp_high = 0;
            Integer temp = 0;

            long now_miles=System.currentTimeMillis();
            Date date = new Date(now_miles);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
            int timeNow=Integer.valueOf(simpleDateFormat.format(date));
            //  Toast.makeText(getApplicationContext(), Integer.toString(timeNow), Toast.LENGTH_SHORT).show();
            //  ex) 10월 12일 => strDate == 12 (아래 계산을 위해 int형으로 변환)

            for (int idx = 0; idx < datas.getLength(); idx++) {

                Node node = datas.item(idx);//data 태그 추출

                int childLength = node.getChildNodes().getLength();
                //자식태그 목록 수정
                NodeList childNodes = node.getChildNodes();

//                regionDBListAdapter.allRemove();    //  새로 갱신이므로 먼저 전부다 지워줌

                for (int childIdx = 0; childIdx < childLength; childIdx++) {
                    Node childNode = childNodes.item(childIdx);
                    int count = 0;
                    if(childNode.getNodeType() == Node.ELEMENT_NODE){
                        count ++;
                        //태그인 경우만 처리
                        //금일,내일,모레 구분(시간정보 포함)
                        if(childNode.getNodeName().equals("day")){
                            int su = Integer.parseInt(childNode.getFirstChild().getNodeValue());
                            if(timeNow==23 || timeNow<2) {  //  예보시간이 23시~02시 일경우
                                switch(su){
                                    case 1 : day = "오늘"; break;
                                    case 2 : day = "내일"; break;
                                }
                            } else {
                                switch (su) {
                                    case 0: day = "오늘"; break;
                                    case 1: day = "내일"; break;
                                    case 2: day = "모레"; break;
                                }
                            }
                        }else if(childNode.getNodeName().equals("hour")) {
                            hour = childNode.getFirstChild().getNodeValue();
                            if (hour.length() == 1)
                                hour = "0" + hour;
                        //하늘상태코드 분석
                        }else if(childNode.getNodeName().equals("wfKor")){
                            sky = childNode.getFirstChild().getNodeValue();
                        }else if(childNode.getNodeName().equals("tmn")){
                            temp = (int)Double.parseDouble(childNode.getFirstChild().getNodeValue());
                            if(temp != -999 && temp < temp_low)
                                temp_low=temp;
                        }else if(childNode.getNodeName().equals("tmx")){
                            temp = (int)Double.parseDouble(childNode.getFirstChild().getNodeValue());
                            if(temp != -999 && temp > temp_high)
                                temp_high=temp;
                        }else if(childNode.getNodeName().equals("temp")){
                            temp_now = (int)Double.parseDouble(childNode.getFirstChild().getNodeValue());
                        }else if(childNode.getNodeName().equals("ws")){
                            ws = childNode.getFirstChild().getNodeValue();
                        }else if(childNode.getNodeName().equals("reh")){
                            reh = childNode.getFirstChild().getNodeValue();
                        }
                    }
                }//end 안쪽 for문

                //  DB cursor(커서)는 0부터시작, getString은 1부터시작
                //  DB에 시간, 현재온도, 날씨상태, 풍속, 습도를 추가함
                dbAdapter.createNote(hour, temp_now  + "℃", sky, ws, reh);
                regionDBListAdapter.addItem(hour, temp_now + "℃", sky,
                        "풍속 : " + ws, "습도 : " + reh);
            }//end 바깥쪽 for문

            txtHighTemp.setText(Integer.toString(temp_high) + "℃");
            txtLowTemp.setText(Integer.toString(temp_low) + "℃");

            mCursor.moveToFirst();
            txtCurrentTemp.setText(mCursor.getString(2));   //  첫 메인온도
            txtCurrentState.setText(mCursor.getString(3));  //  첫 메인날씨상태(텍스트)

            Integer reportTime=(int)Double.parseDouble(mCursor.getString(1));
            //  첫 메인weahterState 설정(그림)
            if(reportTime>=18 || reportTime<=2) {
                if(mCursor.getString(3).equals("맑음"))
                    imgMainWeatherState.setImageResource(R.drawable.afternoon_moon);
                else if(mCursor.getString(3).equals("구름 조금"))
                    imgMainWeatherState.setImageResource(R.drawable.afternoon_little_cloud);
                else if(mCursor.getString(3).equals("구름 많음"))
                    imgMainWeatherState.setImageResource(R.drawable.afternoon_much_cloud);
                else if(mCursor.getString(3).equals("흐림"))
                    imgMainWeatherState.setImageResource(R.drawable.much_cloud);
                else if(mCursor.getString(3).equals("비"))
                    imgMainWeatherState.setImageResource(R.drawable.rain);
                else if(mCursor.getString(3).equals("눈"))
                    imgMainWeatherState.setImageResource(R.drawable.snow);
                else if(mCursor.getString(3).equals("비 또는 눈"))
                    imgMainWeatherState.setImageResource(R.drawable.rain_snow);
                else if(mCursor.getString(3).equals("눈 또는 비"))
                    imgMainWeatherState.setImageResource(R.drawable.snow_rain);
                else
                    imgMainWeatherState.setImageResource(R.drawable.error);
            } else {
                if(mCursor.getString(3).equals("맑음"))
                    imgMainWeatherState.setImageResource(R.drawable.morning_sun);
                else if(mCursor.getString(3).equals("구름 조금"))
                    imgMainWeatherState.setImageResource(R.drawable.morning_little_cloud);
                else if(mCursor.getString(3).equals("구름 많음"))
                    imgMainWeatherState.setImageResource(R.drawable.morning_much_cloud);
                else if(mCursor.getString(3).equals("흐림"))
                    imgMainWeatherState.setImageResource(R.drawable.much_cloud);
                else if(mCursor.getString(3).equals("비"))
                    imgMainWeatherState.setImageResource(R.drawable.rain);
                else if(mCursor.getString(3).equals("눈"))
                    imgMainWeatherState.setImageResource(R.drawable.snow);
                else if(mCursor.getString(3).equals("비 또는 눈"))
                    imgMainWeatherState.setImageResource(R.drawable.rain_snow);
                else if(mCursor.getString(3).equals("눈 또는 비"))
                    imgMainWeatherState.setImageResource(R.drawable.snow_rain);
                else
                    imgMainWeatherState.setImageResource(R.drawable.error);
            }

            //  3시간마다 시간대별로 5단계로 나누어서 시간, 날씨상태, 온도 표현
            for(int i=0; i<5; i++) {
                reportTime=(int)Double.parseDouble(mCursor.getString(1));
                txtWeatherTime[i].setText(mCursor.getString(1) + "시");
                txtWeatherTemp[i].setText(mCursor.getString(2));
                if(reportTime>=18 || reportTime<=2) {
                    if(mCursor.getString(3).equals("맑음"))
                        imgWeatherState[i].setImageResource(R.drawable.afternoon_moon);
                    else if(mCursor.getString(3).equals("구름 조금"))
                        imgWeatherState[i].setImageResource(R.drawable.afternoon_little_cloud);
                    else if(mCursor.getString(3).equals("구름 많음"))
                        imgWeatherState[i].setImageResource(R.drawable.afternoon_much_cloud);
                    else if(mCursor.getString(3).equals("흐림"))
                        imgWeatherState[i].setImageResource(R.drawable.much_cloud);
                    else if(mCursor.getString(3).equals("비"))
                        imgWeatherState[i].setImageResource(R.drawable.rain);
                    else if(mCursor.getString(3).equals("눈"))
                        imgWeatherState[i].setImageResource(R.drawable.snow);
                    else if(mCursor.getString(3).equals("비 또는 눈"))
                        imgWeatherState[i].setImageResource(R.drawable.rain_snow);
                    else if(mCursor.getString(3).equals("눈 또는 비"))
                        imgWeatherState[i].setImageResource(R.drawable.snow_rain);
                    else
                        imgWeatherState[i].setImageResource(R.drawable.error);
                } else {
                    if(mCursor.getString(3).equals("맑음"))
                        imgWeatherState[i].setImageResource(R.drawable.morning_sun);
                    else if(mCursor.getString(3).equals("구름 조금"))
                        imgWeatherState[i].setImageResource(R.drawable.morning_little_cloud);
                    else if(mCursor.getString(3).equals("구름 많음"))
                        imgWeatherState[i].setImageResource(R.drawable.morning_much_cloud);
                    else if(mCursor.getString(3).equals("흐림"))
                        imgWeatherState[i].setImageResource(R.drawable.much_cloud);
                    else if(mCursor.getString(3).equals("비"))
                        imgWeatherState[i].setImageResource(R.drawable.rain);
                    else if(mCursor.getString(3).equals("눈"))
                        imgWeatherState[i].setImageResource(R.drawable.snow);
                    else if(mCursor.getString(3).equals("비 또는 눈"))
                        imgWeatherState[i].setImageResource(R.drawable.rain_snow);
                    else if(mCursor.getString(3).equals("눈 또는 비"))
                        imgWeatherState[i].setImageResource(R.drawable.snow_rain);
                    else
                        imgWeatherState[i].setImageResource(R.drawable.error);
                }
                mCursor.moveToNext();
            }

        } catch (Exception e) {
//            Toast.makeText(getContext(), "Error occur", Toast.LENGTH_SHORT).show();
            // 어플 설치후 가장1번째로 불러오는 데이터는 에러가 나는데 왜그런지 원인파악이
            // 아직 되지않았음.. 해결될때까지 이부분 주석처리
            e.printStackTrace();
        }
    }

    private String domParsing(GeoPoint input_pm_pt) {
        try {
            String html = getXMLData(input_pm_pt);
            //DOM 파싱.
            ByteArrayInputStream bai = new ByteArrayInputStream(html.getBytes());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //dbf.setIgnoringElementContentWhitespace(true);//화이트스패이스 생략
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document parse = builder.parse(bai);//DOM 파서
            //태그 검색
            NodeList datas = parse.getElementsByTagName("item");
            //String result = "data태그 수 =" + datas.getLength()+"\n";
            String result = "";
            //17개의 data태그를 순차로 접근

            //필요한 정보들을 담을 변수 생성
            String stationName = "";
            String addr = "";
            String tm = "";

            for (int idx = 0; idx < datas.getLength(); idx++) {

                Node node = datas.item(idx);//data 태그 추출

                int childLength = node.getChildNodes().getLength();
                //자식태그 목록 수정
                NodeList childNodes = node.getChildNodes();

//                regionDBListAdapter.allRemove();    //  새로 갱신이므로 먼저 전부다 지워줌

                for (int childIdx = 0; childIdx < childLength; childIdx++) {
                    Node childNode = childNodes.item(childIdx);
                    int count = 0;
                    if(childNode.getNodeType() == Node.ELEMENT_NODE){
                        count ++;
                        //태그인 경우만 처리
                        //금일,내일,모레 구분(시간정보 포함)
                        if(childNode.getNodeName().equals("stationName")){
                            stationName = childNode.getFirstChild().getNodeValue();
                            return stationName;
                        }else if(childNode.getNodeName().equals("addr")) {
//                            addr = childNode.getFirstChild().getNodeValue();
//                            txtgetXML.append(addr + " ");
                        }else if(childNode.getNodeName().equals("tm")){
//                            tm = childNode.getFirstChild().getNodeValue();
//                            txtgetXML.append(tm + "\n");
                        }
                    }
                }//end 안쪽 for문

                //  DB cursor(커서)는 0부터시작, getString은 1부터시작
                //  DB에 시간, 현재온도, 날씨상태, 풍속, 습도를 추가함
//                dbAdapter.createNote(hour, temp_now  + "℃", sky, ws, reh);
//                regionDBListAdapter.addItem(hour, temp_now + "℃", sky,
//                        "풍속 : " + ws, "습도 : " + reh);
            }//end 바깥쪽 for문

        } catch (Exception e) {
//            Toast.makeText(getContext(), "Error occur", Toast.LENGTH_SHORT).show();
            // 어플 설치후 가장1번째로 불러오는 데이터는 에러가 나는데 왜그런지 원인파악이
            // 아직 되지않았음.. 해결될때까지 이부분 주석처리
            e.printStackTrace();
        }
        return "정왕동";
    }

    private void domParsing(String addr) {
        try {
            String html = getXMLData(addr);
            //DOM 파싱.
            ByteArrayInputStream bai = new ByteArrayInputStream(html.getBytes());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //dbf.setIgnoringElementContentWhitespace(true);//화이트스패이스 생략
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document parse = builder.parse(bai);//DOM 파서
            //태그 검색
            NodeList datas = parse.getElementsByTagName("item");
            //String result = "data태그 수 =" + datas.getLength()+"\n";
            String result = "";
            //17개의 data태그를 순차로 접근

            //필요한 정보들을 담을 변수 생성
            String[] pm10Value = new String[10];    //  미세먼지
            String[] pm25Value = new String[10];    //  초미세먼지

            for (int idx = 0; idx < datas.getLength(); idx++) {

                Node node = datas.item(idx);//data 태그 추출

                int childLength = node.getChildNodes().getLength();
                //자식태그 목록 수정
                NodeList childNodes = node.getChildNodes();

//                regionDBListAdapter.allRemove();    //  새로 갱신이므로 먼저 전부다 지워줌

                for (int childIdx = 0; childIdx < childLength; childIdx++) {
                    Node childNode = childNodes.item(childIdx);
                    int count = 0;
                    if(childNode.getNodeType() == Node.ELEMENT_NODE){
                        count ++;
                        //태그인 경우만 처리
                        //금일,내일,모레 구분(시간정보 포함)
                        if(childNode.getNodeName().equals("pm10Value")){
                            pm10Value[idx] = childNode.getFirstChild().getNodeValue();
                        }else if(childNode.getNodeName().equals("pm25Value")) {
                            pm25Value[idx] = childNode.getFirstChild().getNodeValue();
                        }else if(childNode.getNodeName().equals("tm")){
//                            tm = childNode.getFirstChild().getNodeValue();
//                            txtgetXML.append(tm + "\n");
                        }
                    }
                }//end 안쪽 for문

                //  DB cursor(커서)는 0부터시작, getString은 1부터시작
                //  DB에 시간, 현재온도, 날씨상태, 풍속, 습도를 추가함
//                dbAdapter.createNote(hour, temp_now  + "℃", sky, ws, reh);
//                regionDBListAdapter.addItem(hour, temp_now + "℃", sky,
//                        "풍속 : " + ws, "습도 : " + reh);
            }//end 바깥쪽 for문

            //  미세먼지
            for(int i=0; i<10; i++) {
                if(!pm10Value[i].equals("-")) {
                    int pm10 = Integer.parseInt(pm10Value[i]);
                    if(pm10>=0 && pm10<=30) {
                        txtpm10Value.setText(pm10Value[i] + "㎍/㎥");
                        txtpm10State.setText("좋음");
                        txtpm10State.setTextColor(Color.rgb(0, 216, 255));
                        txtpm10State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                    } else if(pm10>=31 && pm10<=80) {
                        txtpm10Value.setText(pm10Value[i] + "㎍/㎥");
                        txtpm10State.setText("보통");
                        txtpm10State.setTextColor(Color.rgb(29, 219, 22));
                        txtpm10State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                    } else if(pm10>=81 && pm10<=150) {
                        txtpm10Value.setText(pm10Value[i] + "㎍/㎥");
                        txtpm10State.setText("나쁨");
                        txtpm10State.setTextColor(Color.rgb(255, 228, 0));
                        txtpm10State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                    } else if(pm10>=151) {
                        txtpm10Value.setText(pm10Value[i] + "㎍/㎥");
                        txtpm10State.setText("매우나쁨");
                        txtpm10State.setTextColor(Color.rgb(255, 0, 0));
                        txtpm10State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                    }
                    break;
                }
            }

            //  초미세먼지
            for(int i=0; i<10; i++) {
                if(!pm25Value[i].equals("-")) {
                    int pm25 = Integer.parseInt(pm25Value[i]);
                    if(pm25>=0 && pm25<=15) {
                        txtpm25Value.setText(pm25Value[i] + "㎍/㎥");
                        txtpm25State.setText("좋음");
                        txtpm25State.setTextColor(Color.rgb(0, 216, 255));
                        txtpm25State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                    } else if(pm25>=16 && pm25<=35) {
                        txtpm25Value.setText(pm25Value[i] + "㎍/㎥");
                        txtpm25State.setText("보통");
                        txtpm25State.setTextColor(Color.rgb(29, 219, 22));
                        txtpm25State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                    } else if(pm25>=36 && pm25<=75) {
                        txtpm25Value.setText(pm25Value[i] + "㎍/㎥");
                        txtpm25State.setText("나쁨");
                        txtpm25State.setTextColor(Color.rgb(255, 228, 0));
                        txtpm25State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                    } else if(pm25>=76) {
                        txtpm25Value.setText(pm25Value[i] + "㎍/㎥");
                        txtpm25State.setText("매우나쁨");
                        txtpm25State.setTextColor(Color.rgb(255, 0, 0));
                        txtpm25State.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                    }
                    break;
                }
            }


        } catch (Exception e) {
//            Toast.makeText(getContext(), "Error occur", Toast.LENGTH_SHORT).show();
            // 어플 설치후 가장1번째로 불러오는 데이터는 에러가 나는데 왜그런지 원인파악이
            // 아직 되지않았음.. 해결될때까지 이부분 주석처리
            e.printStackTrace();
        }
    }

    public String getXMLData(String weatherX, String weatherY) {
        String xmlData="";
        // set the server URL
        String url = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + weatherX + "&gridy=" + weatherY;
        //http://www.kma.go.kr/wid/queryDFS.jsp?gridx=56&gridy=122(정왕기준)

        // call data from web URL
        try {
            ConnectivityManager conManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conManager.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected()) {
                xmlData=new DownloadJson().execute(url).get();
            } else {
                Toast toast = Toast.makeText(getContext(), "Network isn't connected", Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlData;
    }

    public String getXMLData(GeoPoint input_tm_pt) {
        String tmX = Double.toString(input_tm_pt.getX());
        String tmY = Double.toString(input_tm_pt.getY());
        String xmlData="";
        // set the server URL
        String url = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?tmX=" + tmX + "&tmY=" + tmY + "&pageNo=1&numOfRows=10&ServiceKey=" + API_ACCESS_KEY;

        // call data from web URL
        try {
            ConnectivityManager conManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conManager.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected()) {
                xmlData=new DownloadJson().execute(url).get();
            } else {
                Toast toast = Toast.makeText(getContext(), "Network isn't connected", Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlData;
    }

    public String getXMLData(String addr) {
        String xmlData="";
        // set the server URL
        String url = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=" + addr + "&dataTerm=month&pageNo=1&numOfRows=10&ServiceKey=" + API_ACCESS_KEY + "&ver=1.3";
        // call data from web URL
        try {
            ConnectivityManager conManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conManager.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected()) {
                xmlData=new DownloadJson().execute(url).get();
            } else {
                Toast toast = Toast.makeText(getContext(), "Network isn't connected", Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlData;
    }

    //  기상청 날씨정보 추출, 아직 파싱되지않은 기상청의
    //  모든데이터를 전부 String에 일단 집어넣은 후 반환해줌

    //  프로그래스 다이얼로그용 쓰레드
    public class BackgroundThread extends Thread {
        volatile boolean running = false;
        int cnt;

        void setRunning(boolean b) {
            running = b;
            cnt = 7;
        }

        @Override
        public void run() {

            while (running) {
                try {
                    sleep(100);
                    if (cnt-- == 0) {
                        running = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.sendMessage(handler.obtainMessage());
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            mProgressDialog.dismiss();

            boolean retry = true;
            while (retry) {
                try {
                    mBackThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

//            Toast.makeText(getContext(),
//                    "완료되었습니다!", Toast.LENGTH_LONG).show();
        }
    };
}