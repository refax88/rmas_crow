package traffic3.simulator;

import java.io.*;
import java.net.*;


/**
 * @author Eric Bruno
 */

public class SocketClient {
    public boolean connected = false;
    Socket conn = null;
    BufferedReader reader = null;
    BufferedOutputStream os = null;
    public SocketClient(String IPAddress) {
        try {
        conn = new Socket( IPAddress, 8080 );
        conn.setTcpNoDelay(true);
        this.reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        this.os = new BufferedOutputStream( conn.getOutputStream() );
        } catch ( Exception e ) {
            connected = false;
            e.printStackTrace();
        }
        this.connected = true;
    
    }
    
    public void sendToServer(String request) {
        
        try {
                os.write( request.getBytes() );
                os.flush();
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    
    public String readFromServer() {
    try {
                    
        boolean listening = true;
        String line;
        while (listening) {
            line = reader.readLine();
            return line;
        }
        return "";
    } catch ( Exception e ) {
                e.printStackTrace();
                return "";
        }
    }
    
    public boolean isConnected() {
        return this.connected;
}
}