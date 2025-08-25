@jakarta.xml.bind.annotation.XmlSchema(
        xmlns = {
                @jakarta.xml.bind.annotation.XmlNs(prefix = "SOAP-ENV", namespaceURI = "http://schemas.xmlsoap.org/soap/envelope/"),
                @jakarta.xml.bind.annotation.XmlNs(prefix = "SOAP-ENC", namespaceURI = "http://schemas.xmlsoap.org/soap/encoding/"),
                @jakarta.xml.bind.annotation.XmlNs(prefix = "xsd", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
                @jakarta.xml.bind.annotation.XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @jakarta.xml.bind.annotation.XmlNs(prefix = "cwmp", namespaceURI = "urn:dslforum-org:cwmp-1-0")
        },
        elementFormDefault = XmlNsForm.QUALIFIED,
        attributeFormDefault =XmlNsForm.QUALIFIED
)
package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.XmlNsForm;