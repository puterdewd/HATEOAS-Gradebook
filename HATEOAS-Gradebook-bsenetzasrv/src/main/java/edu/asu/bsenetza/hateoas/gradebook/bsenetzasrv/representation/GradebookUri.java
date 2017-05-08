package edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation;

import java.net.URI;
import java.net.URISyntaxException;

public class GradebookUri {

    private URI uri;

    public GradebookUri(String uri) {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public GradebookUri(URI uri) {
        this(uri.toString());
    }

    public GradebookUri(URI uri, int identifier) {
        this(uri.toString() + "/" + String.valueOf(identifier));
    }

    public GradebookUri(URI uri, String path) {
        this(uri.toString() + "/" + path);
    }

    public String getId() {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }

    public URI getFullUri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GradebookUri) {
            return ((GradebookUri) obj).uri.equals(uri);
        }
        return false;
    }

    public String getBaseUri() {
        /* // Old implementation
        String port = "";
        if(uri.getPort() != 80 && uri.getPort() != -1) {
            port = ":" + String.valueOf(uri.getPort());
        }
        
        return uri.getScheme() + "://" + uri.getHost() + port;
        * */

        String uriString = uri.toString();
        String baseURI = uriString.substring(0, uriString.lastIndexOf("webresources/") + "webresources".length());

        return baseURI;
    }
}
