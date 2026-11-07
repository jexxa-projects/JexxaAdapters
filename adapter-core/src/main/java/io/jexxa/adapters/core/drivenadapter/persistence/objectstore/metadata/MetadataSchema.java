package io.jexxa.adapters.core.drivenadapter.persistence.objectstore.metadata;


public interface MetadataSchema
{
    <T, S, V> MetaTag<T, S, V> getTag();
}