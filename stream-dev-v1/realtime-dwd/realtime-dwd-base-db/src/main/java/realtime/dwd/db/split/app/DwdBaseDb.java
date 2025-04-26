package realtime.dwd.db.split.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import realtime.common.base.BaseApp;
import realtime.common.bean.TableProcessDwd;
import realtime.common.constant.Constant;
import realtime.common.util.FlinkSinkUtil;
import realtime.common.util.FlinkSourceUtil;
import realtime.dwd.db.split.function.BaseDbTableProcessFunction;

/**
 * @Package realtime.dwd.db.split.app.DwdBaseDb
 * @Author zhaohua.liu
 * @Date 2025/4/17.13:54
 * @description:处理逻辑比较简单的事实表动态分流处理
 */
public class DwdBaseDb extends BaseApp {
    public static void main(String[] args) throws Exception {
        new DwdBaseDb().start(20010,4, Constant.TOPIC_DWD_BASE_DB,Constant.TOPIC_ODS_INITIAL);

    }
    @Override
    public void handle(StreamExecutionEnvironment env, DataStreamSource<String> kafkaStreamDS) {
        //重置消费者组offset
        //kafka-consumer-groups  --bootstrap-server cdh01:9092  --group dwd_base_db --reset-offsets --topic ods_initial --to-earliest --execute
        //对ods_initial流中的数据进行类型转换并进行简单的ETL jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStreamDS.process(
                new ProcessFunction<String, JSONObject>() {
                    @Override
                    public void processElement(String s, ProcessFunction<String, JSONObject>.Context context, Collector<JSONObject> collector) throws Exception {
                        try {
                            JSONObject jsonObj = JSON.parseObject(s);
                            Boolean snapshot = jsonObj.getJSONObject("source").getBoolean("snapshot");
                            if (snapshot == false) {
                                collector.collect(jsonObj);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("不是一个标准的json块");
                        }
                    }
                }
        );
        //使用FlinkCDC读取配置table_process_dwd表中的配置信息
        MySqlSource<String> mysqlSource = FlinkSourceUtil.getMysqlSource("e_commerce_config", "table_process_dwd");
        DataStreamSource<String> mysqlStrDS = env.fromSource(mysqlSource, WatermarkStrategy.noWatermarks(), "mysql_source");
        //配置信息转换为实体类
        SingleOutputStreamOperator<TableProcessDwd> tpDS = mysqlStrDS.map(
                new MapFunction<String, TableProcessDwd>() {
                    @Override
                    public TableProcessDwd map(String s) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(s);
                        String op = jsonObj.getString("op");
                        TableProcessDwd tp = null;
                        if ("d".equals(op)) {
                            tp = jsonObj.getObject("before", TableProcessDwd.class);
                        } else {
                            tp = jsonObj.getObject("after", TableProcessDwd.class);
                        }
                        tp.setOp(op);
                        return tp;
                    }
                }
        );
        //转为广播流
        MapStateDescriptor<String, TableProcessDwd> mapStateDescriptor = new MapStateDescriptor<>("mapStateDescriptor", String.class, TableProcessDwd.class);
        BroadcastStream<TableProcessDwd> broadcast = tpDS.broadcast(mapStateDescriptor);
        //关联数据流
        BroadcastConnectedStream<JSONObject, TableProcessDwd> connectDS = jsonObjDS.connect(broadcast);
        SingleOutputStreamOperator<Tuple2<JSONObject, TableProcessDwd>> processDS = connectDS.process(new BaseDbTableProcessFunction(mapStateDescriptor));

                processDS.print();
                processDS.sinkTo(FlinkSinkUtil.getKafkaSink());

    }
}
