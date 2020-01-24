package oeg.albafernandez.tests.filter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApiOriginFilter implements javax.servlet.Filter {
    /**
     * doFilter
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");
        chain.doFilter(request, response);
    }

    /**
     * destroy
     */
    @Override
    public void destroy() {
    }

    /**
     * init
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}