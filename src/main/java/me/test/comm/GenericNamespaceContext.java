package me.test.comm;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class GenericNamespaceContext implements NamespaceContext {
    private final Map<String, String> prefixMap = new HashMap<>();

    public void addNamespace(String prefix, String uri) {
        prefixMap.put(prefix, uri);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            if (entry.getValue().equals(namespaceURI)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return prefixMap.keySet().iterator();
    }
}
