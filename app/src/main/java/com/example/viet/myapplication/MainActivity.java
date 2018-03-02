package com.example.viet.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends Activity {
    Button btnCallBPM;
    private static String URL = "https://10.140.0.4:9443/teamworks/webservices/INHMC/IHMWebservice.tws";
    ArrayList<TaskDetail> taskDetails = new ArrayList<TaskDetail>();
    private ListView mListView;
    private String username;

    private static final HostnameVerifier DUMMY_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = getIntent().getStringExtra("USER_NAME");
        Log.i("username", username);
        btnCallBPM = (Button) findViewById(R.id.btnCallBPM);
        mListView = (ListView) findViewById(R.id.task_list_view);

        taskDetails.clear();
        OpenConnectionTask openConnection = new OpenConnectionTask(getApplicationContext());
        openConnection.execute();
        btnCallBPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("test", "onClick: ");
                taskDetails.clear();
                OpenConnectionTask openConnection = new OpenConnectionTask(getApplicationContext());
                openConnection.execute();

            }
        });


    }

    private class OpenConnectionTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;

        public OpenConnectionTask (Context context){
            mContext = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*// Dummy trust manager that trusts all certificates
            TrustManager localTrustmanager = new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            };

            // Create SSLContext and set the socket factory as default
            try {
                SSLContext sslc = SSLContext.getInstance("TLS");
                sslc.init(null, new TrustManager[] { localTrustmanager },
                        new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslc
                        .getSocketFactory());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                URL url = new URL(URL);

                HttpsURLConnection connection = (HttpsURLConnection) url
                        .openConnection();
                connection.setHostnameVerifier(DUMMY_VERIFIER);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                connection.setRequestProperty("SOAPAction",
                        "http://HRLVMNG/IHMWebservice.tws/getAllActiveTasks");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //XML
                String reqXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ihm=\"http://HRLVMNG/IHMWebservice.tws\">\n" +
                        "<soapenv:Header/>\n" +
                        "<soapenv:Body>\n" +
                        "<ihm:getAllActiveTasks>\n" +
                        "<ihm:username>" + username + "</ihm:username>\n" +
                        "</ihm:getAllActiveTasks>\n" +
                        "</soapenv:Body>\n" +
                        "</soapenv:Envelope>";

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(reqXML);
                wr.flush();

                int responseCode = connection.getResponseCode();
                System.out.println("Code ... " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));;
                    StringBuilder sb = new StringBuilder();

                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(sb.toString()));

                    Document doc = db.parse(is);
                    //NodeList nodes = doc.getElementsByTagName("item");

                    NodeList tasks = doc.getElementsByTagName("tasks");
                    Element elementTask = (Element) tasks.item(0);

                    NodeList items = elementTask.getChildNodes();
                    System.out.println("node list" + items.getLength());
                    for (int i = 0; i < items.getLength(); i++) {
                        Element element = (Element) items.item(i);

                        TaskDetail task = new TaskDetail();

                        NodeList name = element.getElementsByTagName("instanceId");
                        Element line = (Element) name.item(0);
                        if(line != null){
                            task.setInstanceId(line.getTextContent());
                            System.out.println("instanceId: " + line.getTextContent());
                        }

                        NodeList title = element.getElementsByTagName("taskId");
                        line = (Element) title.item(0);
                        if(line != null){
                            task.setTaskId(line.getTextContent());
                            System.out.println("taskId: " + line.getTextContent());
                        }

                        NodeList owner = element.getElementsByTagName("owner");
                        line = (Element) owner.item(0);
                        if(line != null){
                            task.setOwnerTask(line.getTextContent());
                            System.out.println("owner: " + line.getTextContent());
                        }

                        NodeList requester = element.getElementsByTagName("requester");
                        line = (Element) requester.item(0);
                        if(line != null){
                            task.setRequester(line.getTextContent());
                            System.out.println("requester: " + line.getTextContent());
                        }

                        NodeList startDate = element.getElementsByTagName("startDate");
                        line = (Element) startDate.item(0);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        if(line != null){
                            String sDate1 = line.getTextContent();
                            try {
                                Calendar c = Calendar.getInstance();
                                c.setTime(sdf.parse(sDate1));
                                c.add(Calendar.DATE, 1);

                                String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                                task.setStartDate(date);

                                System.out.println(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        NodeList endDate = element.getElementsByTagName("endDate");
                        line = (Element) endDate.item(0);
                        if(line != null){
                            String sDate2 = line.getTextContent();

                            try {
                                Calendar c = Calendar.getInstance();
                                c.setTime(sdf.parse(sDate2));
                                c.add(Calendar.DATE, 1);

                                String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                                task.setEndDate(date);

                                System.out.println(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        NodeList leaveType = element.getElementsByTagName("leaveType");
                        if(leaveType.getLength() > 0){
                            task.setLeaveType(leaveType.item(0).getLastChild().getTextContent());
                            System.out.println("leaveType: " + leaveType.item(0).getLastChild().getTextContent());
                        }

                        NodeList requestText = element.getElementsByTagName("requestText");
                        line = (Element) requestText.item(0);
                        if(line != null){
                            task.setDescription(line.getTextContent());
                            System.out.println("owner: " + line.getTextContent());
                        }

                        NodeList requesterUsername = element.getElementsByTagName("requesterUsername");
                        line = (Element) requesterUsername.item(0);
                        if(line != null){
                            task.setRequesterName(line.getTextContent());
                            System.out.println("owner: " + line.getTextContent());
                        }

                        NodeList subject = element.getElementsByTagName("subject");
                        line = (Element) subject.item(0);
                        if(line != null){
                            task.setSubject(line.getTextContent());
                            System.out.println("subject: " + line.getTextContent());
                        }

                        taskDetails.add(task);
                    }
                    return true;
                }

                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result != null) {
                //Log.i("Message", "Successful - " + taskDetails);
                if(taskDetails.size() == 0){
                    mListView.setAdapter(null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    // set title
                    alertDialogBuilder.setTitle("Alert");

                    // set dialog message
                    alertDialogBuilder.setMessage("There is no leave request now.");
                    alertDialogBuilder.setNeutralButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {

                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{
                    TaskAdapter adapter = new TaskAdapter(mContext, taskDetails, username);
                    mListView.setAdapter(adapter);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
