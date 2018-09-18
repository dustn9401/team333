package sharkfeel.homeautomation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView actionbarImage;

    String strSever = "";

    //서버 IP
    private static final String KEY_MY_PREFERENCE = "option";   //  키 얻어오기
    private static final String KEY_SERVER1_IP = "Server1_IP";  //  키 입력

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("HN System");    //  앱 이름 설정
        setContentView(R.layout.activity_main);

        /*
          가장아래부분에 새로 뒤로가기, 홈버튼, 실행 리스트 나오는 네비게이션바
          클릭했을 때만 나오게 하기
        */

        //  navigation bar hide code!!

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actionbarImage = (ImageView)findViewById(R.id.actionbarImage);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("HN System");
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });


        strSever = "coaps://[fe80::6cd2:5d4e:6f3a:66fd]:5684/secure";

        //  메인탭에 넣을 이미지 선언
        //  선택하지 않았을 때 이미지
//        ImageView mainTab_UnselectedImage = new ImageView(this);
//        mainTab_UnselectedImage.setImageResource(R.drawable.ic_more_horiz_black_24dp);

        //  선택하였을 때 이미지
//        ImageView mainTab_SelectedImage = new ImageView(this);
//        mainTab_SelectedImage.setImageResource(R.drawable.ic_more_horiz_green_24dp);

        //  collapsingToolbar 글씨색깔설정
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("HN System");
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_more_horiz_green_24dp));
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("1층"));
        tabLayout.addTab(tabLayout.newTab().setText("2층"));
        tabLayout.addTab(tabLayout.newTab().setText("마당"));
//        tabLayout.addTab(tabLayout.newTab().setText("마당"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position=tab.getPosition();
                viewPager.setCurrentItem(position);
                switch (position) {
                    case 0:
                        actionbarImage.setImageResource(R.drawable.home2);
                        break;
                    case 1:
                        actionbarImage.setImageResource(R.drawable.bedroom1);
                        break;
                    case 2:
                        actionbarImage.setImageResource(R.drawable.livingroom2);
                        break;
                    case 3:
                        actionbarImage.setImageResource(R.drawable.kitchen1);
                        break;
//                    case 4:
//                        actionbarImage.setImageResource(R.drawable.yard1);
//                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//                int pos=tab.getPosition();
//                if(pos==0)
//                    tab.setIcon(R.drawable.ic_more_horiz_black_24dp);   //  0번째 탭 선택해제시 다시 검은색으로 강조 해제
//                viewPager.setCurrentItem(pos);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void ServerMenuIPSetting(){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInDialog = inflater.inflate(
                R.layout.dlg_server_ip_set, null);

        final android.app.AlertDialog dlgIPServer = new android.app.AlertDialog.Builder(
                this).setView(viewInDialog).create();
        dlgIPServer.setTitle("IP Setting [ IPv6 ]");
        final EditText editServer1 = (EditText)viewInDialog.findViewById(R.id.editServer1);

        editServer1.setText(optionFileRead(KEY_SERVER1_IP));

        Button btnOK = (Button)viewInDialog.findViewById(R.id.btnSetting);
        Button btnCancel = (Button)viewInDialog.findViewById(R.id.btnCancel);
        Button btnDefault = (Button)viewInDialog.findViewById(R.id.btnDefault);


        //확인버튼
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionFileWrite(KEY_SERVER1_IP, editServer1.getText().toString());
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

        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionFileWrite(KEY_SERVER1_IP, strSever);
                dlgIPServer.dismiss();
            }
        });

        dlgIPServer.show();
    }

    private void optionFileWrite(String kindOption, String strValue) {
        //파일저장
        SharedPreferences prefs = this.getSharedPreferences(kindOption, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_MY_PREFERENCE, strValue);
        editor.commit();
    }

    public String optionFileRead(String kindOption) {
        //파일읽어오기
        SharedPreferences prefs = this.getSharedPreferences(kindOption, MODE_PRIVATE);
        String value = prefs.getString(KEY_MY_PREFERENCE, "coaps://[fe80::6cd2:5d4e:6f3a:66fd]:5684/");
        return value;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_ip_setting:
                ServerMenuIPSetting();
                return true;

            case R.id.action_start:
                if (!item.isChecked()) {
                    startService(new Intent(this, ServerService.class));
                    item.setChecked(true);
                } else {
                    stopService(new Intent(this, ServerService.class));
                    item.setChecked(false);
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class CoapGetTask extends AsyncTask<String, String, CoapResponse> {

        protected void onPreExecute() {
            // reset text fields
        }

        protected CoapResponse doInBackground(String... args) {
            CoapClient client = new CoapClient(args[0]);
            return client.get();
        }

        protected void onPostExecute(CoapResponse response) {
            if (response!=null) {
                Toast.makeText(MainActivity.this, response.getResponseText(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, response.getResponseText(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
