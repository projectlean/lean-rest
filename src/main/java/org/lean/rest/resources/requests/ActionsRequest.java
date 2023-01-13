package org.lean.rest.resources.requests;

/** The vectors for an actions request in the Render resource. */
public class ActionsRequest {
  private String renderId;
  private int pageNumber;
  private int x;
  private int y;

  public ActionsRequest() {}

  /**
   * Gets renderId
   *
   * @return value of renderId
   */
  public String getRenderId() {
    return renderId;
  }

  /**
   * Sets renderId
   *
   * @param renderId value of renderId
   */
  public void setRenderId(String renderId) {
    this.renderId = renderId;
  }

  /**
   * Gets pageNumber
   *
   * @return value of pageNumber
   */
  public int getPageNumber() {
    return pageNumber;
  }

  /**
   * Sets pageNumber
   *
   * @param pageNumber value of pageNumber
   */
  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  /**
   * Gets x
   *
   * @return value of x
   */
  public int getX() {
    return x;
  }

  /**
   * Sets x
   *
   * @param x value of x
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Gets y
   *
   * @return value of y
   */
  public int getY() {
    return y;
  }

  /**
   * Sets y
   *
   * @param y value of y
   */
  public void setY(int y) {
    this.y = y;
  }
}
