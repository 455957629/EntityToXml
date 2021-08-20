package io.npl.common.utils.xml;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;

/**
*
* @auther li m c
**/
public class EntityToXml {

    static Logger logger = LoggerFactory.getLogger(EntityToXml.class);
    private Object object;
    private Element rootElement;
    private Element currentElement;
    private Document document;

    public static class Builder {
        private Object object;
        private Element rootElement;
        private Element currentElement;
        private Document document;

        public Builder(Document doc, String eleName, Map<String,String> attrs){
            try {
                if(doc == null){
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    document = builder.newDocument();
                    document.setXmlStandalone(true);
                }else{
                    document = doc;
                }
                Element element = document.createElement(eleName);
                if(attrs != null){
                    for(Map.Entry<String, String> item : attrs.entrySet()){
                        element.setAttribute(item.getKey(), item.getValue());
                    }
                }
                if(rootElement == null) {
                    document.appendChild(element);
                    rootElement = element;
                }
                currentElement = element;
            }catch (Exception e) {
                logger.error("初始化Xml失败");
            }
        }
        public EntityToXml build() {
            return new EntityToXml(document, rootElement, currentElement);
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public Element getRootElement() {
            return rootElement;
        }

        public void setRootElement(Element rootElement) {
            this.rootElement = rootElement;
        }

        public Element getCurrentElement() {
            return currentElement;
        }

        public void setCurrentElement(Element currentElement) {
            this.currentElement = currentElement;
        }

        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Element getRootElement() {
        return rootElement;
    }

    public void setRootElement(Element rootElement) {
        this.rootElement = rootElement;
    }

    public Element getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(Element currentElement) {
        this.currentElement = currentElement;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public EntityToXml(Document document,Element rootElement, Element currentElement) {
        this.document = document;
        this.rootElement = rootElement;
        this.currentElement = currentElement;
    }

    /**
     * 	节点设置内容
     * @param element 节点
     * @param content	节点内容
     */
    public void setTextContent(Element element, String content) {
        if(null != content)	element.setTextContent(content);
    }

    /**
     * 	往指定节点添加子节点，记录当前节点
     * @param subEname
     * @param content
     * @return
     */
    public void putInner(Document doc, Element parentElement, String subEname, String content, Map<String, String> attrs) {
        Element subElement = doc.createElement(subEname);
        if(attrs != null){
            for(Map.Entry<String, String> attr : attrs.entrySet()){
                subElement.setAttribute(attr.getKey(), attr.getValue());
            }
        }
        subElement.setTextContent(content);
        parentElement.appendChild(subElement);
    }

    /**
     * 生成xml文件
     * @param path
     * @param doc
     * @throws Exception
     */
    public static void saveXmlFile(String path, Document doc) throws Exception {
        try {
            DOMSource domSource = new DOMSource(doc);
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(domSource, new StreamResult(new File(path)));
        }catch (Exception e) {
            logger.error("保存xml文件异常", e);
            throw e;
        }
    }

    private static String getMethodName(String fildeName){
        byte[] items = fildeName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }

    /**
     * 生成单层xml
     * @param object 单层属性的对象
     * @param parentElement 根节点
     * @param nodeName 如果为空则默认上一个节点，不为空则在上一个节点下创建新节点
     * @return
     */

    public EntityToXml beanToXml(EntityToXml tools, Object object, Element parentElement, String nodeName){
        Element currentElement = parentElement;
        if(StringUtils.isNotBlank(nodeName)){
            Element subElement = tools.getDocument().createElement(nodeName);
            parentElement.appendChild(subElement);
            currentElement = subElement;
            tools.setCurrentElement(currentElement);
        }
        if(object != null){
            Field[] fields = object.getClass().getDeclaredFields();
            for(Field field : fields){
                try {
                    if(!field.isAccessible()){
                        field.setAccessible(true);
                    }
                    Type t = field.getGenericType();
                    Method m = object.getClass().getMethod("get" + getMethodName(field.getName()));
                    if(List.class.isAssignableFrom(field.getType())){
                        this.beanToXml(tools,null , tools.getCurrentElement(), field.getName());
                        Element returnTarget = tools.getCurrentElement();
                        logger.info("{}", returnTarget);
                        if (t instanceof ParameterizedType) {
                            List<Object> list = (List)m.invoke(object);
                            Class clasz = list.get(0).getClass();
                            if(clasz.getTypeName().startsWith("java.lang")){
                                for(Object item : list){
                                    this.putInner(tools.getDocument(), returnTarget, item.getClass().getName(), item.toString().trim(), null);
                                }
                            }else{
                                for(Object item : list){
                                    String c = item.getClass().getSimpleName().toLowerCase(Locale.ROOT);
                                    this.beanToXml(tools, item, returnTarget, c);
                                }
                                tools.setCurrentElement(returnTarget);
                            }
                        }
                    }else {
                        this.putInner(tools.getDocument(), currentElement, field.getName(), m.invoke(object).toString().trim(), null);
                    }
                } catch (IllegalAccessException e) {
                    logger.error("beanToXml error", e);
                } catch (InvocationTargetException e) {
                    logger.error("beanToXml error", e);
                } catch (NoSuchMethodException e) {
                    logger.error("beanToXml error", e);
                }
            }
        }
        return tools;
    }

    @Override
    public String toString() {
        String xmlString = "";
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            DOMSource domSource = new DOMSource(document);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            transformer.transform(domSource, new StreamResult(bos));
            xmlString = bos.toString();
        }catch (Exception e) {
            logger.error("xml toString error",e.getMessage());
        }
        return xmlString;
    }

    public static void main(String[] args) throws Exception {

        class TestItem{
            String item;

            public String getItem() {
                return item;
            }

            public void setItem(String item) {
                this.item = item;
            }
        }
        class TestList {
            Integer dns;
            String result;
            List<TestItem> items;

            public Integer getDns() {
                return dns;
            }

            public void setDns(Integer dns) {
                this.dns = dns;
            }

            public String getResult() {
                return result;
            }

            public void setResult(String result) {
                this.result = result;
            }

            public List<TestItem> getItems() {
                return items;
            }

            public void setItems(List<TestItem> items) {
                this.items = items;
            }
        }

        class TestVo{
            Integer id;
            List<TestList> strs;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public List<TestList> getStrs() {
                return strs;
            }

            public void setStrs(List<TestList> strs) {
                this.strs = strs;
            }

        }
        TestItem item1 = new TestItem();
        item1.setItem("item1");
        TestItem item2 = new TestItem();
        item2.setItem("item2");
        List<TestItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        TestVo test = new TestVo();
        test.setId(100);
        TestList list1 = new TestList();
        list1.setDns(1);
        list1.setResult("dns 1");
        list1.setItems(items);
        TestList list2 = new TestList();
        list2.setDns(2);
        list2.setResult("dns 2");
        List<TestList> list = new ArrayList<>();
        list.add(list1);
        list.add(list2);
        list2.setItems(items);
        test.setStrs(list);
        Map<String, String> map = new HashMap<>();
        map.put("account","develop");
        map.put("billtype","vouchergl");
        map.put("businessunitcode","develop");
        map.put("filename","");
        map.put("groupcode","");
        map.put("isexchange","");
        map.put("orgcode","");
        map.put("receiver","0001121000000000JIYO");
        map.put("replace","develop");
        map.put("roottag","develop");
        map.put("sender","001");
        EntityToXml toXml = new EntityToXml.Builder(null,"ufinterface", map ).build();
        toXml = toXml.beanToXml(toXml, null, toXml.currentElement, "voucher");
        toXml = toXml.beanToXml(toXml, test, toXml.currentElement, "voucher_head");
        logger.info("{}", toXml);
    }


}
