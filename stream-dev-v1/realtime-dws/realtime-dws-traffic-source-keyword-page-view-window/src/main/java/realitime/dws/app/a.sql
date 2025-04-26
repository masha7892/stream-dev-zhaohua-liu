-- create table topic_log(
--     common map<string,string>,
--     page map<string,string>,
--     ts bigint,
--     et as to_timestamp_ltz(ts,3),
--     watermark for et as et
-- )


-- select
--     `page`['item'] fullword,
--     et
-- from order_log
-- where `page`['last_page_id']='search'
-- and `page`['item_type']='keyword'
-- and `page`['item'] is not null ;

-- SELECT
--     keyword, et
-- FROM search_table,
-- LATERAL TABLE(ik_analyze(fullword)) t(keyword)

SELECT
    window_start,
    window_end,
    keyword,
    count(price)
FROM TABLE(
             TUMBLE(TABLE split_table, DESCRIPTOR(et), INTERVAL '10' second ))
GROUP BY window_start, window_end,keyword;