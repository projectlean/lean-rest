package org.lean.rest.resources;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.hop.core.Const;
import org.lean.rest.LeanUtil;

public abstract class BaseResource {
  protected final LeanUtil leanUtil = LeanUtil.getInstance();

  protected Response getServerError(String errorMessage) {
    return getServerError(errorMessage, null, true);
  }

  protected Response getServerError(String errorMessage, boolean logOnServer) {
    return getServerError(errorMessage, null, logOnServer);
  }

  protected Response getServerError(String errorMessage, Exception e) {
    return getServerError(errorMessage, e, true);
  }

  protected Response getServerError(String errorMessage, Exception e, boolean logOnServer) {
    if (logOnServer) {
      if (e != null) {
        leanUtil.getLog().logError(errorMessage, e);
      } else {
        leanUtil.getLog().logError(errorMessage);
      }
    }
    return Response.serverError()
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(errorMessage + (e == null ? "" : ("\n" + Const.getSimpleStackTrace(e))))
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
