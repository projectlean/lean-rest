package org.lean.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("lean")
public class LeanRestApplication extends Application {
  public LeanRestApplication() {
    // Initialize the singleton up-front
    LeanUtil.getInstance();
  }
}
