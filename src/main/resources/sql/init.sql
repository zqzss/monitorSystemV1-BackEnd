DROP DATABASE IF EXISTS test1;
CREATE DATABASE test1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

drop table if exists test1.user;
CREATE TABLE test1.user
(
    `id`       int(20)                                                NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `fullname` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `age`      int(3)                                                 NULL,
    `tel`      varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `email`    varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `deleted`  bigint(1)                                              NULL DEFAULT 0,
    `version`  int(11)                                                NULL DEFAULT 0
);
INSERT INTO test1.user(username, password, fullname, email)
VALUES ("admin", "7adb67ff773f359f33ae2e457f6e281d", "admin", "448933144@qq.com");

drop table if exists test1.host;
CREATE TABLE test1.host
(
    `id`              int(20)                                                NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `hostname`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `ip`              varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `port`            int(5)                                                 NULL,
    `username`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `password`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `description`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
    `enable`          int(1)                                                 NULL DEFAULT 1,
    `reConnectNumber` int(1)                                                 NULL DEFAULT 1,
    `deleted`         int(1)                                                 NULL DEFAULT 0,
    `version`         int(11)                                                NULL DEFAULT 0,
    `reason`          varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL
);

drop table if exists test1.monitorType;
CREATE TABLE test1.monitorType
(
    id   int(20)     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name varchar(32) NULL
);
insert into test1.monitorType(id, name) value (1, "cpu使用率(单位: %)");
insert into test1.monitorType(id, name) value (2, "内存剩余空间(单位: G)");
insert into test1.monitorType(id, name) value (3, "磁盘分区剩余空间(单位: G)");
insert into test1.monitorType(id, name) value (4, "端口");
insert into test1.monitorType(id, name) value (5, "进程名称");
insert into test1.monitorType(id, name) value (6, "url");

drop table if exists test1.monitorItem;
CREATE TABLE test1.monitorItem
(
    id             int(20)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    host_id        int(20)      NOT NULL,
    monitorType_id int(20)      NOT NULL,
    detail         varchar(128) NULL,
    warnValue      float        NULL,

    CONSTRAINT monitorItem_host_fk FOREIGN KEY (host_id) REFERENCES host (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT monitorItem_monitorType_fk FOREIGN KEY (monitorType_id) REFERENCES monitorType (id) ON DELETE CASCADE ON UPDATE CASCADE
);


drop table if exists test1.monitorData;
CREATE TABLE test1.monitorData
(
    id             int(20)     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    host_id        int(20)     NOT NULL,
    monitorType_id int(20)     NOT NULL,
    monitorItem_id int(20)     NOT NULL,
    CONSTRAINT monitorData_host_fk FOREIGN KEY (host_id) REFERENCES host (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT monitorData_monitorType_fk FOREIGN KEY (monitorType_id) REFERENCES monitorType (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT monitorData_monitorItem_fk FOREIGN KEY (monitorItem_id) REFERENCES monitorItem (id) ON DELETE CASCADE ON UPDATE CASCADE,
    createTime     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data           varchar(32) NULL
);

drop table if exists test1.monitorAlarm;
CREATE TABLE test1.monitorAlarm
(
    id             int(20)     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    host_id        int(20)     NOT NULL,
    monitorType_id int(20)     NOT NULL,
    monitorItem_id int(20)     NOT NULL,

    CONSTRAINT monitorAlarm_host_fk FOREIGN KEY (host_id) REFERENCES host (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT monitorAlarm_monitorType_fk FOREIGN KEY (monitorType_id) REFERENCES monitorType (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT monitorAlarm_monitorItem_fk FOREIGN KEY (monitorItem_id) REFERENCES monitorItem (id) ON DELETE CASCADE ON UPDATE CASCADE,
    alarmTime      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data           varchar(32) NULL

);

drop table if exists test1.noticeType;
CREATE TABLE test1.noticeType
(
    id             int(20)     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name           varchar(32) NULL,
    intervalMinute int(20)     NULL
);
insert into test1.noticeType(id, name, intervalMinute)
values (1, '邮箱通知', 10);
drop table if exists test1.noticeItem;
CREATE TABLE test1.noticeItem
(
    id             int(20)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    noticeType_id  int(20)      NOT NULL,
    user_id        int(20)      NULL,

    createTime     datetime     NULL,
    lastNoticeTime datetime     NULL,
    webhookAddress varchar(128) NULL,
    Enable         int(2)       NULL defalut 0,
    CONSTRAINT noticeItem_noticeType_fk FOREIGN KEY (noticeType_id) REFERENCES noticeType (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT noticeItem_user_fk FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE

);