package com.lxq.train.generator.server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestServer {
    static String toPath = "generator/src/main/java/com/lxq/train/generator/test/";
    static String pomPath = "generator/pom.xml";
    static{
        new File(toPath).mkdirs();
    }

    public static void main(String[] args) throws Exception {
        String generatorPath = generatorPath();

        Document document = new SAXReader().read("generator/"+ generatorPath);
        Node table = document.selectSingleNode("//table");
        System.out.println("table = " + table);
        Node tableName = table.selectSingleNode("@tableName");
        System.out.println("tableName = " + tableName.getText());
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println("domainObjectName = " + domainObjectName.getText());
    }

    private static String generatorPath() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Map<String, String> map = new HashMap<String, String>();
        map.put("pom", "http://maven.apache.org/POM/4.0.0");
        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
        Document document = saxReader.read(pomPath);
        Node node = document.selectSingleNode("//pom:configurationFile");
        System.out.println(node.getText());
        return node.getText();
    }
}
