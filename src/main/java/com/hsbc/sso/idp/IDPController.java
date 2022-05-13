package com.hsbc.sso.idp;

import com.hsbc.sso.factory.OpenSAMLUtils;
import com.hsbc.sso.factory.SAMLFactory;
import com.hsbc.sso.factory.SPCredentials;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Class
 * Created by wwx193433
 * 2019-12-07 17:55
 */

@Controller
@RequestMapping("/idp")
public class IDPController {

    private static Logger logger = LoggerFactory.getLogger(IDPController.class);
    private static final String SENDER_METADATA_PATH = "sender-metadata.xml";
    private static final String IPD_SSO_DESTINATION = "http://localhost:8080/idp/singleSignOn";
    private static final String SP_ASSERTION_CONSUMER_SERVICE_URL = "http://localhost:4502/content/saml_login";
    private static final String SP_ISSUED_ID = "TestSP";
    private static final String IDP_ISSUED_ID = "TestIDP";

    @RequestMapping("/index")
    public String index(){
        System.out.println("success");
        return "auth";
    }


    @RequestMapping("/singleSignOn")
    public String singleSignOn(HttpServletRequest req){
        HTTPPostDecoder decoder = new HTTPPostDecoder();
        decoder.setHttpServletRequest(req);

        AuthnRequest authnRequest;
        try {
            decoder.initialize();

            decoder.decode();
            MessageContext messageContext = decoder.getMessageContext();
            authnRequest = (AuthnRequest) messageContext.getMessage();

            //validate signature
            SPCredentials.validate(authnRequest.getSignature(), SPCredentials.getCredential());
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("success");
        return "auth";
    }

    @RequestMapping("/save")
    public void save(HttpServletRequest req, HttpServletResponse resp){
        System.out.println(1111);
        System.out.println(req);
        Response response = buildSAMLResponse();
        OpenSAMLUtils.logSAMLObject(response);
        System.out.println(222);

        sendMessageUsingPOST(resp, response);
    }




    public Response buildSAMLResponse(){
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

        Assertion assertion = buildAssertion();

//        signAssertion(assertion);
//        EncryptedAssertion encryptedAssertion = encryptAssertion(assertion);

        response.getAssertions().add(assertion);
        return response;
    }


    private void sendMessageUsingPOST(HttpServletResponse httpServletResponse, Response response) {

        MessageContext context = new MessageContext();

        context.setMessage(response);

//        SAMLBindingContext bindingContext = context.getSubcontext(SAMLBindingContext.class, true);
//        bindingContext.setRelayState("teststate");

        SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true);

        SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
        endpointContext.setEndpoint(URLToEndpoint(SP_ASSERTION_CONSUMER_SERVICE_URL));

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();

        HTTPPostEncoder encoder = new HTTPPostEncoder();

        encoder.setMessageContext(context);
        encoder.setHttpServletResponse(httpServletResponse);
        encoder.setVelocityEngine(velocityEngine);

        try {
            encoder.initialize();
        } catch (ComponentInitializationException e) {
            throw new RuntimeException(e);
        }

        logger.info("Sending auto-sumbitting form to receiver with AuthnRequest");
        try {
            encoder.encode();
        } catch (MessageEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Endpoint URLToEndpoint(String URL) {
        SingleSignOnService endpoint = OpenSAMLUtils.buildSAMLObject(SingleSignOnService.class);
        endpoint.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        endpoint.setLocation(URL);
        return endpoint;
    }


    private Assertion buildAssertion() {

        Assertion assertion = OpenSAMLUtils.buildSAMLObject(Assertion.class);

        Issuer issuer = OpenSAMLUtils.buildSAMLObject(Issuer.class);
        issuer.setValue(IDP_ISSUED_ID);
        assertion.setIssuer(issuer);
        assertion.setIssueInstant(Instant.now());

        assertion.setID(OpenSAMLUtils.generateSecureRandomId());

        Subject subject = OpenSAMLUtils.buildSAMLObject(Subject.class);
        assertion.setSubject(subject);

        NameID nameID = OpenSAMLUtils.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        nameID.setValue(SP_ISSUED_ID);
        nameID.setSPNameQualifier(SP_ISSUED_ID);
        nameID.setNameQualifier(SP_ISSUED_ID);

        subject.setNameID(nameID);

        subject.getSubjectConfirmations().add(buildSubjectConfirmation());

        assertion.setConditions(buildConditions());

        assertion.getAttributeStatements().add(buildAttributeStatement());

        assertion.getAuthnStatements().add(buildAuthnStatement());

        return assertion;
    }

    private SubjectConfirmation buildSubjectConfirmation() {
        SubjectConfirmation subjectConfirmation = OpenSAMLUtils.buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);

        SubjectConfirmationData subjectConfirmationData = OpenSAMLUtils.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setInResponseTo("Made up ID");
        subjectConfirmationData.setNotBefore(Instant.now());
        subjectConfirmationData.setNotOnOrAfter(Instant.now().plus(10, ChronoUnit.MINUTES));
        subjectConfirmationData.setRecipient(SP_ASSERTION_CONSUMER_SERVICE_URL);

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        return subjectConfirmation;
    }

    private AuthnStatement buildAuthnStatement() {
        AuthnStatement authnStatement = OpenSAMLUtils.buildSAMLObject(AuthnStatement.class);
        AuthnContext authnContext = OpenSAMLUtils.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = OpenSAMLUtils.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setURI(AuthnContext.SMARTCARD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);

        authnStatement.setAuthnInstant(Instant.now());

        return authnStatement;
    }

    private Conditions buildConditions() {
        Conditions conditions = OpenSAMLUtils.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(Instant.now());
        conditions.setNotOnOrAfter(Instant.now().plus(10, ChronoUnit.MINUTES));
        AudienceRestriction audienceRestriction = OpenSAMLUtils.buildSAMLObject(AudienceRestriction.class);
        Audience audience = OpenSAMLUtils.buildSAMLObject(Audience.class);
        audience.setURI(SP_ASSERTION_CONSUMER_SERVICE_URL);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private AttributeStatement buildAttributeStatement() {
        AttributeStatement attributeStatement = OpenSAMLUtils.buildSAMLObject(AttributeStatement.class);

        Attribute attributeUserName = OpenSAMLUtils.buildSAMLObject(Attribute.class);

        XSStringBuilder stringBuilder = (XSStringBuilder) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(XSString.TYPE_NAME);
        XSString userNameValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        userNameValue.setValue("admin");

        attributeUserName.getAttributeValues().add(userNameValue);
        attributeUserName.setName("username");
        attributeStatement.getAttributes().add(attributeUserName);

        return attributeStatement;

    }

}
