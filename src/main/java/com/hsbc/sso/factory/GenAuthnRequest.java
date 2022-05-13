package com.hsbc.sso.factory;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class GenAuthnRequest {
	private static final String IPD_SSO_DESTINATION = "http://localhost:8080/idp/singleSignOn";
	private static final String SP_ASSERTION_CONSUMER_SERVICE_URL = "http://localhost:8080/sp/assertionConsumerService";
	private static final String SP_ISSUED_ID = "IssuerEntityId";
	private static final String IDP_ISSUED_ID = "IssuerEntityId";
	private static Logger logger = LoggerFactory.getLogger(GenAuthnRequest.class);

	public static void main(String[] args) {

		initOpenSAML();
		Response response = buildSAMLResponse();
		OpenSAMLUtils.logSAMLObject(response);



//		AuthnRequest authnRequest = buildAuthnRequest();
//		OpenSAMLUtils.logSAMLObject(authnRequest);


	}


	private static Response buildSAMLResponse(){
		Response response = OpenSAMLUtils.buildSAMLObject(Response.class);
		response.setDestination(SP_ASSERTION_CONSUMER_SERVICE_URL);
		response.setIssueInstant(Instant.now());
		response.setID(OpenSAMLUtils.generateSecureRandomId());
		Issuer issuer2 = OpenSAMLUtils.buildSAMLObject(Issuer.class);
		issuer2.setValue(IDP_ISSUED_ID);

		response.setIssuer(issuer2);

		Status status2 = OpenSAMLUtils.buildSAMLObject(Status.class);
		StatusCode statusCode2 = OpenSAMLUtils.buildSAMLObject(StatusCode.class);
		statusCode2.setValue(StatusCode.SUCCESS);
		status2.setStatusCode(statusCode2);

		response.setStatus(status2);
		return response;
	}


	private static void initOpenSAML() {
		XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
		ConfigurationService.register(XMLObjectProviderRegistry.class, registry);

		registry.setParserPool(getParserPool());
		try {
			InitializationService.initialize();
		} catch (InitializationException e) {
			logger.error(e.getMessage(), e);
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
			logger.error(e.getMessage(), e);
		}

		return parserPool;
	}

	private static AuthnRequest buildAuthnRequest() {
		AuthnRequest authnRequest = OpenSAMLUtils.buildSAMLObject(AuthnRequest.class);
		authnRequest.setIssueInstant(Instant.now());
		authnRequest.setDestination(IPD_SSO_DESTINATION);
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
		authnRequest.setAssertionConsumerServiceURL(SP_ASSERTION_CONSUMER_SERVICE_URL);
		authnRequest.setID(OpenSAMLUtils.generateSecureRandomId());
		authnRequest.setIssuer(buildIssuer());
		authnRequest.setNameIDPolicy(buildNameIdPolicy());

		return authnRequest;
	}

	private static NameIDPolicy buildNameIdPolicy() {
		NameIDPolicy nameIDPolicy = OpenSAMLUtils.buildSAMLObject(NameIDPolicy.class);
		nameIDPolicy.setAllowCreate(true);
		nameIDPolicy.setFormat(NameIDType.TRANSIENT);

		return nameIDPolicy;
	}

	private static Issuer buildIssuer() {
		Issuer issuer = OpenSAMLUtils.buildSAMLObject(Issuer.class);
		issuer.setValue(SP_ISSUED_ID);

		return issuer;
	}

}
