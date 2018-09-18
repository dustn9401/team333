package sharkfeel.homeautomation;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.content.Context.MODE_PRIVATE;


public class ListViewAdapter extends BaseAdapter {

    public class TimeChecker {

        private long startTime;
        private long endTime;
        private long term;

        public void setStartTime(){
            startTime = System.currentTimeMillis();
        }

        public void setEndTime(){
            endTime = System.currentTimeMillis();
        }

        public double calcTerm(){
            term = endTime - startTime;
            return term;
        }
    }
    TimeChecker mTimeChecker = new TimeChecker();
    private TimerTask timerTask;
    private Timer mTimer;
    private Context mContext = null;

    public ArrayList<ListData> mListData = new ArrayList<ListData>();
    //서버 IP
    private static final String KEY_MY_PREFERENCE = "option";
    private static final String KEY_SERVER1_IP = "Server1IP";
    private static final String KEY_SERVER2_IP = "Server2IP";

    static {
        ScandiumLogger.initialize();
        ScandiumLogger.setLevel(Level.FINE);
    }
    private static final Logger LOG = Logger.getLogger(MainActivity.class.getName());
    private static final String TRUST_STORE_PASSWORD = "rootPass";
    private static final String KEY_STORE_PASSWORD = "endPass";
    private static final String KEY_STORE_LOCATION = "storage/emulated/0/certs/keyStore.p12";
    private static final String TRUST_STORE_LOCATION = "storage/emulated/0/certs/trustStore.p12";
    private DTLSConnector dtlsConnector;
    private boolean BedroomLED = false;
    private boolean FireSwitch = false;

