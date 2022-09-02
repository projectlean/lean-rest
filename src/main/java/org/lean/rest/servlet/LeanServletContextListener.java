package org.lean.rest.servlet;

//import org.lean.rest.LeanUtil;

import org.lean.rest.LeanUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class LeanServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event){
        LeanUtil leanUtil = LeanUtil.getInstance();
    }

}
