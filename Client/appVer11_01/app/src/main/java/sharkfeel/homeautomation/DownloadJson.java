package sharkfeel.homeautomation;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DownloadJson extends AsyncTask<String,String,String> {
    @Override
    protected String doInBackground(String... arg0) {
        try {
            return (String)getData((String) arg0[0]);
        } catch (Exception e) {
            return "Json download failed";
        }
    }

    protected void onPostExecute(String allresult) {
        try{
            ByteArrayInputStream bai = new ByteArrayInputStream(allresult.getBytes());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //dbf.setIgnoringElementContentWhitespace(true);//화이트스패이스 생략
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document parse = builder.parse(bai);//DOM 파서
            //태그 검색
            NodeList datas = parse.getElementsByTagName("data");
            //String result = "data태그 수 =" + datas.getLength()+"\n";
            String finalresult = "";
            //17개의 data태그를 순차로 접근
            for (int idx = 0; idx < datas.getLength(); idx++) {
                //필요한 정보들을 담을 변수 생성
                String day = "";
                String hour = "";
                String sky = "";
                String temp = "";
                Node node = datas.item(idx);//data 태그 추출
                int childLength = node.getChildNodes().getLength();
                //자식태그 목록 수정
                NodeList childNodes = node.getChildNodes();
                for (int childIdx = 0; childIdx < childLength; childIdx++) {
                    Node childNode = childNodes.item(childIdx);
                    int count = 0;
                    if(childNode.getNodeType() == Node.ELEMENT_NODE){
                        count ++;
                        //태그인 경우만 처리
                        //금일,내일,모레 구분(시간정보 포함)
                        if(childNode.getNodeName().equals("day")){
                            int su = Integer.parseInt(childNode.getFirstChild().getNodeValue());
                            switch(su){
                                case 0 : day = "오늘"; break;
                                case 1 : day = "내일"; break;
                                case 2 : day = "모레"; break;
                            }
                        }else if(childNode.getNodeName().equals("hour")){
                            hour = childNode.getFirstChild().getNodeValue();
                            //하늘상태코드 분석
                        }else if(childNode.getNodeName().equals("wfKor")){
                            sky = childNode.getFirstChild().getNodeValue();
                        }else if(childNode.getNodeName().equals("temp")){
                            temp = childNode.getFirstChild().getNodeValue();
                        }
                    }
                }//end 안쪽 for문
                finalresult += day+" "+hour+"시 ("+sky+","+temp+"도)\n";
            }//end 바깥쪽 for문
//            mTextView.setText(finalresult);
        } catch (Exception e) {
            e.printStackTrace();
        }
//            mTextView.setText(result);
    }

    private String getData(String strUrl) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedInputStream bis = null;
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responseCode;

            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

            responseCode = con.getResponseCode();

            if (responseCode == 200) {
                bis = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
                String line = null;

                while ((line = reader.readLine()) != null)
                    sb.append(line);

                bis.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}