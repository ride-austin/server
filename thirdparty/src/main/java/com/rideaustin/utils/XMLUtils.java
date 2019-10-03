package com.rideaustin.utils;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.rideaustin.rest.exception.ServerError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XMLUtils {

  private XMLUtils() {

  }

  public static String getXMLNode(String xmlData, String path) throws ServerError {
    Document document = createDocument(xmlData);
    return firstChild(document, path);
  }

  private static String firstChild(Document document, String path) throws ServerError {
    if (document == null) {
      return null;
    }
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      NodeList nodes = (NodeList) xPath.evaluate(path, document.getDocumentElement(), XPathConstants.NODESET);
      if (nodes.getLength() > 0) {
        return nodes.item(0).getTextContent();
      }
    } catch (XPathExpressionException e) {
      throw new ServerError(e);
    }
    return null;
  }

  private static Document createDocument(String data) throws ServerError {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(data));
      return builder.parse(is);
    } catch (Exception e) {
      log.error("Error while parsing xml", e);
      throw new ServerError("Error while parsing xml", e);
    }
  }
}
