package com.facturacion.negocio.services.dian.xml;

import com.facturacion.negocio.services.dian.certificate.DianCertificateData;
import com.facturacion.negocio.services.dian.certificate.DianCertificateLoader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

@Component
public class DianXmlSigner {

    private final DianCertificateLoader dianCertificateLoader;

    public DianXmlSigner(DianCertificateLoader dianCertificateLoader) {
        this.dianCertificateLoader = dianCertificateLoader;
    }

    public String sign(String xml) {
        try {
            Document document = parseXml(xml);

            DianCertificateData certData = dianCertificateLoader.load();
            PrivateKey privateKey = certData.privateKey();
            X509Certificate certificate = certData.certificate();

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

            Reference reference = factory.newReference(
                    "",
                    factory.newDigestMethod(DigestMethod.SHA256, null),
                    List.of(
                            factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                            factory.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null)
                    ),
                    null,
                    null
            );

            SignedInfo signedInfo = factory.newSignedInfo(
                    factory.newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null
                    ),
                    factory.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                    Collections.singletonList(reference)
            );

            KeyInfo keyInfo = buildKeyInfo(factory, certificate);

            DOMSignContext signContext = buildSignContext(document, privateKey);

            XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(signContext);

            return toXmlString(document);

        } catch (Exception e) {
            throw new IllegalStateException("Error firmando XML DIAN", e);
        }
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        return dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private DOMSignContext buildSignContext(Document document, PrivateKey privateKey) {
        NodeList extensionContentList = document.getElementsByTagNameNS(
                "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2",
                "ExtensionContent"
        );

        if (extensionContentList.getLength() == 0) {
            throw new IllegalStateException("No se encontro ext:ExtensionContent en el XML");
        }

        DOMSignContext signContext = new DOMSignContext(privateKey, extensionContentList.item(0));
        signContext.setDefaultNamespacePrefix("ds");
        return signContext;
    }

    private KeyInfo buildKeyInfo(XMLSignatureFactory factory, X509Certificate certificate) {
        KeyInfoFactory kif = factory.getKeyInfoFactory();

        X509Data x509Data = kif.newX509Data(List.of(
                certificate.getSubjectX500Principal().getName(),
                certificate
        ));

        return kif.newKeyInfo(Collections.singletonList(x509Data));
    }

    private String toXmlString(Document document) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.toString();
    }


    public boolean verify(String signedXml) {
        try {
            Document document = parseXml(signedXml);

            NodeList signatureNodes = document.getElementsByTagNameNS(
                    XMLSignature.XMLNS,
                    "Signature"
            );

            if (signatureNodes.getLength() == 0) {
                throw new IllegalStateException("No se encontro ds:Signature en el XML");
            }

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

            DOMValidateContext validateContext = new DOMValidateContext(
                    new X509KeySelector(),
                    signatureNodes.item(0)
            );

            XMLSignature signature = factory.unmarshalXMLSignature(validateContext);

            boolean coreValid = signature.validate(validateContext);
            boolean signatureValueValid = signature.getSignatureValue().validate(validateContext);
            boolean referenceValid = signature.getSignedInfo().getReferences().get(0).validate(validateContext);

            System.out.println("coreValid = " + coreValid);
            System.out.println("signatureValueValid = " + signatureValueValid);
            System.out.println("referenceValid = " + referenceValid);

            return coreValid;

        } catch (Exception e) {
            throw new IllegalStateException("Error validando firma XML", e);
        }
    }
}