/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.examples.todo.server.api;

import com.oracle.coherence.examples.todo.server.Task;
import com.oracle.coherence.examples.todo.server.TaskRepository;
import com.oracle.coherence.examples.todo.server.ToDoListService;
import com.tangosol.net.Cluster;
import com.tangosol.net.Member;

import io.micronaut.core.annotation.Nullable;

import io.micronaut.http.MediaType;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;

import io.micronaut.http.sse.Event;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.util.Collection;

import javax.annotation.PostConstruct;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.reactivestreams.Publisher;

/**
 * REST API for To Do list management.
 */
@Controller("/api/tasks")
@Singleton
public class ToDoListRestController
    {
    // TODO: add implementation here
    }
