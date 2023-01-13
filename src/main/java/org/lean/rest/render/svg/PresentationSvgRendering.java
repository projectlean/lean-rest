package org.lean.rest.render.svg;

import java.util.List;
import org.lean.presentation.LeanPresentation;
import org.lean.presentation.variable.LeanParameter;
import org.lean.rest.render.Rendering;

public class PresentationSvgRendering extends Rendering {
  private LeanPresentation presentation;

  public PresentationSvgRendering() {
    super();
  }

  public PresentationSvgRendering(LeanPresentation presentation, List<LeanParameter> parameters) {
    this();
    this.presentation = presentation;
    this.parameters = parameters;
  }

  /**
   * Gets presentation
   *
   * @return value of presentation
   */
  public LeanPresentation getPresentation() {
    return presentation;
  }

  /**
   * Sets presentation
   *
   * @param presentation value of presentation
   */
  public void setPresentation(LeanPresentation presentation) {
    this.presentation = presentation;
  }
}
