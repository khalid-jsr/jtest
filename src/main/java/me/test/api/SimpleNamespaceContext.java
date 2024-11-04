package me.test.api;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SimpleNamespaceContext implements NamespaceContext {
    private final Map<String, String> namespaces;

    public void addNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    public SimpleNamespaceContext() {
        this.namespaces = new HashMap<>();
    }

    public SimpleNamespaceContext(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return namespaces.getOrDefault(prefix, null);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            if (entry.getValue().equals(namespaceURI)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return namespaces.keySet().iterator();
    }
}