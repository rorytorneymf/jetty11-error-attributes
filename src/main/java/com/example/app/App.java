/*
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and its
 * affiliates and licensors ("Open Text") are as may be set forth in the
 * express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional
 * warranty. Open Text shall not be liable for technical or editorial
 * errors or omissions contained herein. The information contained herein
 * is subject to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items
 * are licensed to the U.S. Government under vendor's standard commercial
 * license.
 */
package com.example.app;


import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.util.security.Constraint;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class App extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        // 1. Set breakpoint on line 112 of ErrorPageErrorHandler:
        // errorStatusCode = (Integer)request.getAttribute(org.eclipse.jetty.server.handler.ErrorHandler.ERROR_STATUS);
        // 2. Debug this main method using Java 17 + the command line args 'server app-config-dev.json'
        // 3. Navigate to http://localhost:3200/bla, breakpoint should be hit
        //
        // The return value from the above line of code is null in Jetty 12, because no error attributes are set,
        // whereas in Jetty 11 multiple error attributes are set.
        //
        // The error attributes are used to map to the error page, but in Jetty 12 this does not happen and a
        // generic page is shown instead of my custom error page.
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/app-error", "/app-error", "index.html", "app-error"));
        super.initialize(bootstrap);
    }

    @Override
    public void run(final Configuration apiGatewayConfiguration, final Environment environment) {

        initAuthentication(environment);

        final ErrorPageErrorHandler errorPageErrorHandler = new ErrorPageErrorHandler();
        errorPageErrorHandler.addErrorPage(403, "/app-error/error.html");
        environment.getApplicationContext().setErrorHandler(errorPageErrorHandler);
    }

    private static void initAuthentication(final Environment environment) {
        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        environment.getApplicationContext().setSecurityHandler(securityHandler);
        final Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"**"});

        final ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setPathSpec("/*");
        constraintMapping.setConstraint(constraint);
        constraintMapping.setMethodOmissions(new String[]{"OPTIONS"});
        securityHandler.addConstraintMapping(constraintMapping);
    }
}
