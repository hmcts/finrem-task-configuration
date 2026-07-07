package uk.gov.hmcts.reform.finrem.taskconfiguration.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;

public final class DmnXmlHelper {

    private DmnXmlHelper() {
    }

    public static Set<String> extractOutputColumn(String dmnFile, String columnName) throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(dmnFile)) {
            Document doc = parse(is);
            int index = findOutputColumnIndex(doc, columnName, dmnFile);
            Set<String> values = new LinkedHashSet<>();
            NodeList rules = doc.getElementsByTagName("rule");
            for (int r = 0; r < rules.getLength(); r++) {
                String v = outputEntryText(rules.item(r), index);
                if (v != null) {
                    values.add(v);
                }
            }
            return values;
        }
    }

    public static Map<String, String> extractOutputColumnPair(String dmnFile,
                                                              String keyColumn,
                                                              String valueColumn) throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(dmnFile)) {
            Document doc = parse(is);
            int keyIdx = findOutputColumnIndex(doc, keyColumn, dmnFile);
            int valIdx = findOutputColumnIndex(doc, valueColumn, dmnFile);
            Map<String, String> pairs = new LinkedHashMap<>();
            NodeList rules = doc.getElementsByTagName("rule");
            for (int r = 0; r < rules.getLength(); r++) {
                String k = outputEntryText(rules.item(r), keyIdx);
                if (k != null) {
                    String v = outputEntryText(rules.item(r), valIdx);
                    pairs.put(k, v != null ? v : "");
                }
            }
            return pairs;
        }
    }

    public static Set<String> extractInputColumn(String dmnFile, int colIndex) throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(dmnFile)) {
            Document doc = parse(is);
            Set<String> values = new LinkedHashSet<>();
            NodeList rules = doc.getElementsByTagName("rule");
            for (int r = 0; r < rules.getLength(); r++) {
                String v = inputEntryText(rules.item(r), colIndex);
                if (v != null) {
                    values.add(v);
                }
            }
            return values;
        }
    }

    public static int findOutputColumnIndex(Document doc, String columnName, String dmnFile) {
        NodeList outputs = doc.getElementsByTagName("output");
        for (int i = 0; i < outputs.getLength(); i++) {
            Node nameAttr = outputs.item(i).getAttributes().getNamedItem("name");
            if (nameAttr != null && columnName.equals(nameAttr.getNodeValue())) {
                return i;
            }
        }
        throw new IllegalStateException(
            "Output column '" + columnName + "' not found in " + dmnFile);
    }

    private static String outputEntryText(Node rule, int targetIndex) {
        return nthChildText(rule, "outputEntry", targetIndex);
    }

    private static String inputEntryText(Node rule, int targetIndex) {
        return nthChildText(rule, "inputEntry", targetIndex);
    }

    private static String nthChildText(Node parent, String tagName, int targetIndex) {
        int count = 0;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (tagName.equals(child.getNodeName())) {
                if (count == targetIndex) {
                    NodeList grandchildren = child.getChildNodes();
                    for (int t = 0; t < grandchildren.getLength(); t++) {
                        if ("text".equals(grandchildren.item(t).getNodeName())) {
                            String raw = grandchildren.item(t).getTextContent().trim();
                            if (!raw.isEmpty()) {
                                return raw.replaceAll("^\"|\"$", "");
                            }
                        }
                    }
                    return null;
                }
                count++;
            }
        }
        return null;
    }

    private static Document parse(InputStream is) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
    }
}
