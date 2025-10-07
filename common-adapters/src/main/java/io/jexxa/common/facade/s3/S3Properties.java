package io.jexxa.common.facade.s3;

public class S3Properties {
    private static String prefix = "";

    public static final String S3_ENDPOINT = "s3.endpoint";
    public static final String S3_REGION = "s3.region";
    public static final String S3_BUCKET = "s3.bucket";
    public static final String S3_ACCESS_KEY = "s3.access-key";
    public static final String S3_SECRET_KEY = "s3.secret-key";
    public static final String S3_FILE_ACCESS_KEY = "s3.file.access-key-path";
    public static final String S3_FILE_SECRET_KEY = "s3.file.secret-key-path";
    public static final String S3_APPLICATION_PREFIX = "s3.application.prefix";

    public static String s3ApplicationPrefix() {
        return prefix() + S3_APPLICATION_PREFIX;
    }

    public static String s3Endpoint() {
        return prefix() + S3_ENDPOINT;
    }

    public static String s3Region()
    {
        return prefix() + S3_REGION;
    }
    public static String s3Bucket()
    {
        return prefix() + S3_BUCKET;
    }

    public static String s3AccessKey()
    {
        return prefix() + S3_ACCESS_KEY;
    }

    public static String s3SecretKey()
    {
        return prefix() + S3_SECRET_KEY;
    }

    public static String s3FileAccessKey()
    {
        return prefix() + S3_FILE_ACCESS_KEY;
    }

    public static String s3FileSecretKey()
    {
        return prefix() + S3_FILE_SECRET_KEY;
    }

    public static void prefix(String prefix) { S3Properties.prefix = prefix;}
    public static String prefix() { return S3Properties.prefix; }
    private S3Properties()
    {
        //private constructor
    }
}
