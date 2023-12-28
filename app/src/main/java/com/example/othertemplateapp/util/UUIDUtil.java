package com.example.othertemplateapp.util;

import java.util.UUID;

public class UUIDUtil {
    public static final UUID UUID_SERVICE = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB"); //自定义UUID
    //为了兼容IOS端服务数据丢失 增加多个服务 下面UUID 同理
    public static final UUID UUID_SERVICE_2 = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FC"); //自定义UUID

    public static final UUID UUID_SERVICE_3 = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FD"); //自定义UUID

    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("69400003-B5A3-F393-E0A9-E50E24DCCA99");
    public static final UUID UUID_CHAR_READ_NOTIFY_2 = UUID.fromString("69400003-B5A3-F393-E0A9"
            + "-E50E24DCCA98");
    public static final UUID UUID_CHAR_READ_NOTIFY_3 = UUID.fromString("69400003-B5A3-F393-E0A9"
            + "-E50E24DCCA97");

    public static final UUID UUID_DESC_NOTITY = UUID.fromString("11100000-0000-0000-0000-000000000000");

    public static final UUID UUID_CHAR_WRITE = UUID.fromString("69400002-B5A3-F393-E0A9-E50E24DCCA99");
    public static final UUID UUID_CHAR_WRITE_2 = UUID.fromString("69400002-B5A3-F393-E0A9"
            + "-E50E24DCCA98");
    public static final UUID UUID_CHAR_WRITE_3 = UUID.fromString("69400002-B5A3-F393-E0A9"
            + "-E50E24DCCA97");

    public static final UUID UUID_NOTIFY_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    public static final UUID UUID_NOTIFY_DESCRIPTOR_2 = UUID.fromString("00002902-0000-1000-8000"
            + "-00805F9B34FC");
    public static final UUID UUID_NOTIFY_DESCRIPTOR_3 = UUID.fromString("00002902-0000-1000-8000"
            + "-00805F9B34FD");
}
