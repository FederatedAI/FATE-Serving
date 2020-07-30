package com.webank.ai.fate.serving.admin.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter(urlPatterns={"/**"}, filterName="PotentialClickjackingFilter")
public class SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        ((HttpServletResponse) resp).addHeader("X-Frame-Options","DENY");
        filterChain.doFilter(req, resp);
    }
}