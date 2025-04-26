create table a(
    order_detail_id string,
    order_id string,
    user_id string,
    sku_id string,
    sku_name string,
    province_id string,
    activity_id string,
    activity_rule_id string,
    coupon_id string,
    payment_type_code string,
    payment_type_name string,
    callback_time string,
    sku_num string,
    split_original_amount string,
    split_activity_amount string,
    split_coupon_amount string,
    split_payment_amount string,
    ts bigint,
    primary key (order_detail_id) not enforced )

-- select
--     od.id order_detail_id,
--     od.order_id,
--     od.user_id,
--     od.sku_id,
--     od.sku_name,
--     od.province_id,
--     od.activity_id,
--     od.activity_rule_id,
--     od.coupon_id,
--     pi.payment_type payment_type_code ,
--     dic.dic_name payment_type_name,
--     pi.callback_time,
--     od.sku_num,
--     od.split_original_amount,
--     od.split_activity_amount,
--     od.split_coupon_amount,
--     od.split_total_amount split_payment_amount,
--     pi.ts
-- from payment_info pi
-- join dwd_trade_order_detail od
-- on pi.order_id=od.order_id
-- and od.et>=pi.et-interval '30' minute
-- and od.et<=pi.et+interval '5' second
-- join base_dic for system_time as of pi.pt as dic
-- on pi.payment_type=dic.dic_code;


-- select
--     `after`['user_id'] user_id,
--     `after`['order_id'] order_id,
--     `after`['payment_type'] payment_type,
--     `after`['callback_time'] callback_time,
--     `pt` as proctime(),
--     `ts`,
--     `et`
-- from ods_initial
-- where `source`['table']='payment_info'
-- and `op`='u'
-- and `before`['payment_status'] is null
-- and `after`['payment_status']='1602'

-- create table dwd_trade_order_detail
-- (
--     id                    string,
--     order_id              string,
--     user_id               string,
--     sku_id                string,
--     sku_name              string,
--     province_id           string,
--     activity_id           string,
--     activity_rule_id      string,
--     coupon_id             string,
--     date_id               string,
--     create_time           string,
--     sku_num               string,
--     split_original_amount string,
--     split_activity_amount string,
--     split_coupon_amount   string,
--     split_total_amount    string,
--     ts                    bigint,
--     et AS TO_TIMESTAMP_LTZ(ts, 3),
--     WATERMARK FOR et AS et - INTERVAL '5' SECOND
-- )