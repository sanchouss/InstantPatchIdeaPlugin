package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.openapi.util.text.StringUtil;
import com.jcraft.jsch.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import static org.sanchouss.idea.plugins.instantpatch.JschCredentials.*;

/**
 *
 * Test abilities of 'exec' channel
 *
 * Created by Alexander Perepelkin
 */
public class JschChannelExecTest {

    // test of 'exec' channel
    @Test
    public void testJschChannelExec() throws InterruptedException, JSchException, IOException, URISyntaxException {

        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey, StringUtil.isEmpty(passphrase) ? null : passphrase);

            Session session = jsch.getSession(user, host, port);
            System.out.println("session created.");

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications",
                    "publickey,keyboard-interactive,password");
            session.setConfig(config);
            try {
                session.connect();
            } catch (JSchException e) {
                throw new RuntimeException("Can not connect Jsch session: " + e.getMessage(), e);
            }
            System.out.println("Jsch session connected.....");

            ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

            channelExec.setCommand("pwd");
            channelExec.setInputStream(null);

            InputStream[] inputs = new InputStream [] {channelExec.getInputStream(), channelExec.getExtInputStream(), channelExec.getErrStream()};

            channelExec.connect();
            final long now = System.currentTimeMillis();
            boolean gotResult = false;
            while (!gotResult && System.currentTimeMillis() - now < 1000) {
                for (int i = 0; i < inputs.length; ++i)  {
                    InputStream input = inputs[i];
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String line;
                    StringBuilder builder = new StringBuilder();
                    if (reader.ready()) {
                        while ((line = reader.readLine()) != null) {
                            builder.append(line).append("\n");
                        }
                        gotResult = true;
                        System.out.println("Got reply from input#" + i + ":\n" + builder.toString());
                        break;
                    }
                }
                Thread.sleep(50);
            }

            int exitStatus = channelExec.getExitStatus();
            channelExec.disconnect();
            System.out.println("Executing command exit status: " + exitStatus);

            // It must not be recommended, but if you want to skip host-key check,
            // invoke following,
            // session.setConfig("StrictHostKeyChecking", "no");

            //session.connect();
//            session.connect(30000);   // making a connection with timeout.

            Channel channel=session.openChannel("shell");

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
            channel.connect(3 * 1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}