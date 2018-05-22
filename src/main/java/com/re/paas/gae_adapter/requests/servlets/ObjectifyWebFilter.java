package com.re.paas.gae_adapter.requests.servlets;

import javax.servlet.annotation.WebFilter;
import com.googlecode.objectify.ObjectifyFilter;

@WebFilter(urlPatterns = { "/*" })
public class ObjectifyWebFilter extends ObjectifyFilter {
}
