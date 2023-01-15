package org.lean.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;

@Provider
public class LeanCorsFilter implements ContainerResponseFilter {

  private ILogChannel log;

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

    log = new LogChannel("Lean Cors Filter");

    Properties props = new Properties();
    try {
      String propertyPath;
      if (StringUtils.isEmpty(System.getProperty("CONFIG_PATH"))) {
        propertyPath = "/config";
      } else {
        propertyPath = System.getProperty("CONFIG_PATH");
      }
      try (InputStream inputStream = new FileInputStream(propertyPath + "/leanrest.properties")) {
        props.load(inputStream);
        boolean isCors = Boolean.parseBoolean(props.getProperty("cors.allow.origin"));
        if (isCors) {
          responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        }
      }
    } catch (FileNotFoundException e) {
      log.logError("leanrest.properties files not found.", e);
    } catch (IOException e) {
      log.logError("could not read leanrest.properties file", e);
    }
  }
}
