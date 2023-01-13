package org.lean.rest.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.metadata.api.HopMetadata;
import org.apache.hop.metadata.api.IHopMetadata;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.api.IHopMetadataSerializer;
import org.lean.presentation.LeanPresentation;
import org.lean.rest.LeanUtil;
import org.lean.rest.resources.responses.PresentationResponse;

@Path("metadata/")
public class MetadataResource {

  private final LeanUtil leanUtil = LeanUtil.getInstance();

  /**
   * List all the type keys
   *
   * @return A list with all the type keys in the metadata
   */
  @GET
  @Path("/types")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTypes() {
    List<String> types = new ArrayList<>();
    IHopMetadataProvider provider = leanUtil.getMetadataProvider();
    List<Class<IHopMetadata>> metadataClasses = provider.getMetadataClasses();
    for (Class<IHopMetadata> metadataClass : metadataClasses) {
      HopMetadata metadata = metadataClass.getAnnotation(HopMetadata.class);
      types.add(metadata.key());
    }
    return Response.ok(types).build();
  }

  /**
   * List all the element names for a given type
   *
   * @param key the metadata key to use
   * @return A list with all the metadata element names
   * @throws HopException
   */
  @GET
  @Path("/list/{key}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listNames(@PathParam("key") String key) throws HopException {
    IHopMetadataProvider provider = leanUtil.getMetadataProvider();
    Class<IHopMetadata> metadataClass = provider.getMetadataClassForKey(key);
    IHopMetadataSerializer<IHopMetadata> serializer = provider.getSerializer(metadataClass);
    return Response.ok(serializer.listObjectNames()).build();
  }

  /**
   * Get a metadata element with a given type and name
   *
   * @param key The key of the metadata type
   * @param name The name to look up
   * @return The metadata element
   * @throws HopException
   */
  @GET
  @Path("/{key}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getElement(@PathParam("key") String key, @PathParam("name") String name)
      throws HopException {
    IHopMetadataProvider provider = leanUtil.getMetadataProvider();
    Class<IHopMetadata> metadataClass = provider.getMetadataClassForKey(key);
    IHopMetadataSerializer<IHopMetadata> serializer = provider.getSerializer(metadataClass);
    IHopMetadata metadata = serializer.load(name);
    return Response.ok().entity(metadata).build();
  }

  /**
   * Save a metadata element
   *
   * @param key
   * @param metadata
   * @return
   * @throws HopException
   */
  @POST
  @Path("/{key}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response saveElement(@PathParam("key") String key, IHopMetadata metadata)
      throws HopException {
    IHopMetadataProvider provider = leanUtil.getMetadataProvider();
    Class<IHopMetadata> metadataClass = provider.getMetadataClassForKey(key);
    IHopMetadataSerializer<IHopMetadata> serializer = provider.getSerializer(metadataClass);
    serializer.save(metadata);
    return Response.ok().entity(metadata.getName()).build();
  }

  /**
   * List presentation details.
   *
   * @return
   * @throws HopException
   */
  @GET
  @Path("/presentations/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listPresentations() throws HopException {
    IHopMetadataProvider provider = leanUtil.getMetadataProvider();
    IHopMetadataSerializer<LeanPresentation> serializer =
        provider.getSerializer(LeanPresentation.class);
    List<PresentationResponse> list = new ArrayList<>();
    List<String> names = serializer.listObjectNames();
    for (String name : names) {
      LeanPresentation presentation = serializer.load(name);
      list.add(new PresentationResponse(presentation.getName(), presentation.getDescription()));
    }
    try {
      String json = new ObjectMapper().writeValueAsString(list);
      return Response.ok()
          .entity(json)
          .type(MediaType.APPLICATION_JSON_TYPE)
          .encoding("UTF-8")
          .build();
    } catch (Exception e) {
      return Response.serverError()
          .entity("Error listing presentation metadata details " + Const.getStackTracker(e))
          .type(MediaType.TEXT_PLAIN)
          .encoding("UTF-8")
          .build();
    }
  }
}
