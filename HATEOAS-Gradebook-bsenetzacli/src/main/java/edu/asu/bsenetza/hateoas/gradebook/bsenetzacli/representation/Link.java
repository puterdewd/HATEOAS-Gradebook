package edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation;

import static edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation.Representation.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(namespace = DAP_NAMESPACE)
public class Link {
    
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    @XmlAttribute(namespace = DAP_NAMESPACE)
    private String rel;
    @XmlAttribute(namespace = DAP_NAMESPACE)
    private String uri;

    @XmlAttribute(namespace = DAP_NAMESPACE)
    private String mediaType;

    /**
     * For JAXB :-(
     */
    Link() {
        LOG.info("Link Constructor");
    }

    public Link(String name, GradebookUri uri, String mediaType) {
        LOG.info("Creating a Link object");
        LOG.debug("name = {}", name);
        LOG.debug("uri = {}", uri);
        LOG.debug("mediaType = {}", mediaType);
        
        this.rel = name;
        this.uri = uri.getFullUri().toString();
        this.mediaType = mediaType;

        LOG.debug("Created Link Object {}", this);
    }

    public Link(String name, GradebookUri uri) {
        this(name, uri, GRADEBOOK_MEDIA_TYPE);
    }

    public String getRelValue() {
        return rel;
    }

    public URI getUri() {
        
        try {
            URI local_uri = new URI(uri);
            return local_uri;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMediaType() {
        return mediaType;
    }
}
