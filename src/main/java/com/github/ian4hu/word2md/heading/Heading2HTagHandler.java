package com.github.ian4hu.word2md.heading;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.SdtTagHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtPr;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.Optional;

public class Heading2HTagHandler extends SdtTagHandler {
    @Override
    public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr, HashMap<String, String> tagMap, NodeIterator childResults) throws TransformerException {
        try {
            // Create a DOM builder and parse the fragment
            Document document = XmlUtils.getNewDocumentBuilder().newDocument();
            DocumentFragment docfrag = document.createDocumentFragment();

            attachContents(docfrag, docfrag, childResults);

            String outlineLevel = tagMap.get("OUTLINE_LEVEL");
            if (outlineLevel == null) {
                return docfrag;
            }

            return processOutline(outlineLevel, docfrag, document);
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }

    private Node processOutline(String outlineLevel, DocumentFragment docfrag, Document document) {
        Element element = document.createElement("h" + outlineLevel);
        NodeList childNodes = docfrag.getChildNodes();
        Node src = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                src = item;
                break;
            }
        }
        if (src == null) {
            return docfrag;
        }
        XmlUtils.treeCopy(src.getChildNodes(), element);
        NamedNodeMap attributes = src.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            element.setAttribute(item.getNodeName(), item.getNodeValue());
        }
        StringBuilder cls = Optional.ofNullable(element.getAttribute("class"))
                .map(StringBuilder::new)
                .orElseGet(StringBuilder::new);
        cls.append("outline-heading-").append(outlineLevel).append(" outline-heading ");
        element.setAttribute("class", cls.toString());
        DocumentFragment fragment = document.createDocumentFragment();
        fragment.appendChild(element);
        return fragment;
    }

    @Override
    public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr, HashMap<String, String> tagMap, Node resultSoFar) throws TransformerException {
        try {
            // Create a DOM builder and parse the fragment
            Document document = XmlUtils.getNewDocumentBuilder().newDocument();
            DocumentFragment docfrag = document.createDocumentFragment();

            attachContents(docfrag, docfrag, resultSoFar);

            String outlineLevel = tagMap.get("OUTLINE_LEVEL");
            if (outlineLevel == null) {
                return docfrag;
            }

            return processOutline(outlineLevel, docfrag, document);
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }
}
