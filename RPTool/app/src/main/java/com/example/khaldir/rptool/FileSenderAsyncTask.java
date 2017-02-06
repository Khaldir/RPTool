package com.example.khaldir.rptool;

import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by JakeT12 on 06/02/2017.
 */

public class FileSenderAsyncTask extends AsyncTask<Void, Void, Void> {

    private InetAddress targetAddress;
    private String message;

    public FileSenderAsyncTask(InetAddress targetAddress, String message){
        this.targetAddress = targetAddress;
        this.message = message;
    }
    @Override
    protected Void doInBackground(Void... params) {
        int len;
        Socket socket = null;
        byte buf[]  = new byte[1024];

        try {
            socket = new Socket(targetAddress, 8888);
            if (!socket.isConnected())
            {
                InetSocketAddress targetSocketAddress = new InetSocketAddress(targetAddress, 8888);
                socket.connect(targetSocketAddress);
            }

            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = null;
            inputStream = new ByteArrayInputStream(message.getBytes());
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }

        return null;
    }
}
