package org.lean.rest.render;

import java.util.HashMap;
import java.util.Map;

public class RenderCache {
  private static RenderCache instance;

  private Map<String, IRendering> cache;

  /**
   * Gets instance
   *
   * @return value of instance
   */
  public static RenderCache getInstance() {
    if (instance == null) {
      instance = new RenderCache();
    }
    return instance;
  }

  private RenderCache() {
    this.cache = new HashMap<>();
  }

  public static IRendering getRendering(String id) {
    return getInstance().getCache().get(id);
  }

  public static void addRendering(IRendering rendering) {
    getInstance().getCache().put(rendering.getId(), rendering);
  }

  public static IRendering removeRendering(String id) {
    return getInstance().getCache().remove(id);
  }

  /**
   * Gets cache
   *
   * @return value of cache
   */
  public Map<String, IRendering> getCache() {
    return cache;
  }

  /**
   * Sets cache
   *
   * @param cache value of cache
   */
  public void setCache(Map<String, IRendering> cache) {
    this.cache = cache;
  }
}