    public void loadCert(){
        try {
            // load key store
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream in = new FileInputStream(KEY_STORE_LOCATION);
            keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());
            in.close();

            // load trust store
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            in = new FileInputStream(TRUST_STORE_LOCATION);
            trustStore.load(in, TRUST_STORE_PASSWORD.toCharArray());
            in.close();

            // You can load multiple certificates if needed
            Certificate[] trustedCertificates = new Certificate[1];
            trustedCertificates[0] = trustStore.getCertificate("root");

            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
            builder.setPskStore(new StaticPskStore("Client_identity", "secretPSK".getBytes()));
            builder.setIdentity((PrivateKey)keyStore.getKey("client", KEY_STORE_PASSWORD.toCharArray()),
                    keyStore.getCertificateChain("client"), true);
            builder.setTrustStore(trustedCertificates);
            dtlsConnector = new DTLSConnector(builder.build());

        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Could not load the keystore");
            e.printStackTrace();
        }
    }

    public ListViewAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final int pos = position;
        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_main, null);

            holder.mIcon = (ImageView) convertView.findViewById(R.id.imgView);
            holder.mSensorName = (TextView) convertView.findViewById(R.id.tvSensor);
            holder.mData = (TextView) convertView.findViewById(R.id.tv_Data);
            holder.mSwitch = (Switch) convertView.findViewById(R.id.switch_Btn);
            holder.mButton = (Button) convertView.findViewById(R.id.button_Btn); //화재 ON
            holder.mButton2 = (Button) convertView.findViewById(R.id.button_Btn2); //화재 OFF

            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final ListData mData = mListData.get(position);

        if (mData.mIcon != null) {
            holder.mIcon.setVisibility(View.VISIBLE);
            holder.mIcon.setImageDrawable(mData.mIcon);
        }else{
            holder.mIcon.setVisibility(View.GONE);
        }

        holder.mSensorName.setText(mData.mTitle);
        holder.mData.setText(mData.mData);

        if( mData.mCheck ) {
            holder.mSwitch.setChecked(true);
        } else {
            holder.mSwitch.setChecked(false);
        }

        if(mData.mClass.equals("Switch")) {
            holder.mSwitch.setVisibility(View.VISIBLE);
            holder.mData.setVisibility(View.GONE);
//            holder.mSwitch2.setVisibility(View.GONE);
            holder.mButton.setVisibility(View.GONE);
            holder.mButton2.setVisibility(View.GONE);
        } else if(mData.mClass.equals("Button")){
            holder.mSwitch.setVisibility(View.GONE);
            holder.mData.setVisibility(View.GONE);
//            holder.mSwitch2.setVisibility(View.VISIBLE);
            holder.mButton.setVisibility(View.VISIBLE);
            holder.mButton2.setVisibility(View.VISIBLE);
        } else if(mData.mClass.equals("Button2")){
            holder.mSwitch.setVisibility(View.GONE);
            holder.mData.setVisibility(View.GONE);
            holder.mButton.setVisibility(View.GONE);
            holder.mButton2.setVisibility(View.VISIBLE);
        } else {
            holder.mSwitch.setVisibility(View.GONE);
            holder.mData.setVisibility(View.VISIBLE);
//            holder.mSwitch2.setVisibility(View.GONE);
            holder.mButton.setVisibility(View.GONE);
            holder.mButton2.setVisibility(View.GONE);
        }

        holder.mData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "" + mListData.get(pos).mServerIP + mListData.get(pos).mSensorURL;
                mTimeChecker.setStartTime();
                new CoapGetTask(holder).execute(uri);
                mTimeChecker.setEndTime();
            }
        });

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        String uri = "" + mListData.get(pos).mServerIP + mListData.get(pos).mSensorURL;
                        new CoapGetTask(holder).execute(uri);
                    }
                };
                mTimer = new Timer();
                mTimer.schedule(timerTask,0,10000);
            }
        });

        holder.mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerTask.cancel();
            }
        });


        if(!mData.bOnCheckedChangeListener) {
            holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String uri = "" + mListData.get(pos).mServerIP + mListData.get(pos).mSensorURL;

                    if (isChecked == true) {
                        new CoapPostTask().execute(uri, "01");
                        mData.mCheck = true;
                    } else {
                        new CoapPostTask().execute(uri, "00");
                        mData.mCheck = false;
                    }
                }
            });
            mData.bOnCheckedChangeListener = true;
        }

        return convertView;
    }




    public String optionFileRead(String kindOption) {
        //파일읽어오기
        SharedPreferences prefs = mContext.getSharedPreferences(kindOption, MODE_PRIVATE);
        String value = prefs.getString(KEY_MY_PREFERENCE, "coaps://[fe80::6cd2:5d4e:6f3a:66fd]:5684");
        return value;
    }

    private class DownloadWebpageTask extends AsyncTask<String,Void,String> {

        private ViewHolder holder;

        public DownloadWebpageTask(ViewHolder viewholder) {
            holder  = viewholder;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return (String)downloadUrl((String)args[0]);
            } catch (IOException e) {
                return "download error!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            holder.mData.setText(result);
        }

        private String downloadUrl(String myurl) throws IOException{
            HttpURLConnection conn=null;
            URL url=new URL(myurl);
            conn=(HttpURLConnection)url.openConnection();

            BufferedInputStream buf=new BufferedInputStream(conn.getInputStream());
            BufferedReader bufreader=new BufferedReader(new InputStreamReader(buf,"utf-8"));

            String line=null;
            String page="";
            while((line=bufreader.readLine())!=null){
                Log.d("test",line);
                page+=line;
            }
            return page;
        }
    }


    class CoapGetTask extends AsyncTask<String, String, CoapResponse> {

        private ViewHolder holder;
        String posView;

        public CoapGetTask(ViewHolder viewholder) {
            holder  = viewholder;
        }

        protected void onPreExecute() {
            // reset text fields
        }

        protected CoapResponse doInBackground(String... args) {
            loadCert();
            final CoapClient client = new CoapClient(args[0]); // uri
            client.setEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));
            return client.get();

        }

        protected void onPostExecute(CoapResponse response) {
            if (response != null) {
                //시간 측정 종료시점
                String temp=holder.mSensorName.getText().toString();
                if(temp.equals("온도"))
                    holder.mData.setText(response.getResponseText() + "℃");
                else if(temp.equals("습도"))
                    holder.mData.setText(response.getResponseText() + "%");
                else if (temp.equals("화재알리미")){
                    if (response.getResponseText().contains("Call") == true)
                        Toast.makeText(mContext, response.getResponseText(),Toast.LENGTH_SHORT).show();
//                    else if (response.getResponseText().contains("Your") == true)
//                        Toast.makeText(mContext, response.getResponseText(),Toast.LENGTH_SHORT).show();
                }
                else if(temp.equals("미세먼지")){
                    holder.mData.setText(response.getResponseText() + "%");
                }
            } else {
                Toast.makeText(mContext, "Get Error!!!", Toast.LENGTH_SHORT).show();
            }
        }

    }


    class CoapPostTask extends AsyncTask<String, String, CoapResponse> {

        protected void onPreExecute() {
            // reset text fields
        }

        protected CoapResponse doInBackground(String... args) {
            loadCert();
            CoapClient client = new CoapClient(args[0]);
            client.setEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));

            if(BedroomLED == true)
                return client.put(args[1], MediaTypeRegistry.TEXT_PLAIN);
            else
                return client.put(args[1], MediaTypeRegistry.TEXT_PLAIN);

        }

        protected void onPostExecute(CoapResponse response) {
            if (response!=null) {
                //Toast.makeText(mContext, response.getResponseText(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "서버를 확인하세요.", Toast.LENGTH_SHORT).show();
                //Toast.makeText(mContext, response.getResponseText(), Toast.LENGTH_SHORT).show();
            }
        }


    }
    public void addItem(Drawable mIcon, String mSensorName, String mServerIP, String mSensorURL,
                        String mClass, String mData, Boolean mSwitchCheck){
        ListData addInfo = null;
        addInfo = new ListData();
        addInfo.mIcon = mIcon;
        addInfo.mTitle = mSensorName;
        addInfo.mServerIP = mServerIP;
        addInfo.mSensorURL = mSensorURL;
        addInfo.mClass = mClass;
        addInfo.mData = mData;
        addInfo.mCheck = mSwitchCheck;

        mListData.add(addInfo);
    }
}
