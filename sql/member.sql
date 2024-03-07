drop table if exists member;
create table member (
    id  bigint not null comment 'id',
    mobile varchar(11) comment '手机号',
    primary key (id),
    unique key mobile_unique(mobile)
)engine=innodb default charset=utf8mb4 comment='用户';

insert into member values(1,'18735874098');

select count(*) from member;

delete from member where id =2;