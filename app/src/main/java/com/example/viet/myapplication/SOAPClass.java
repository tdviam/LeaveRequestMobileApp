package com.example.viet.myapplication;

import android.os.StrictMode;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by iet on 22-Feb-18.
 */

public class SOAPClass {
    private static String SOAP_ACTION = "http://HRLVMNG/test.tws/testHeader";
    private static String NAMESPACE = "http://HRLVMNG/test.tws";
    private static String METHOD_NAME = "testHeader";
    private static String URL = "https://bpm-85-pc:9443/teamworks/webservices/INHMC/test.tws";



    public String remotereq() {

        // Strict mode is defined because executing network operations in the
        // main
        // thread will give exception
        // Strict mode is available only above version 9
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Create the soap request object
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        // Create the envelop.Envelop will be used to send the request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        // Says that the soap webservice is a .Net service
        envelope.dotNet = true;

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        String s = "";
        Double x = 1.0;
        try {
            androidHttpTransport.call(SOAP_ACTION, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            // Output to the log
            s = response.toString();
            //x = Double.parseDouble(s) * Double.parseDouble(value);
            Log.d("Converter", response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return x.toString();

    }
}
