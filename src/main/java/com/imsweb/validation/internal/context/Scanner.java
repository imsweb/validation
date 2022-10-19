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
public interface Scanner {

    /**
     * Created on Oct 4, 2011 by murphyr
     * @return the next <code>Symbol</code>
     */
    Symbol next_token() throws Exception;
}
