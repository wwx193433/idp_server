package com.hsbc.sso;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class CoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            init();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    public static void init() throws ServletException {
        try {
            XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
            ConfigurationService.register(XMLObjectProviderRegistry.class, registry);

            registry.setParserPool(getParserPool());

            System.out.println("Initializing");
            InitializationService.initialize();
            System.out.println(1111);
        } catch (InitializationException e) {
            throw new RuntimeException("Initialization failed");
        }
    }

    private static ParserPool getParserPool() {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.setMaxPoolSize(100);
        parserPool.setCoalescing(true);
        parserPool.setIgnoreComments(true);
        parserPool.setIgnoreElementContentWhitespace(true);
        parserPool.setNamespaceAware(true);
        parserPool.setExpandEntityReferences(false);
        parserPool.setXincludeAware(false);

        final Map<String, Boolean> features = new HashMap<String, Boolean>();
        features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
        features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
        features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
        features.put("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
        features.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);

        parserPool.setBuilderFeatures(features);

        parserPool.setBuilderAttributes(new HashMap<String, Object>());

        try {
            parserPool.initialize();
        } catch (ComponentInitializationException e) {
            e.printStackTrace();
        }

        return parserPool;
    }

}
