package org.sanchouss.idea.plugins.instantpatch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteAuth;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientImpl;

/**
 * Created by Alexander Perepelkin
 */

public class ShellTest {
    public static void main(String[] arg){

        try{
            //jsch.setKnownHosts("/home/foo/.ssh/known_hosts");

            String user = "user";
            String host = "host.com";
            int port = 22;

            RemoteClientImpl rc = new RemoteClientImpl(host, user, port, new RemoteAuth("", ""));

            Session session= rc.getSession();


            // It must not be recommended, but if you want to skip host-key check,
            // invoke following,
            // session.setConfig("StrictHostKeyChecking", "no");

            //session.connect();
//            session.connect(30000);   // making a connection with timeout.

//            Channel channel=session.openChannel("shell");
            Channel channel= rc.getChannelShell();

            // Enable agent-forwarding.
            //((ChannelShell)channel).setAgentForwarding(true);

            channel.setInputStream(System.in);
      /*
      // a hack for MS-DOS prompt on Windows.
      channel.setInputStream(new FilterInputStream(System.in){
          public int read(byte[] b, int off, int len)throws IOException{
            return in.read(b, off, (len>1024?1024:len));
          }
        });
       */

            channel.setOutputStream(System.out);

      /*
      // Choose the pty-type "vt102".
      ((ChannelShell)channel).setPtyType("vt102");
      */

      /*
      // Set environment variable "LANG" as "ja_JP.eucJP".
      ((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
      */

            //channel.connect();
            channel.connect(3*1000);
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

}