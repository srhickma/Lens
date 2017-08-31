package com.konjex.util.provide;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provider design pattern implementation.
 */
public class Provider<MatchType, ProvidedType extends Providable<MatchType>> {

    private Map<MatchType, ProvidedType> cache = new HashMap<>();

    public void store(Stream<ProvidedType> objects){
        objects.forEach(this::store);
    }

    public void store(Collection<ProvidedType> objects){
        objects.forEach(this::store);
    }

    public void store(ProvidedType[] objects){
        for(ProvidedType object : objects){
            store(object);
        }
    }

    public void store(ProvidedType object){
        cache.put(object.getProviderKey(), object);
    }

    public ProvidedType request(MatchType key) throws ProvidableNotFoundException {
        if(cache.containsKey(key)){
            return cache.get(key);
        }
        throw new ProvidableNotFoundException();
    }

}
