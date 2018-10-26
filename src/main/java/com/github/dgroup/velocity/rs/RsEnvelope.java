/*
 * MIT License
 *
 * Copyright (c) 2018 Yurii Dubinka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.dgroup.velocity.rs;

import com.github.dgroup.velocity.Arg;
import com.github.dgroup.velocity.Resource;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.cactoos.Func;
import org.cactoos.Input;
import org.cactoos.Scalar;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;

/**
 * The envelope for {@link Resource}.
 *
 * @since 0.1.0
 */
public class RsEnvelope implements Resource<String> {

    /**
     * The func to evaluate the entity.
     */
    private final Func<VelocityContext, String> fnc;

    /**
     * Ctor.
     * @param src The Velocity template as {@link org.cactoos.Input}.
     * @param tname The name of Velocity template.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public RsEnvelope(final Scalar<String> tname, final Scalar<Input> src) {
        this(
            ctx -> {
                // @checkstyle IllegalCatchCheck (20 lines)
                try {
                    final RuntimeServices rsrv = RuntimeSingleton
                        .getRuntimeServices();
                    final StringReader srdr = new StringReader(
                        new TextOf(src.value()).asString()
                    );
                    final Template template = new Template();
                    template.setRuntimeServices(rsrv);
                    template.setData(rsrv.parse(srdr, tname.value()));
                    template.initDocument();
                    final StringWriter writer = new StringWriter();
                    template.merge(ctx, writer);
                    return writer.toString();
                } catch (final Exception cause) {
                    throw new RsException(cause);
                }
            }
        );
    }

    /**
     * Ctor.
     * @param fnc The function to map the {@link VelocityContext} to entity.
     */
    public RsEnvelope(final Func<VelocityContext, String> fnc) {
        this.fnc = fnc;
    }

    @Override
    public final String compose(final Arg... args) throws RsException {
        return this.compose(new ListOf<>(args));
    }

    @Override
    public final String compose(final Iterable<Arg> args)
        throws RsException {
        return this.compose(
            () -> {
                final VelocityContext ctx = new VelocityContext();
                for (final Arg arg : args) {
                    ctx.put(arg.name(), arg.value());
                }
                return ctx;
            }
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public final String compose(final Scalar<VelocityContext> ctx)
        // @checkstyle IllegalCatchCheck (5 lines)
        throws RsException {
        try {
            return this.fnc.apply(ctx.value());
        } catch (final Exception cause) {
            throw new RsException(cause);
        }
    }

}
