package io.jexxa.common.facade.s3;

import io.jexxa.common.facade.utils.properties.PropertiesPrefix;

public class S3Properties {
    private static final String S3_ENDPOINT = "s3.endpoint";
    private static final String S3_REGION = "s3.region";
    private static final String S3_BUCKET = "s3.bucket";
    private static final String S3_ACCESS_KEY = "s3.access-key";
    private static final String S3_SECRET_KEY = "s3.secret-key";
    private static final String S3_FILE_ACCESS_KEY = "s3.file.access-key-path";
    private static final String S3_FILE_SECRET_KEY = "s3.file.secret-key-path";
    private static final String S3_APPLICATION_PREFIX = "s3.application.prefix";

    public static String s3ApplicationPrefix() {
        return PropertiesPrefix.globalPrefix() + S3_APPLICATION_PREFIX;
    }

    public static String s3Endpoint() {
        return PropertiesPrefix.globalPrefix() + S3_ENDPOINT;
    }

    public static String s3Region()
    {
        return PropertiesPrefix.globalPrefix() + S3_REGION;
    }
    public static String s3Bucket()
    {
        return PropertiesPrefix.globalPrefix() + S3_BUCKET;
    }

    public static String s3AccessKey()
    {
        return PropertiesPrefix.globalPrefix() + S3_ACCESS_KEY;
    }

    public static String s3SecretKey()
    {
        return PropertiesPrefix.globalPrefix() + S3_SECRET_KEY;
    }

    public static String s3FileAccessKey()
    {
        return PropertiesPrefix.globalPrefix() + S3_FILE_ACCESS_KEY;
    }

    public static String s3FileSecretKey()
    {
        return PropertiesPrefix.globalPrefix() + S3_FILE_SECRET_KEY;
    }


    private S3Properties()
    {
        //private constructor
    }
}
