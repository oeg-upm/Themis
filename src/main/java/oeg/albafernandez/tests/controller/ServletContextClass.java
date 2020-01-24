package oeg.albafernandez.tests.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by Alba on 10/01/2017.
 */
@WebListener
public class ServletContextClass implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent)
    {

    }

    public void contextDestroyed(ServletContextEvent event){

    }
}
