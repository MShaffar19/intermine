package org.intermine.web;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.util.ModuleException;

/**
 * InterMine implementation of the Struts ExceptionHandler
 *
 * @author Andrew Varley
 */
public class InterMineExceptionHandler extends ExceptionHandler
{
    protected static final Logger LOG = Logger.getLogger(InterMineExceptionHandler.class);

    /**
     * Handle the exception. In this case we traverse the exception
     * chain and report the message from each exception. The original
     * exception and the stack trace are also added to the request.
     *
     * @param ex The exception to handle
     * @param ae The ExceptionConfig corresponding to the exception
     * @param mapping The ActionMapping we are processing
     * @param formInstance The ActionForm we are processing
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return an ActionForward
     *
     * @exception ServletException if a servlet exception occurs
     *
     */
    public ActionForward execute(Exception ex,
                                 ExceptionConfig ae,
                                 ActionMapping mapping,
                                 ActionForm formInstance,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws ServletException {

        ActionForward forward = null;
        ActionMessage error = null;
        String property = null;

        // Build the forward from the exception mapping if it exists
        // or from the form input
        if (ae.getPath() != null) {
            forward = new ActionForward(ae.getPath());
        } else {
            forward = mapping.getInputForward();
        }

        Throwable t = ex;

        // Put the stack trace on the request
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        request.setAttribute("stacktrace", sw.toString());

        LOG.error(sw.toString());
        
        // Put the original exception on the request
        request.setAttribute("exception", ex);

        do {
            // Figure out the error
            if (t instanceof ModuleException) {
                error = ((ModuleException) t).getError();
                property = ((ModuleException) t).getProperty();
            } else {
                error = new ActionMessage(ae.getKey(), t.getMessage());
                property = error.getKey();
            }

            // Store the exception as a message
            storeException(request, property, error, forward, ae.getScope());
        } while ((t = t.getCause()) != null);

        return forward;
    }

    /**
     * Default implementation for handling an <b>ActionMessage</b> generated
     * from an Exception during <b>Action</b> delegation.  The default
     * implementation is to set an attribute of the request or session, as
     * defined by the scope provided (the scope from the exception mapping).  An
     * <b>ActionErrors</b> instance is created, the error is added to the collection
     * and the collection is set under the Globals.ERROR_KEY.
     *
     * @param request - The request we are handling
     * @param property  - The property name to use for this error
     * @param message - The message generated from the exception mapping
     * @param forward - The forward generated from the input path (from the form
     *              or exception mapping)
     * @param scope - The scope of the exception mapping.
     */
    protected void storeException(HttpServletRequest request,
                                  String property,
                                  ActionMessage message,
                                  ActionForward forward,
                                  String scope) {

        ActionErrors errors = null;
        if ("request".equals(scope)) {
            errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
        } else {
            request.getSession().getAttribute(Globals.ERROR_KEY);
        }
        if (errors == null) {
            errors = new ActionErrors();
        }
        errors.add(property, message);

        if ("request".equals(scope)) {
            request.setAttribute(Globals.ERROR_KEY, errors);
        } else {
            request.getSession().setAttribute(Globals.ERROR_KEY, errors);
        }
    }

}
