package com.example.viet.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Viet on 26-Feb-18.
 */

public class TaskAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<TaskDetail> mDataSource;
    private static String URL = "https://10.140.0.4:9443/teamworks/webservices/INHMC/IHMWebservice.tws";
    private boolean exisingRequest = false;
    private String username;
    private static final HostnameVerifier DUMMY_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public TaskAdapter(Context context, ArrayList<TaskDetail> items, String username) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.username = username;
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.list_item_task, viewGroup, false);

        // Get instance element
        TextView instanceId =
                (TextView) rowView.findViewById(R.id.instanceId);

        // Get owner element
        TextView owner =
                (TextView) rowView.findViewById(R.id.owner);

        // Get requester element
        TextView requester =
                (TextView) rowView.findViewById(R.id.requester);

        // Get startDate element
        TextView startDate =
                (TextView) rowView.findViewById(R.id.startDate);

        // Get endDate element
        TextView endDate =
                (TextView) rowView.findViewById(R.id.endDate);

        // Get leaveType element
        TextView leaveType =
                (TextView) rowView.findViewById(R.id.leaveType);

        // Get description element
        TextView description =
                (TextView) rowView.findViewById(R.id.description);

        // Button
        Button btnApprove = (Button) rowView.findViewById(R.id.btnSubmit);
        Button btnReject = (Button) rowView.findViewById(R.id.btnCancel);

        final TaskDetail task = (TaskDetail) getItem(i);
        instanceId.setText(task.getSubject());
        owner.setText(task.getOwnerTask());
        requester.setText(task.getRequester());
        startDate.setText(task.getStartDate());
        endDate.setText(task.getEndDate());
        leaveType.setText(task.getLeaveType());
        description.setText(task.getDescription());

        btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.i("List View Click Item", ":" + task.getInstanceId());
                //Intent intent = new Intent(mContext, TaskDetailActivity.class);
                //mContext.startActivity(intent);
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bpm-85-pc:9443/teamworks/process.lsw?zWorkflowState=1&zTaskId=130357&zResetContext=true"));
                //mContext.startActivity(browserIntent);
                Boolean result = false;
                CompleteTask completeTask = new CompleteTask(task.getInstanceId(), task.getTaskId(), "Approved");
                try {
                    result = completeTask.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Log.i("Result", result == true ? "true" : "false");
                Log.i("Existing Request", exisingRequest == true ? "true" : "false");
                if(exisingRequest){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getRootView().getContext());
                    // set title
                    alertDialogBuilder.setTitle("Alert");

                    // set dialog message
                    alertDialogBuilder.setMessage("One or more of your requested leave dates are duplicated to your other submission, or your request has been processing now.!");
                    alertDialogBuilder.setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0,
                                                int arg1) {

                            }
                        }
                    );

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{
                    mDataSource.remove(task);
                    notifyDataSetChanged();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getRootView().getContext());
                    // set title
                    alertDialogBuilder.setTitle("Alert");

                    // set dialog message
                    alertDialogBuilder.setMessage("The leave request is approved!");
                    alertDialogBuilder.setNeutralButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {

                                }
                            }
                    );

                    //
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }


            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CompleteTask completeTask = new CompleteTask(task.getInstanceId(), task.getTaskId(), "Rejected");
                completeTask.execute();

                mDataSource.remove(task);
                notifyDataSetChanged();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getRootView().getContext());
                // set title
                alertDialogBuilder.setTitle("Alert");

                // set dialog message
                alertDialogBuilder.setMessage("The leave request is rejected!");
                alertDialogBuilder.setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0,
                                                int arg1) {

                            }
                        }
                );

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        return rowView;
    }

    private class CompleteTask extends AsyncTask<Void, Void, Boolean> {

        private String instanceId;
        private String taskId;
        private String flowStatus;

        public CompleteTask (String instanceId, String taskId, String flowStatus){
            this.instanceId = instanceId;
            this.taskId = taskId;
            this.flowStatus = flowStatus;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {

                URL url = new URL(URL);

                HttpsURLConnection connection = (HttpsURLConnection) url
                        .openConnection();
                connection.setHostnameVerifier(DUMMY_VERIFIER);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                connection.setRequestProperty("SOAPAction",
                        "http://HRLVMNG/IHMWebservice.tws/completeTask");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //XML
                String reqXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ihm=\"http://HRLVMNG/IHMWebservice.tws\">\n" +
                        "<soapenv:Header/>\n" +
                        "<soapenv:Body>\n" +
                        "<ihm:completeTask>\n" +
                        "<ihm:instanceId>" + instanceId + "</ihm:instanceId>\n" +
                        "<ihm:taskId>" + taskId + "</ihm:taskId>\n" +
                        "<ihm:flowStatus>" + flowStatus + "</ihm:flowStatus>\n" +
                        "<ihm:username>" + username + "</ihm:username>\n" +
                        "</ihm:completeTask>\n" +
                        "</soapenv:Body>\n" +
                        "</soapenv:Envelope>";

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(reqXML);
                wr.flush();

                int responseCode = connection.getResponseCode();
                System.out.println("Code ... " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK && flowStatus != "Rejected") {
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

                    NodeList tasks = doc.getElementsByTagName("truncatedRequest");

                    Element elementTask = (Element) tasks.item(0);
                    exisingRequest = Boolean.valueOf(elementTask.getTextContent());
                    System.out.println("node list" + Boolean.valueOf(elementTask.getTextContent()));
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
        }

    }
}