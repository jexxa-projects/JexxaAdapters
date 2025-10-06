package io.jexxa.common.drivenadapter.persistence.objectstore.s3;


import io.jexxa.common.drivenadapter.persistence.objectstore.INumericQuery;
import io.jexxa.common.drivenadapter.persistence.objectstore.IObjectStore;
import io.jexxa.common.drivenadapter.persistence.objectstore.IStringQuery;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.NumericTag;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.StringTag;
import io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository;

import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class S3ObjectStore<T, K, M extends Enum<M> & MetadataSchema>  extends S3KeyValueRepository<T, K> implements IObjectStore<T, K, M>
{
    private final Set<M> metaData;

    public S3ObjectStore(
            Class<T> aggregateClazz,
            Function<T, K> keyFunction,
            Class<M> metaData,
            Properties properties
            )
    {
        super(aggregateClazz, keyFunction, properties);
        this.metaData = EnumSet.allOf(metaData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> INumericQuery<T, S> getNumericQuery(M metaTag, Class<S> queryType)
    {
        if ( !metaData.contains(metaTag) )
        {
            throw new IllegalArgumentException("Unknown strategy for "+ metaTag.name());
        }

        //noinspection unchecked
        NumericTag<T, S> numericTag = (NumericTag) metaTag.getTag();

        return new S3NumericQuery<>(this, numericTag, queryType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> IStringQuery<T, S> getStringQuery(M metaTag, Class<S> queryType)
    {
        if ( !metaData.contains(metaTag) )
        {
            throw new IllegalArgumentException("Unknown strategy for " + metaTag.name());
        }

        //noinspection unchecked
        StringTag<T, S> stringTag = (StringTag) metaTag.getTag();

        return new S3StringQuery<>(this, stringTag, queryType);
    }


    List<T> getAggregates()
    {
        return get();
    }

}

