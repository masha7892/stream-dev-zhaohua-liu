-- select
--     ri.id,
--     ri.user_id,
--     ri.order_id,
--     ri.sku_id,
--     oi.province_id,
--     date_format(to_timestamp_ltz(cast(ri.create_time as bigint),3),cast('yyyy-MM-dd' as string)),
--     ri.create_time,
--     ri.refund_type,
--     dic1.dic_name,
--     ri.refund_reason_type,
--     dic2.dic_name,
--     ri.refund_reason_txt,
--     ri.refund_num,
--     ri.refund_amount,
--     ri.ts
-- from order_refund_info ri
-- join order_info oi on ri.order_id=oi.id
-- join base_dic for system_time as of ri.pt as dic1
-- on r1.refund_type=dic1.dic_code
-- join base_dic for system_time as of ri.pt as dic2
-- on ri.refund_reason_type=dic2.dic_code


create table a(
    id string,
    user_id string,
    order_id string,
    sku_id string,
    province_id string,
    date_id  string,
    create_time string,
    refund_type string,
    refund_type_name string,
    refund_reason_type string,
    refund_reason_type_name string,
    refund_reason_txt string,
    refund_num string,
    refund_amount string,
    ts bigint,
    primary key (id) not enforced
)