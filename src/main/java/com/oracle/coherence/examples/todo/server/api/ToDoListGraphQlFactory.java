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
    // TODO: add implementation here

    // ---- scalars ---------------------------------------------------------
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
