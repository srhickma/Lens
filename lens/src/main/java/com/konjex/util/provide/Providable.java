package com.konjex.util.provide;

/**
 * Object that can be provided by a provider.
 */
public interface Providable<MatchType> {

    MatchType getProviderKey();

}
