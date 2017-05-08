package edu.asu.bsenetza.hateoas.gradebook.bsenetzacli;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation.Representation;
import static edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation.Representation.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gradebook_HATEOAS_cl {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final WebResource webResource;
    private final Client client;
    private static final String BASE_URI = "http://localhost:8080/HATEOAS-Gradebook-bsenetzasrv/webresources";

    public Gradebook_HATEOAS_cl() {
        LOG.info("Creating a Gradebook REST client");

        ClientConfig config = new DefaultClientConfig();
        client = Client.create(config);
        webResource = client.resource(BASE_URI).path("gradebook/item");
        LOG.debug("webResource = {}", webResource.getURI());
    }

    public ClientResponse createGradedItem(Object requestEntity) throws UniformInterfaceException {
        LOG.info("Initiating a Create GradedItem request");
        LOG.debug("XML String = {}", (String) requestEntity);

        return webResource.type(GRADEBOOK_MEDIA_TYPE).post(ClientResponse.class, requestEntity);
    }

    public ClientResponse deleteGradedItem() throws UniformInterfaceException {
        LOG.info("Initiating a Delete request");
        LOG.debug("getGradedItemDeleteLink {}", getGradedItemDeleteLink());

        return client.resource(getGradedItemDeleteLink().getUri()).type(getGradedItemDeleteLink().getMediaType()).delete(ClientResponse.class);
    }

    public ClientResponse updateGradedItem(Object requestEntity) throws UniformInterfaceException {
        LOG.info("Initiating an Update request");
        LOG.debug("XML String = {}", (String) requestEntity);
        LOG.debug("getGradedItemUpdateLink {}", getGradedItemUpdateLink());

        return client.resource(getGradedItemUpdateLink().getUri()).type(getGradedItemUpdateLink().getMediaType()).accept(getGradedItemUpdateLink().getMediaType()).put(ClientResponse.class, requestEntity);
    }

    /**
     * get GradedItem using gradeItemId This is needed for initial requests so
     * as not to require a create first
     */
    public <T> T retrieveGradedItem(Class<T> responseType, String gradedItemId) throws UniformInterfaceException {
        LOG.info("Initiating a Retrieve request");
        LOG.debug("responseType = {}", responseType.getClass());
        LOG.debug("gradedItemId = {}", gradedItemId);

        return webResource.path(gradedItemId).accept(GRADEBOOK_MEDIA_TYPE).get(responseType);
    }

    public <T> T retrieveGradedItem(Class<T> responseType) throws UniformInterfaceException {
        LOG.info("Initiating a Retrieve request");
        LOG.debug("responseType = {}", responseType.getClass());
        LOG.debug("getSelfLink {}", getSelfLink());

        return client.resource(getSelfLink().getUri()).accept(getSelfLink().getMediaType()).get(responseType);
    }

    public ClientResponse createGrade(Object requestEntity, String studentId) throws UniformInterfaceException {
        LOG.info("Initiating a Create Grade request");
        LOG.debug("XML String = {}", (String) requestEntity);
        LOG.debug("getGradeCreateLink {}", getGradeCreateLink());
        URI locationUri = URI.create(getGradeCreateLink().getUri().toString() + "/" + studentId);
        LOG.debug(locationUri.toString());

        return client.resource(locationUri).accept(getGradeCreateLink().getMediaType()).type(getGradeCreateLink().getMediaType()).post(ClientResponse.class, requestEntity);
    }

    public <T> T retrieveGrade(Class<T> responseType, String studentId) throws UniformInterfaceException {
        LOG.info("Initiating a Retrieve Grade request");
        LOG.debug("responseType = {}", responseType.getClass());
        LOG.debug("getGradeSelfLink {}", getGradeSelfLink(studentId));
        return client.resource(getGradeSelfLink(studentId).getUri()).accept(getGradeSelfLink(studentId).getMediaType()).get(responseType);
    }

    public ClientResponse updateGrade(Object requestEntity) throws UniformInterfaceException {
        LOG.info("Initiating an Update Grade request");
        LOG.debug("XML String = {}", (String) requestEntity);

        return client.resource(getGradeUpdateLink().getUri()).type(getGradeUpdateLink().getMediaType()).accept(getGradeUpdateLink().getMediaType()).put(ClientResponse.class, requestEntity);
    }

    public ClientResponse deleteGrade() throws UniformInterfaceException {
        LOG.info("Initiating a Delete Grade request");
        return client.resource(getGradeDeleteLink().getUri()).type(getGradeDeleteLink().getMediaType()).delete(ClientResponse.class);

    }

    public void close() {
        LOG.info("Closing the REST Client");
        client.destroy();
    }
}
