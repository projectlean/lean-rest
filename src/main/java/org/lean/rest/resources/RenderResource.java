package org.lean.rest.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import org.apache.hop.core.Const;
import org.lean.core.draw.DrawnItem;
import org.lean.presentation.LeanPresentation;
import org.lean.presentation.interaction.LeanInteraction;
import org.lean.presentation.layout.LeanRenderPage;
import org.lean.rest.interaction.InteractionLookupResult;
import org.lean.rest.render.IRendering;
import org.lean.rest.render.RenderFactory;
import org.lean.rest.resources.requests.ActionsRequest;

@Path("render/")
public class RenderResource extends BaseResource {
  /**
   * Conveniently renders a main presentation
   *
   * @return
   */
  @GET
  @Path("/main/")
  public Response getMainPage() {
    try {
      return RenderFactory.getMainPage(this);
    } catch (Exception e) {
      String errorMessage = "Unexpected error retrieving the main page";
      return getServerError(errorMessage, e);
    }
  }

  /**
   * Render returns a unique ID to a rendering. The rendering contains the layout results, the SVG,
   * the drawn areas and everything else that is needed to visualize a presentation for the
   * requested render type.
   *
   * @param presentationName The name of the presentation to render
   * @return The response in the form of the UUID of the rendering.
   */
  @GET
  @Path("/presentation/{name}/")
  public Response renderPresentation(@PathParam("name") String presentationName) {
    try {
      // Check the cache first. Render if needed.
      //
      IRendering rendering = leanUtil.findRendering(presentationName);
      if (rendering == null) {
        LeanPresentation presentation = leanUtil.loadPresentation(presentationName);
        rendering =
            RenderFactory.renderPresentation(
                leanUtil.getLoggingObject(),
                leanUtil.getMetadataProvider(),
                presentation,
                Collections.emptyList());

        leanUtil.storeRendering(rendering);
      }

      return Response.ok().entity(rendering.getId()).type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      String errorMessage = "Unexpected error rendering presentation '" + presentationName + "'";
      return getServerError(errorMessage, e);
    }
  }

  /**
   * Get the amount of rendered pages.
   *
   * @param renderId The rendering ID
   * @return
   */
  @GET
  @Path("/info/pages/{renderId}")
  public Response getPageCount(@PathParam("renderId") String renderId) {
    try {
      IRendering rendering = leanUtil.getRendering(renderId);
      if (rendering == null) {
        return getServerError("Unable to find rendering with ID " + renderId, false);
      }

      int pageCount = rendering.getLayoutResults().getRenderPages().size();

      return Response.ok().entity(pageCount).build();
    } catch (Exception e) {
      String errorMessage =
          "Unexpected error retrieving the number of pages for render ID " + renderId;
      return getServerError(errorMessage, e);
    }
  }

  /**
   * Expose a page of a rendering in a certain way
   *
   * @param renderId The rendering ID
   * @param renderType The type of rendering to do: SVG, HTML, PDF, ...
   * @param pageNumber The page number
   * @return
   */
  @GET
  @Path("/page/{renderId}/{renderType}/{pageNumber}/")
  public Response getRenderPageSvg(
      @PathParam("renderId") String renderId,
      @PathParam("renderType") String renderType,
      @PathParam("pageNumber") int pageNumber) {

    try {
      IRendering rendering = leanUtil.getRendering(renderId);
      if (rendering == null) {
        return getServerError("Unable to find rendering with ID " + renderId, false);
      }

      List<LeanRenderPage> renderPages = rendering.getLayoutResults().getRenderPages();
      if (pageNumber < 0 || pageNumber >= renderPages.size()) {
        return getServerError(
            "Invalid page number requested: "
                + pageNumber
                + ".  Available pages: "
                + renderPages.size(),
            false);
      }
      LeanRenderPage page = renderPages.get(pageNumber);

      return RenderFactory.renderPage(rendering, page, renderType);
    } catch (Exception e) {
      String errorMessage =
          "Unexpected error retrieving page " + pageNumber + " for render ID " + renderId;
      return getServerError(errorMessage, e);
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/lookupActions/")
  public Response lookupActions(String json) {
    try {
      ActionsRequest r = new ObjectMapper().readValue(json, ActionsRequest.class);

      IRendering rendering = leanUtil.getRendering(r.getRenderId());
      if (rendering == null) {
        return getServerError("Unable to find rendering with ID " + r.getRenderId(), false);
      }
      List<LeanRenderPage> renderPages = rendering.getLayoutResults().getRenderPages();
      if (r.getPageNumber() < 0 || r.getPageNumber() >= renderPages.size()) {
        return getServerError(
            "Invalid page number requested: "
                + r.getPageNumber()
                + ".  Available pages: "
                + renderPages.size(),
            false);
      }
      LeanRenderPage page = renderPages.get(r.getPageNumber());

      DrawnItem drawnItem = page.lookupDrawnItem(r.getX(), r.getY());
      InteractionLookupResult result = new InteractionLookupResult();
      if (drawnItem != null) {
        // See if we have an action to match this...
        //
        LeanPresentation presentation = rendering.getPresentation();
        LeanInteraction interaction = presentation.findInteraction(null, drawnItem);
        if (interaction != null) {
          if (leanUtil.getLog().isDetailed()) {
            leanUtil
                .getLog()
                .logDetailed("Interaction found for (" + r.getX() + "," + r.getY() + ")");
          }
          // We found an interaction for this drawn item...
          //
          result.setFound(true);
          result.setDrawnItem(drawnItem);
          result.setActions(interaction.getActions());
          result.setMethod(interaction.getMethod());
        }
      }

      return Response.ok()
          .entity(result.toJsonString())
          .encoding("UTF-8")
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      // Don't log on the server, it can be tedious to see all the failed lookups.
      //
      String errorMessage = "Unexpected error retrieving the possible actions for request: " + json;
      return Response.serverError()
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorMessage + "\n" + Const.getSimpleStackTrace(e))
          .type(MediaType.TEXT_PLAIN)
          .build();
    }
  }
}
