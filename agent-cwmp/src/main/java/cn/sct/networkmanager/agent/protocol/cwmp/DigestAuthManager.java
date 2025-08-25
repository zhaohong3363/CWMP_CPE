package cn.sct.networkmanager.agent.protocol.cwmp;

import io.netty.handler.codec.http.DefaultFullHttpRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class DigestAuthManager extends AnonymousAuthManager{
    private final String userName;
    private final String password;
    private final String realm;
    private final String nonce;
    private final String uri;
    private final String httpMethod;
    private final String qop ;
    private final AtomicInteger ncCounter = new AtomicInteger(1); // 需要持久化的计数器

    public DigestAuthManager(String cookie,
                             String userName,
                             String password,
                             String realm,
                             String nonce,
                             String uri,
                             String httpMethod,
                             String qop) {
        super(cookie);
        this.userName = userName;
        this.password = password;
        this.realm = realm;
        this.nonce = nonce;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.qop = qop;
    }
    @Override
    public void authenticate(DefaultFullHttpRequest request) {
        super.authenticate(request);
        request.headers().set("Authorization", calculateDigestResponse());
    }
    private  String calculateDigestResponse() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String a1 = userName + ":" + realm + ":" + password;
            String ha1 = md5Hex(md, a1);
            String a2 = httpMethod + ":" + uri;
            String ha2 = md5Hex(md, a2);
            String nc = String.format("%08x", ncCounter.getAndAdd(1));
            String cnonce = generateCnonce();
            String responseData = ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2;
           return String.format(
                    "Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", " +
                            "uri=\"%s\", qop=%s, nc=%s, cnonce=\"%s\", " +
                            "response=\"%s\", algorithm=MD5",
                    userName, realm, nonce, uri, qop, nc, cnonce, md5Hex(md, responseData)
            );

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }
    private  String md5Hex(MessageDigest md, String data) {
        byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
        // 替换 HexFormat 为 JDK 11 兼容的实现
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    private String generateCnonce() {
        byte[] bytes = new byte[4];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
