package org.sanchouss.idea.plugins.instantpatch;

import org.junit.Test;
import org.sanchouss.idea.plugins.instantpatch.settings.Configuration;
import org.sanchouss.idea.plugins.instantpatch.settings.Host;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Alexander Perepelkin
 */
public class ConfigMySerializerTest {
    static final String path = "C:\\usr\\tmp\\file.xml";

    @Test
    public void testWriteReadSampleConfig() {
        Configuration config = createSampleConfig();
        ConfigSerializer.write(path, config);
        try {
            ConfigSerializer.read(path);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }
    static Configuration createSampleConfig() {

        String classFilesDir = "/home/alex-pekin/patches/";
        String tempDir = "/home/alex-pekin/tmp/";
        ArrayList<String> etcFilesDirs = new ArrayList<>(Arrays.asList("/usr/bin/"));

        ArrayList<Process> alist = new ArrayList();
        {
            Process proc = new Process();
            proc.setProcessName("clusterizer-dispatcher");
            proc.setProcessDirectory("/etc/init.d/");
            proc.setProcessStartCommand("sudo /etc/init.d/clusterizer-dispatcher start");
            proc.setProcessStopCommand("sudo /etc/init.d/clusterizer-dispatcher stop");
            proc.setClassFilesDirectory(classFilesDir);
            proc.setRemoteDirectories(etcFilesDirs);
            proc.setTemporaryDirectory(tempDir);
            alist.add(proc);
        }
        {
            Process proc = new Process();
            proc.setProcessName("clusterizer-worker");
            proc.setProcessDirectory("/etc/init.d/");
            proc.setProcessStartCommand("sudo /etc/init.d/clusterizer-worker start");
            proc.setProcessStopCommand("sudo /etc/init.d/clusterizer-worker stop");
            proc.setClassFilesDirectory(classFilesDir);
            proc.setRemoteDirectories(etcFilesDirs);
            proc.setTemporaryDirectory(tempDir);
            alist.add(proc);
        }
        {
            Process proc = new Process();
            proc.setProcessName("market-markup-worker");
            proc.setProcessDirectory("/etc/init.d/");
            proc.setProcessStartCommand("sudo /etc/init.d/market-markup-worker start");
            proc.setProcessStopCommand("sudo /etc/init.d/market-markup-worker stop");
            proc.setClassFilesDirectory(classFilesDir);
            proc.setRemoteDirectories(etcFilesDirs);
            proc.setTemporaryDirectory(tempDir);
            alist.add(proc);
        }
        {
            Process proc = new Process();
            proc.setProcessName("market-robot-tms");
            proc.setProcessDirectory("/etc/init.d/");
            proc.setProcessStartCommand("sudo /etc/init.d/market-robot-tms start");
            proc.setProcessStopCommand("sudo /etc/init.d/market-robot-tms stop");
            proc.setClassFilesDirectory(classFilesDir);
            proc.setRemoteDirectories(etcFilesDirs);
            proc.setTemporaryDirectory(tempDir);
            alist.add(proc);
        }

        ArrayList<Host> hlist = new ArrayList();
        {
            String username = "alex-pekin";
            String hostname = "aida";

            Host host = new Host();
            host.setHostname(hostname);
            host.setUsername(username);

            host.setProcesses(alist);

            hlist.add(host);
        }
        {
            String username = "alex-pekin";
            String hostname = "braavos";

            Host host = new Host();
            host.setHostname(hostname);
            host.setUsername(username);

            host.setProcesses(alist);

            hlist.add(host);
        }

        {
            String username = "alex-pekin";
            String hostname = "callisto";

            Host host = new Host();
            host.setHostname(hostname);
            host.setUsername(username);

            host.setProcesses(alist);

            hlist.add(host);
        }


        Configuration conf = new Configuration();
        conf.setHosts(hlist);

        return conf;
    }

}