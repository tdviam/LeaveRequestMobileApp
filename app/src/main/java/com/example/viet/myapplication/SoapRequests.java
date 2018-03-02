package com.example.viet.myapplication;

import android.os.StrictMode;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by iet on 22-Feb-18.
 */

public class SoapRequests {
    private static String SOAP_ACTION = "http://HRLVMNG/test.tws/testHeader";
    private static String NAMESPACE = "http://HRLVMNG/test.tws";
    private static String METHOD_NAME = "testHeader";
    private static String URL = "https://10.140.0.4:9443/teamworks/webservices/INHMC/test.tws";

    public String callBPM() {
        String data = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Create the soap request object
        /*SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        // Create the envelop.Envelop will be used to send the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        // Says that the soap webservice is a .Net service
        //envelope.dotNet = true;

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {

            androidHttpTransport.call(SOAP_ACTION, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            // Output to the log
            data = response.toString();
            //x = Double.parseDouble(s) * Double.parseDouble(value);
            Log.d("Converter", response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            //String certificatesTrustStorePath = "C:\\Program Files\\Java\\jre1.8.0_161\\lib\\security\\cacerts";
            //System.setProperty("javax.net.ssl.trustStore", certificatesTrustStorePath);
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("10.140.0.4", session);
                }
            };

            URL url = new URL("https://10.140.0.4:9443/teamworks/webservices/INHMC/test.tws");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            //conn.setDoOutput(true);
            conn.setHostnameVerifier(hostnameVerifier);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return data;
    }
}
