package org.sanchouss.idea.plugins.instantpatch;

import org.sanchouss.idea.plugins.instantpatch.settings.Configuration;
import org.sanchouss.idea.plugins.instantpatch.settings.Host;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;
import com.google.common.collect.Lists;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;

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

        String classFilesDir = "/home/sanchouss/patches/";
        String tempDir = "/home/sanchouss/tmp/";
        ArrayList<String> etcFilesDirs = Lists.newArrayList("/usr/bin/", "/var/lib/");

        ArrayList<Process> alist = Lists.newArrayList();
        {
            Process proc = new Process();
            proc.setProcessName("server-process1");
            proc.setProcessDirectory("/usr/bin/");
            proc.setProcessStartCommand("server-process1 start");
            proc.setProcessStopCommand("server-process1 stop");
            proc.setClassFilesDirectory(classFilesDir);
            proc.setRemoteDirectories(etcFilesDirs);
            proc.setTemporaryDirectory(tempDir);
            alist.add(proc);
        }
        {
            Process proc = new Process();
            proc.setProcessName("server-process2");
            proc.setProcessDirectory("/usr/bin/");
            proc.setProcessStartCommand("server-process2 start");
            proc.setProcessStopCommand("server-process2 stop");
            proc.setClassFilesDirectory(classFilesDir);
            proc.setRemoteDirectories(etcFilesDirs);
            proc.setTemporaryDirectory(tempDir);
            alist.add(proc);
        }

        ArrayList<Host> hlist = Lists.newArrayList();
        {
            String username = "sanchouss";
            String hostname = "hosta";

            Host host = new Host();
            host.setHostname(hostname);
            host.setUsername(username);

            host.setProcesses(alist);

            hlist.add(host);
        }
        {
            String username = "sanchouss";
            String hostname = "hostb";

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