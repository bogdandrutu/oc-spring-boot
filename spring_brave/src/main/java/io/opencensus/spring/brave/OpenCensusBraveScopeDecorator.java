/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.spring.brave;

import brave.propagation.CurrentTraceContext.Scope;
import brave.propagation.CurrentTraceContext.ScopeDecorator;
import brave.propagation.TraceContext;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Map;

/**
 * Implementation of the {@link ScopeDecorator} that synchronize the Brave's current
 * {@link TraceContext} with the OpenCensus's current {@link Span}.
 *
 * <p>The synchronized {@code Span} is a no-op implementation that only carries the trace
 * identifiers in order to ensure trace continuation when OpenCensus is used to create new spans.
 */
public final class OpenCensusBraveScopeDecorator implements ScopeDecorator {

  private static final Tracer tracer = Tracing.getTracer();

  @Override
  public Scope decorateScope(TraceContext traceContext, Scope scope) {
    final Span span = new BraveOpenCensusSpan(traceContext);
    final io.opencensus.common.Scope openCensusScope = tracer.withSpan(span);

    class OpenCensusBraveCurrentTraceContextScope implements Scope {
      @Override
      public void close() {
        scope.close();
        openCensusScope.close();
      }
    }
    return new OpenCensusBraveCurrentTraceContextScope();
  }

  private static final class BraveOpenCensusSpan extends Span {

    private static final EnumSet<Options> recordOptions = EnumSet.of(Options.RECORD_EVENTS);
    private static final EnumSet<Options> notRecordOptions = EnumSet.noneOf(Options.class);

    private static final TraceOptions sampledOptions = TraceOptions.builder().setIsSampled(true)
        .build();
    private static final TraceOptions notSampledOptions = TraceOptions.builder().setIsSampled(false)
        .build();

    BraveOpenCensusSpan(TraceContext traceContext) {
      super(fromTraceContext(traceContext),
          Boolean.TRUE.equals(traceContext.sampled()) ? recordOptions
              :
                  notRecordOptions);
    }

    @Override
    public void addAnnotation(String s, Map<String, AttributeValue> map) {
    }

    @Override
    public void addAnnotation(Annotation annotation) {
    }

    @Override
    public void addLink(Link link) {
    }

    @Override
    public void end(EndSpanOptions endSpanOptions) {
    }

    private static SpanContext fromTraceContext(TraceContext traceContext) {
      return SpanContext.create(TraceId.fromBytes(ByteBuffer.allocate(TraceId.SIZE).putLong
              (traceContext.traceIdHigh()).putLong(traceContext.traceId())
              .array()),
          SpanId.fromBytes(ByteBuffer.allocate(SpanId.SIZE).putLong(traceContext.spanId()).array
              ()),
          Boolean.TRUE.equals(traceContext.sampled()) ? sampledOptions : notSampledOptions);
    }
  }
}
