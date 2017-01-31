package com.example.khaldir.rptool;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by JakeT12 on 30/01/2017.
 */

public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private ArrayList<InetAddress> addressConnectionsList;

    public FileServerAsyncTask(Context context) {
        this.context = context;
    }

    public FileServerAsyncTask(Context context, ArrayList<InetAddress> addressConnectionsList) {
        this.context = context;

    }

    @Override
    protected String doInBackground(Void... params) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();

            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */



            InputStream inputstream = client.getInputStream();

            serverSocket.close();
            return convertStreamToString(inputstream);
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Start activity that can handle the JPEG image
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Toast.makeText(context, result,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


}
