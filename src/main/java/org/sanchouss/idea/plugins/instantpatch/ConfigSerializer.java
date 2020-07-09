package org.sanchouss.idea.plugins.instantpatch;

import org.sanchouss.idea.plugins.instantpatch.settings.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by Alexander Perepelkin
 */
public class ConfigSerializer {

    public static Configuration read(String path) throws JAXBException {
        File file = new File(path);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Configuration conf = (Configuration) jaxbUnmarshaller.unmarshal(file);
        System.out.println(conf);

        return conf;
    }

    public static void write(String path, Configuration config) {
        System.out.println("Writing...");

        try {
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(config, file);
            jaxbMarshaller.marshal(config, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


}
