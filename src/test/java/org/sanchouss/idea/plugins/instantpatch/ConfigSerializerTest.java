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
public class ConfigSerializerTest {
    static final String path = "instantPatchConfig.xml";

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

        String classFilesDir = "/home/instantpatch/classes/";
        String tempDir = "/home/instantpatch/tmp_ftp/";
        ArrayList<String> etcFilesDirs = new ArrayList<>(Arrays.asList("/usr/bin/", "/var/lib/", "/home/instantpatch/resources/"));

        ProcList genProcList = (String name) ->
        {
            ArrayList<Process> res = new ArrayList<>();
            {
                Process proc = new Process();
                proc.setProcessName(name + ".myservice");
                proc.setProcessDirectory("/home/instantpatch/myservice/");
                proc.setProcessStartCommand("systemctl --user start myservice.service");
                proc.setProcessStopCommand("systemctl --user stop myservice.service");
                proc.setClassFilesDirectory(classFilesDir);
                proc.setRemoteDirectories(etcFilesDirs);
                proc.setTemporaryDirectory(tempDir);
                res.add(proc);
            }
            {
                Process proc = new Process();
                proc.setProcessName(name + ".server-process");
                proc.setProcessDirectory("/usr/bin/");
                proc.setProcessStartCommand("server-process start");
                proc.setProcessStopCommand("server-process stop");
                proc.setClassFilesDirectory(classFilesDir);
                proc.setRemoteDirectories(etcFilesDirs);
                proc.setTemporaryDirectory(tempDir);
                res.add(proc);
            }
            return res;
        };

        ArrayList<Host> hlist = new ArrayList<>();
        {
            String username = "instantpatch";
            String hostname = "host.a";

            Host host = new Host();
            host.setHostname(hostname);
            host.setUsername(username);

            host.setProcesses(genProcList.gen("a"));

            hlist.add(host);
        }
        {
            String username = "instantpatch";
            String hostname = "host.b";

            Host host = new Host();
            host.setHostname(hostname);
            host.setUsername(username);

            host.setProcesses(genProcList.gen("b"));

            hlist.add(host);
        }

        Configuration conf = new Configuration();
        conf.setHosts(hlist);

        return conf;
    }

    @FunctionalInterface
    interface ProcList {
        ArrayList<Process> gen(String name);
    }
}