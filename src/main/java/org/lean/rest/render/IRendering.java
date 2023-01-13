package org.lean.rest.render;

import java.util.Date;
import java.util.List;
import org.lean.presentation.LeanPresentation;
import org.lean.presentation.layout.LeanLayoutResults;
import org.lean.presentation.variable.LeanParameter;

/**
 * A rendering is given a unique ID so that we can interrogate the rendering with web services. We
 * can cache the rendering this way, keep it around for a bit and so on.
 */
public interface IRendering {
  String getId();

  LeanPresentation getPresentation();

  String getPresentationName();

  Date getRenderDate();

  List<LeanParameter> getParameters();

  LeanLayoutResults getLayoutResults();
}
