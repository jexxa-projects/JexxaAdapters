package io.jexxa.commons.adapter.persistence.objectstore.metadata;


public interface MetadataSchema
{
    <T, S, V> MetaTag<T, S, V> getTag();
}