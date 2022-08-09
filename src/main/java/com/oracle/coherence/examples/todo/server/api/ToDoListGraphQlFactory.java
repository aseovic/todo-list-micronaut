/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.examples.todo.server.api;

import com.oracle.coherence.examples.todo.server.Task;
import com.oracle.coherence.examples.todo.server.TaskNotFoundException;
import com.oracle.coherence.examples.todo.server.TaskRepository;

import com.oracle.coherence.examples.todo.server.ToDoListService;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;

import graphql.GraphQL;

import graphql.language.StringValue;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import io.micronaut.core.io.ResourceResolver;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * Various bean factories for {@code GraphQL} {@link DataFetcher}s.
 */
@Factory
public class ToDoListGraphQlFactory
    {
    @Bean
    @Singleton
    @Inject
    public GraphQL graphQL(ResourceResolver resourceResolver,
                           @Named("createTask") DataFetcher<Task> createTaskFetcher,
                           @Named("deleteCompletedTasks") DataFetcher<Boolean> deleteCompletedTasksFetcher,
                           @Named("deleteTask") DataFetcher<Task> deleteTaskFetcher,
                           @Named("updateDescription") DataFetcher<Task> updateDescriptionFetcher,
                           @Named("updateCompletionStatus") DataFetcher<Task> updateCompletionStatusFetcher,
                           @Named("findTask") DataFetcher<Task> findTaskFetcher,
                           @Named("tasks") DataFetcher<Collection<Task>> tasksFetcher)
        {
        // Parse the schema.
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        SchemaParser schemaParser = new SchemaParser();
        typeRegistry.merge(schemaParser.parse(
                new BufferedReader(new InputStreamReader(resourceResolver.getResourceAsStream("classpath:schema.graphqls").get()))));

        // Create the runtime wiring.
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .scalar(BIG_INTEGER)
                .scalar(LOCAL_DATE_TIME)
                .type("Query", typeWiring -> typeWiring.dataFetcher("findTask", findTaskFetcher))
                .type("Query", typeWiring -> typeWiring.dataFetcher("tasks", tasksFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("createTask", createTaskFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("deleteCompletedTasks", deleteCompletedTasksFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("deleteTask", deleteTaskFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("updateDescription", updateDescriptionFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("updateCompletionStatus", updateCompletionStatusFetcher))
                .build();

        // Create the executable schema.
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

        // Return the GraphQL bean.
        return GraphQL.newGraphQL(graphQLSchema).build();
        }

    @Bean
    @Singleton
    @Named("createTask")
    public DataFetcher<Task> createTasksFetcher(ToDoListService tasks)
        {
        return environment ->
                tasks.createTask(environment.getArgument("description"));
        }

    @Bean
    @Singleton
    @Named("deleteCompletedTasks")
    public DataFetcher<Boolean> deleteCompletedTasksFetcher(ToDoListService tasks)
        {
        return environment -> tasks.deleteCompletedTasks();
        }

    @Bean
    @Singleton
    @Named("deleteTask")
    public DataFetcher<Task> deleteTaskFetcher(ToDoListService tasks)
        {
        return environment ->
                tasks.deleteTask(environment.getArgument("id"));
        }

    @Bean
    @Singleton
    @Named("findTask")
    public DataFetcher<Task> findTaskFetcher(ToDoListService tasks)
        {
        return environment ->
                tasks.findTask(environment.getArgument("id"));
        }

    @Bean
    @Singleton
    @Named("tasks")
    public DataFetcher<Collection<Task>> tasksFetcher(ToDoListService tasks)
        {
        return environment ->
                tasks.getTasks(environment.getArgument("completed"));
        }

    @Bean
    @Singleton
    @Named("updateDescription")
    public DataFetcher<Task> updateDescriptionFetcher(ToDoListService tasks)
        {
        return environment ->
            {
            String id = environment.getArgument("id");
            String description = environment.getArgument("description");

            return tasks.updateDescription(id, description);
            };
        }

    @Bean
    @Singleton
    @Named("updateCompletionStatus")
    public DataFetcher<Task> updateCompletionStatusFetcher(ToDoListService tasks)
        {
        return environment ->
            {
            String id = environment.getArgument("id");
            boolean completed = environment.getArgument("completed");

            return tasks.updateCompletionStatus(id, completed);
            };
        }

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();


    // TODO: replace with graphql-java-extended-scalars once it become possible to use graphql-java 15+
    public static final GraphQLScalarType BIG_INTEGER = GraphQLScalarType.newScalar()
            .name("BigInteger")
            .description("A custom scalar that handles BigInteger")
            .coercing(new Coercing<BigInteger, String>()
                {
                public String serialize(Object o)
                        throws CoercingSerializeException
                    {
                    return o.toString();
                    }

                public BigInteger parseValue(Object o)
                        throws CoercingParseValueException
                    {
                    return BigInteger.valueOf(Long.parseLong((String) o));
                    }

                public BigInteger parseLiteral(Object o)
                        throws CoercingParseLiteralException
                    {
                    return BigInteger.valueOf(Long.parseLong((String) o));
                    }
                })
            .build();

    public static final GraphQLScalarType LOCAL_DATE_TIME = GraphQLScalarType.newScalar()
            .name("LocalDateTime")
            .description("A custom scalar that handles LocalDateTime")
            .coercing(new Coercing<LocalDateTime, String>()
                {
                @Override
                public String serialize(Object input)
                    {
                    LocalDateTime localDateTime;
                    if (input instanceof LocalDateTime)
                        {
                        localDateTime = (LocalDateTime) input;
                        }
                    else if (input instanceof String)
                        {
                        localDateTime = parseLocalDateTime(input.toString(), CoercingSerializeException::new);
                        }
                    else
                        {
                        throw new CoercingSerializeException(
                                "Expected something we can convert to 'java.time.LocalDateTime' but was '" + input.getClass() + "'.");
                        }
                    try
                        {
                        return LOCAL_DATE_TIME_FORMATTER.format(localDateTime);
                        }
                    catch (DateTimeException e)
                        {
                        throw new CoercingSerializeException(
                                "Unable to turn TemporalAccessor into LocalDateTime because of : '" + e.getMessage() + "'."
                        );
                        }
                    }

                @Override
                public LocalDateTime parseValue(Object input)
                    {
                    LocalDateTime localDateTime;
                    if (input instanceof LocalDateTime)
                        {
                        localDateTime = (LocalDateTime) input;
                        }
                    else if (input instanceof String)
                        {
                        localDateTime = parseLocalDateTime(input.toString(), CoercingParseValueException::new);
                        }
                    else
                        {
                        throw new CoercingParseValueException("Expected a 'String' but was '" + "typeName(input)" + "'.");
                        }
                    return localDateTime;
                    }

                private LocalDateTime parseLocalDateTime(String s, Function<String, RuntimeException> exceptionMaker)
                    {
                    try
                        {
                        return LocalDateTime.parse(s, LOCAL_DATE_TIME_FORMATTER);
                        }
                    catch (DateTimeParseException e)
                        {
                        throw exceptionMaker.apply("Invalid RFC3339 value : '" + s + "'. because of : '" + e.getMessage() + "'");
                        }
                    }

                @Override
                public LocalDateTime parseLiteral(Object input)
                    {
                    if (!(input instanceof StringValue))
                        {
                        throw new CoercingParseLiteralException(
                                "Expected AST type 'StringValue' but was '" + input.getClass() + "'.");
                        }
                    return parseLocalDateTime(((StringValue) input).getValue(), CoercingParseLiteralException::new);
                    }
                })
            .build();

    }
