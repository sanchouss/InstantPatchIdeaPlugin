package org.sanchouss.idea.plugins.instantpatch.settings;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Created by Alexander Perepelkin
 */
@XmlRootElement
public class Configuration {
    private ArrayList<Host> hosts;

    public ArrayList<Host> getHosts() {
        return hosts;
    }

    @XmlElementWrapper(name = "hostsList")
    @XmlElement(name = "host")
    public void setHosts(ArrayList<Host> hosts) {
        this.hosts = hosts;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "hosts=" + hosts +
                '}';
    }
}
