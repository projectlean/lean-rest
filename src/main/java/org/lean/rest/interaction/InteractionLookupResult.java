package org.lean.rest.interaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.lean.core.draw.DrawnItem;
import org.lean.presentation.interaction.LeanInteractionAction;
import org.lean.presentation.interaction.LeanInteractionMethod;

public class InteractionLookupResult {
  /** Was the lookup found? */
  private boolean found;

  /** The possible method that is required. */
  private LeanInteractionMethod method;

  /** The actions that need to happen */
  private List<LeanInteractionAction> actions;

  /** The drawn item that was found on a particular location */
  private DrawnItem drawnItem;

  public InteractionLookupResult() {
    this.actions = new ArrayList<>();
  }

  public String toJsonString() throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(this);
  }

  /**
   * Gets found
   *
   * @return value of found
   */
  public boolean isFound() {
    return found;
  }

  /**
   * Sets found
   *
   * @param found value of found
   */
  public void setFound(boolean found) {
    this.found = found;
  }

  /**
   * Gets method
   *
   * @return value of method
   */
  public LeanInteractionMethod getMethod() {
    return method;
  }

  /**
   * Sets method
   *
   * @param method value of method
   */
  public void setMethod(LeanInteractionMethod method) {
    this.method = method;
  }

  /**
   * Gets actions
   *
   * @return value of actions
   */
  public List<LeanInteractionAction> getActions() {
    return actions;
  }

  /**
   * Sets actions
   *
   * @param actions value of actions
   */
  public void setActions(List<LeanInteractionAction> actions) {
    this.actions = actions;
  }

  /**
   * Gets drawnItem
   *
   * @return value of drawnItem
   */
  public DrawnItem getDrawnItem() {
    return drawnItem;
  }

  /**
   * Sets drawnItem
   *
   * @param drawnItem value of drawnItem
   */
  public void setDrawnItem(DrawnItem drawnItem) {
    this.drawnItem = drawnItem;
  }
}
