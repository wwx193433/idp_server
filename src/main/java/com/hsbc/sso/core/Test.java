package com.hsbc.sso.core;

import com.hsbc.sso.factory.OpenSAMLUtils;
import com.hsbc.sso.factory.SAMLFactory;
import org.apache.commons.codec.CodecPolicy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class Test {
    public static void main(String[] args) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
//        SAMLFactory.initOpenSAML();
//        checkAlias();

        KeyStore keystore = KeyStore.getInstance("JKS");

        keystore.load(Test.class.getClassLoader().getResourceAsStream("/server.keystore"), "123456".toCharArray());

        try {
            PrivateKey key = (PrivateKey)keystore.getKey("spkeystore", "123456".toCharArray());
            System.out.println(key);
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }

    public static void checkAlias() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        FileInputStream is = new FileInputStream(new File("/Users/apple/tool/aem_cert/server.keystore"));
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "123456".toCharArray());
        Enumeration aliasEnum = keyStore.aliases();
        String keyAlias = "" ;
        while (aliasEnum.hasMoreElements()) {
            keyAlias = (String) aliasEnum.nextElement();
            System.out.println("别名"+keyAlias);
        }

    }
}
