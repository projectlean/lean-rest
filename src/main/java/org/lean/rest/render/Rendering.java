package org.lean.rest.render;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.lean.core.LeanSize;
import org.lean.presentation.LeanPresentation;
import org.lean.presentation.layout.LeanLayoutResults;
import org.lean.presentation.variable.LeanParameter;

public class Rendering implements IRendering {
  protected String id;
  protected LeanPresentation presentation;
  protected String presentationName;
  protected Date renderDate;
  protected List<LeanParameter> parameters;
  protected LeanLayoutResults layoutResults;

  protected Rendering() {
    this.id = UUID.randomUUID().toString();
    this.renderDate = new Date();
    this.parameters = new ArrayList<>();
  }

  /**
   * Gets id
   *
   * @return value of id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets id
   *
   * @param id value of id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets presentation
   *
   * @return value of presentation
   */
  @Override
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

  /**
   * Gets presentationName
   *
   * @return value of presentationName
   */
  @Override
  public String getPresentationName() {
    return presentationName;
  }

  /**
   * Sets presentationName
   *
   * @param presentationName value of presentationName
   */
  public void setPresentationName(String presentationName) {
    this.presentationName = presentationName;
  }

  /**
   * Gets renderDate
   *
   * @return value of renderDate
   */
  public Date getRenderDate() {
    return renderDate;
  }

  /**
   * Sets renderDate
   *
   * @param renderDate value of renderDate
   */
  public void setRenderDate(Date renderDate) {
    this.renderDate = renderDate;
  }

  /**
   * Gets parameters
   *
   * @return value of parameters
   */
  public List<LeanParameter> getParameters() {
    return parameters;
  }

  /**
   * Sets parameters
   *
   * @param parameters value of parameters
   */
  public void setParameters(List<LeanParameter> parameters) {
    this.parameters = parameters;
  }

  /**
   * Gets layoutResults
   *
   * @return value of layoutResults
   */
  public LeanLayoutResults getLayoutResults() {
    return layoutResults;
  }

  /**
   * Sets layoutResults
   *
   * @param layoutResults value of layoutResults
   */
  public void setLayoutResults(LeanLayoutResults layoutResults) {
    this.layoutResults = layoutResults;
  }
}
