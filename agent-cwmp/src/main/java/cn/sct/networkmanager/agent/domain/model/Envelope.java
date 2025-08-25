package cn.sct.networkmanager.agent.domain.model;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import jakarta.xml.bind.JAXBException;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public  abstract class Envelope {
    abstract public String getId();
    public ByteBuf content() {
        try{
            return Unpooled.wrappedBuffer(JAXBUtils.marshal(this).getBytes(StandardCharsets.UTF_8));
        }catch (JAXBException e){
            throw new RuntimeException(e);
        }
    }
     public String contentAsString() {
        try{
            return JAXBUtils.marshal(this);
        }catch (JAXBException e){
          throw new RuntimeException(e);
        }

    }
    public static String getId(String content){
       Pattern pattern = Pattern.compile("<cwmp:ID[^>]*>([^<]+)</cwmp:ID>",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        pattern = Pattern.compile("<ID[^>]*>([^<]+)</ID>",
                Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }

}
