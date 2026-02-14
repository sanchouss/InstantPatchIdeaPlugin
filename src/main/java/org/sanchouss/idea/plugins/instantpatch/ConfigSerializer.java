package org.sanchouss.idea.plugins.instantpatch;

import org.sanchouss.idea.plugins.instantpatch.settings.Configuration;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by Alexander Perepelkin
 */
public class ConfigSerializer {

    public static Configuration read(String path) throws JAXBException {
        final ClassLoader pluginCl = Configuration.class.getClassLoader();
        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();

        try {
            // need to change ContextClassLoader for ServiceLoader to locate JAXBContext within plugin's jar bundle
            Thread.currentThread().setContextClassLoader(pluginCl);
            // only passing plugin's classloader is not enough to lookup and causes JAXBException
            // JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class.getName(), pluginCl);

            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            File file = new File(path);
            Configuration conf = (Configuration) jaxbUnmarshaller.unmarshal(file);
            System.out.println(conf);

            return conf;
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }

    public static void write(String path, Configuration config) throws JAXBException {
        final ClassLoader pluginCl = Configuration.class.getClassLoader();
        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();

        try {
            // need to change ContextClassLoader for ServiceLoader to locate JAXBContext within plugin's jar bundle
            Thread.currentThread().setContextClassLoader(pluginCl);

            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(path);
            jaxbMarshaller.marshal(config, file);
            jaxbMarshaller.marshal(config, System.out);

        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }


}
