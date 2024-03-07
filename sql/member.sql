drop table if exists memeber;
create table memeber (
    id  bigint not null comment 'id',
    mobile varchar(11) comment '手机号',
    primary key (id),
    unique key mobile_unique(mobile)
)engine=innodb default charset=utf8mb4 comment='用户';