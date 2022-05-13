package com.hsbc.sso.factory;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Privat on 13/05/14.
 */
public class SPCredentials {
    private static final String KEY_STORE_PASSWORD = "123456";
    private static final String KEY_STORE_ENTRY_PASSWORD = "123456";
    private static final String KEY_STORE_PATH = "/server.keystore";
    private static final String KEY_ENTRY_ID = "spkeystore";

    private static final Credential credential;

    static {
        try {
            KeyStore keystore = readKeystoreFromFile(KEY_STORE_PATH, KEY_STORE_PASSWORD);
            Map<String, String> passwordMap = new HashMap<String, String>();
            passwordMap.put(KEY_ENTRY_ID, KEY_STORE_ENTRY_PASSWORD);
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

            Criterion criterion = new EntityIdCriterion(KEY_ENTRY_ID);
            CriteriaSet criteriaSet = new CriteriaSet();
            criteriaSet.add(criterion);

            credential = resolver.resolveSingle(criteriaSet);

        } catch (ResolverException e) {
            throw new RuntimeException("Something went wrong reading credentials", e);
        }
    }

    private static KeyStore readKeystoreFromFile(String pathToKeyStore, String keyStorePassword) {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream inputStream = SPCredentials.class.getResourceAsStream(pathToKeyStore);
            keystore.load(inputStream, keyStorePassword.toCharArray());
            inputStream.close();
            return keystore;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Credential getCredential() {
        return credential;
    }

    public static void validate(Signature signature, Credential credential) throws SignatureException {
        SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
        profileValidator.validate(signature);
        SignatureValidator.validate(signature, credential);
    }

    public static void main(String[] args) {
        Credential credentials = SPCredentials.getCredential();
        System.out.println(credentials);
    }


}
