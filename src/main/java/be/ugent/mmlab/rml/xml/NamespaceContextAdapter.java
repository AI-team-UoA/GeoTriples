/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.mmlab.rml.xml;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathNSResolver;

/**
 *
 * @author mielvandersande
 */
public class NamespaceContextAdapter implements XPathNSResolver, NamespaceContext, PrefixResolver{
    
    private final NamespaceContext nscontext;

    public NamespaceContextAdapter(NamespaceContext nscontext) {
        this.nscontext = nscontext;
    }
    
    @Override
    public String lookupNamespaceURI(String prefix) {
        return nscontext.getNamespaceURI(prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return nscontext.getNamespaceURI(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return nscontext.getPrefix(namespaceURI);
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return nscontext.getPrefixes(namespaceURI);
    }

    @Override
    public String getNamespaceForPrefix(String prefix) {
        return nscontext.getNamespaceURI(prefix);
    }

    @Override
    public String getNamespaceForPrefix(String prefix, Node context) {
        return nscontext.getNamespaceURI(prefix);
    }

    @Override
    public String getBaseIdentifier() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean handlesNullPrefixes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
