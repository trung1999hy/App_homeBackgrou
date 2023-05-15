package com.hst.beautifulwall.data.sqlite;

import android.provider.BaseColumns;

public class DbConstants implements BaseColumns {

    // commons
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String COLUMN_NAME_NULLABLE = null;

    // notification
    public static final String NOTIFICATION_TABLE_NAME = "notifications";

    public static final String COLUMN_NOTI_TITLE = "not_title";
    public static final String COLUMN_NOTI_MESSAGE = "not_message";
    public static final String COLUMN_NOTI_READ_STATUS = "not_status";
    public static final String COLUMN_NOTI_CONTENT_URL = "content_url";

    public static final String SQL_CREATE_NOTIFICATION_ENTRIES =
            "CREATE TABLE " + NOTIFICATION_TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NOTI_TITLE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTI_MESSAGE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTI_CONTENT_URL + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTI_READ_STATUS + TEXT_TYPE +
                    " )";


    public static final String SQL_DELETE_NOTIFICATION_ENTRIES =
            "DROP TABLE IF EXISTS " + NOTIFICATION_TABLE_NAME;

    // favorites
    public static final String FAVORITE_TABLE_NAME = "favorite";

    public static final String POST_TITLE = "post_title";
    public static final String POST_IMAGE_URL = "post_image_url";
    public static final String POST_CATEGORY = "post_category";
    public static final String IS_NEED_POINT = "is_need_point";

    public static final String SQL_CREATE_FAVOURITE_ENTRIES =
            "CREATE TABLE " + FAVORITE_TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    POST_TITLE + TEXT_TYPE + COMMA_SEP +
                    POST_IMAGE_URL + TEXT_TYPE + COMMA_SEP +
                    POST_CATEGORY + TEXT_TYPE +
                    " )";


    public static final String SQL_DELETE_FAVORITE_ENTRIES =
            "DROP TABLE IF EXISTS " + FAVORITE_TABLE_NAME;

}
