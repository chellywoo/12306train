package com.lxq.train.generator.server;

import com.lxq.train.generator.util.DbUtil;
import com.lxq.train.generator.util.Field;
import com.lxq.train.generator.util.FreemarkerUtil;
import freemarker.template.TemplateException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerGenerator {
    static boolean readOnly = true;
    static String vuePath = "admin/src/views/main/";
    static String serverPath = "[module]/src/main/java/com/lxq/train/[module]/";
    static String pomPath = "generator/pom.xml";

    static String module = "";

    public static void main(String[] args) throws Exception {
        String generatorPath = generatorPath();

        module = generatorPath.replace("src/main/resources/generator-config-", "").replace(".xml", "");
        System.out.println("module = " + module);
        serverPath = serverPath.replace("[module]",module);
        System.out.println("servicePath = " + serverPath);
        
        Document document = new SAXReader().read("generator/"+ generatorPath);
        Node table = document.selectSingleNode("//table");
        System.out.println("table = " + table);
        Node tableName = table.selectSingleNode("@tableName");
        System.out.println("tableName = " + tableName.getText());
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println("domainObjectName = " + domainObjectName.getText());

        Node connectUrl = document.selectSingleNode("//@connectionURL");
        Node userId = document.selectSingleNode("//@userId");
        Node password = document.selectSingleNode("//@password");
        System.out.println("connectUrl = " + connectUrl.getText());
        System.out.println("userId = " + userId.getText());
        System.out.println("password = " + password.getText());

        DbUtil.url = connectUrl.getText();
        DbUtil.user = userId.getText();
        DbUtil.password = password.getText();

//        DbUtil.getConnection();

        String Domain = domainObjectName.getText();
        String domain = Domain.substring(0,1).toLowerCase() + Domain.substring(1);
        String do_main = tableName.getText().replaceAll("_", "-");

        String tableNameCn = DbUtil.getTableComment(tableName.getText());
        List<Field> fieldList = DbUtil.getColumnByTableName(tableName.getText());
        Set<String> typeSet = getJavaTypes(fieldList);//

        Map<String, Object> param = new HashMap<>();
        param.put("module",module);
        param.put("Domain",Domain);
        param.put("domain",domain);
        param.put("do_main",do_main);
        param.put("tableNameCn",tableNameCn);
        param.put("fieldList",fieldList);
        param.put("typeSet", typeSet);
        param.put("readOnly", readOnly);
        System.out.println("组装参数 = " + param);

        generateFile(Domain, param, "service","service");
        generateFile(Domain, param, "controller/admin","adminController");
        generateFile(Domain, param, "req", "saveReq");
        generateFile(Domain, param, "req","queryReq");
        generateFile(Domain, param, "resp","queryResp");

        generateVue(do_main, param);

    }

    private static void generateFile(String Domain, Map<String, Object> param, String packageName, String target) throws IOException, TemplateException {
        FreemarkerUtil.initConfig(target + ".ftl");
        String toPath = serverPath + packageName + "/";
        new File(toPath).mkdirs();
        String Target = target.substring(0,1).toUpperCase() + target.substring(1);
        String filename = toPath + Domain + Target + ".java";
        System.out.println("开始生成" + filename);
        FreemarkerUtil.generator(filename, param);
    }

    private static void generateVue(String do_main, Map<String, Object> param) throws IOException, TemplateException {
        FreemarkerUtil.initConfig("adminVue.ftl");
        new File(vuePath + module).mkdirs();
        String filename = vuePath + module +  "/" + do_main + ".vue";
        System.out.println("开始生成" + filename);
        FreemarkerUtil.generator(filename, param);
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

    /***
     * 将fieldList中的得到的java类型进行去重
     */
    private static Set<String> getJavaTypes(List<Field> fieldList){
        Set<String> javaType = new HashSet<>();
        for (Field i : fieldList) {
            javaType.add(i.getJavaType());
        }
        return javaType;
    }
}
