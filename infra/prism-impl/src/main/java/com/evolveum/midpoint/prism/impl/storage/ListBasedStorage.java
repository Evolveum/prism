package com.evolveum.midpoint.prism.impl.storage;

import java.util.List;

public class ListBasedStorage<V> extends MultiValueStorage<V> {


    List<V> value()
    
    @Override
    public boolean isEmpty() {
        return false;
    }
}
