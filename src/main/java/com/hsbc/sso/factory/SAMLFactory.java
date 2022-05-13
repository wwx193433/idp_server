package com.hsbc.sso.factory;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SAMLFactory {
    private static Logger logger = LoggerFactory.getLogger(SAMLFactory.class);

    public static void initOpenSAML() {
        XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
        ConfigurationService.register(XMLObjectProviderRegistry.class, registry);

        registry.setParserPool(getParserPool());
        try {
            InitializationService.initialize();
            System.out.println(1112233);
        } catch (InitializationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static ParserPool getParserPool() {
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
            logger.error(e.getMessage(), e);
        }
        return parserPool;
    }
}
