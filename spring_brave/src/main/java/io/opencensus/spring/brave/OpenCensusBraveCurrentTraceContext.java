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

import brave.propagation.CurrentTraceContext;
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
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Map;

/**
 * Implementation of the {@link CurrentTraceContext} that synchronize the Brave's current
 * {@link TraceContext} with the OpenCensus's current {@link Span}.
 *
 * <p>The synchronized {@code Span} is a no-op implementation that only carries the trace
 * identifiers in order to ensure trace continuation when OpenCensus is used to create new spans.
 */
public final class OpenCensusBraveCurrentTraceContext extends CurrentTraceContext {

  private static final io.opencensus.trace.Tracer tracer = io.opencensus.trace.Tracing.getTracer();

  private final CurrentTraceContext delegate;

  static OpenCensusBraveCurrentTraceContext create() {
    return new OpenCensusBraveCurrentTraceContext(Default.create());
  }

  @Override
  public TraceContext get() {
    return delegate.get();
  }

  @Override
  public Scope newScope(final TraceContext traceContext) {
    final Scope scope = delegate.newScope(traceContext);
    final Span span = new BraveOpenCensusSpan(traceContext);
    final io.opencensus.common.Scope openCensusScope = tracer.withSpan(span);

    class OpenCensusBraveCurrentTraceContextScope implements Scope {

      private OpenCensusBraveCurrentTraceContextScope() {
      }

      public void close() {
        scope.close();
        openCensusScope.close();
      }
    }

    return new OpenCensusBraveCurrentTraceContextScope();
  }

  private OpenCensusBraveCurrentTraceContext(CurrentTraceContext delegate) {
    this.delegate = delegate;
  }

  private static class BraveOpenCensusSpan extends Span {

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
