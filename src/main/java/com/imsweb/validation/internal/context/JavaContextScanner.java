/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.context;

/**
 * Java context scanner interface.
 * <p/>
 * Created on Oct 7, 2011 by depryf
 * @author depryf
 */
@SuppressWarnings("java:S100")
public interface JavaContextScanner {

    /**
     * Created on Oct 4, 2011 by murphyr
     * @return the next <code>Symbol</code>
     */
    @SuppressWarnings("java:S112")
    // don't throw Exception (this is a very old class, I don't want to mess with that)
    JavaContextSymbol next_token() throws Exception;
}
