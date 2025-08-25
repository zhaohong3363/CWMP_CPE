package cn.sct.networkmanager.agent.domain.model;

import jakarta.xml.bind.*;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JAXBUtils {
    private static final Map<Class, JAXBContext> contextCache=new ConcurrentHashMap<>();

    public static String marshal(Object obj) throws JAXBException {
        JAXBContext context = contextCache.computeIfAbsent(obj.getClass(), c -> {
            try {
                return JAXBContext.newInstance(c);
            } catch (JAXBException e) {
                throw new RuntimeException("Failed to create JAXB context for " + c.getName(), e);
            }
        });
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(obj, writer);
        String replace = writer.toString().replace("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"", "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + replace;
    }

    public static <T> T unmarshal(String xml, Class<T> clazz) throws JAXBException {
        JAXBContext context = contextCache.computeIfAbsent(clazz, c -> {
            try {
                return JAXBContext.newInstance(c);
            } catch (JAXBException e) {
                throw new RuntimeException("Failed to create JAXB context for " + c.getName(), e);
            }
        });
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return clazz.cast(unmarshaller.unmarshal(new StringReader(xml)));
    }
}
