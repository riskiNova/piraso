package org.piraso.maven.packaging;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * The base packaging task.
 *
 * @author Stephane Nicoll
 * @version $Id: WarPackagingTask.java 682908 2008-08-05 19:57:49Z hboutemy $
 */
public interface WarPackagingTask
{

    /**
     * Performs the packaging for the specified task.
     * <p/>
     * The task is responsible to update the packaging context, namely
     * with the files that have been copied.
     *
     * @param context the packaging context
     * @throws org.apache.maven.plugin.MojoExecutionException if an error occurred
     * @throws org.apache.maven.plugin.MojoFailureException   if the project configuration is invalid
     */
    void performPackaging(WarPackagingContext context)
        throws MojoExecutionException, MojoFailureException;


}