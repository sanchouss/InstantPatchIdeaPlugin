package org.sanchouss.idea.plugins.instantpatch.settings;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;

/**
 * Created by Alexander Perepelkin
 */
public class Host {
    private String hostname;
    private String username;

    private ArrayList<Process> processes;

    public String getHostname() {
        return hostname;
    }

    @XmlElement
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    @XmlElement
    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<Process> getProcesses() {
        return processes;
    }

    @XmlElementWrapper(name = "processesList")
    @XmlElement(name = "process")
    public void setProcesses(ArrayList<Process> processes) {
        this.processes = processes;
    }

    @Override
    public String toString() {
        return "Host{" +
                "hostname='" + hostname + '\'' +
                ", username='" + username + '\'' +
                ", processes=" + processes +
                '}';
    }

}
