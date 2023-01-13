package org.lean.rest.resources.responses;

public class PresentationResponse {
  private String name;
  private String description;

  public PresentationResponse() {}

  public PresentationResponse(String name, String description) {
    this.name = name;
    this.description = description;
  }

  /**
   * Gets name
   *
   * @return value of name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name
   *
   * @param name value of name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets description
   *
   * @return value of description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets description
   *
   * @param description value of description
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
