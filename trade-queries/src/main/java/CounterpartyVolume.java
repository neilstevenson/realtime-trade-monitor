import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.accumulator.MutableReference;
import com.hazelcast.jet.aggregate.AggregateOperation;
import com.hazelcast.jet.aggregate.AggregateOperation1;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.function.FunctionEx;
import com.hazelcast.jet.function.SupplierEx;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Map.Entry;
import java.util.Properties;

import static com.hazelcast.jet.aggregate.AggregateOperations.allOf;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.aggregate.AggregateOperations.summingLong;

public class CounterpartyVolume {

    public static final String TOPIC = "trades";

    public static void cpvQuery(JetInstance jet, String servers) {
        try {
            JobConfig query1config = new JobConfig()
                    .setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE)
                    .setName(CounterpartyVolume.class.getSimpleName())
                    .addClass(TradeJsonDeserializer.class)
                    .addClass(Trade.class)
                    .addClass(CounterpartyVolume.class);

            jet.newJobIfAbsent(createPipeline(servers), query1config);

        } finally {
            Jet.shutdownAll();
        }
    }

    private static Pipeline createPipeline(String servers) {
        Pipeline p = Pipeline.create();

        StreamStage<Trade> source =
                p.drawFrom(KafkaSources.<String, Trade, Trade>kafka(kafkaSourceProps(servers),
                        ConsumerRecord::value, TOPIC))
                 .withoutTimestamps();        

		StreamStage<Entry<String, Tuple3<Long, Long, Long>>> aggregated =
                source
                        .groupingKey(Trade::getCounterparty)
                        .rollingAggregate(allOf(
                                counting(),
                                summingLong(trade -> trade.getPrice() * trade.getQuantity()),
                                latestValue(trade -> trade.getTimestamp())
                        ))
                        
                        .setName("aggregate by trade volume");

        // write results to IMDG IMap
        aggregated.drainTo(Sinks.map("counterparty"));

        return p;
    }

    private static Properties kafkaSourceProps(String servers) {
        Properties props = new Properties();
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("bootstrap.servers", servers);
        props.setProperty("key.deserializer", StringDeserializer.class.getName());
        props.setProperty("value.deserializer", TradeJsonDeserializer.class.getName());
        return props;
    }

    private static <T, R> AggregateOperation1<T, ?, R> latestValue(FunctionEx<T, R> toValueFn) {
        return AggregateOperation.withCreate((SupplierEx<MutableReference<R>>) MutableReference::new)
                .<T>andAccumulate((ref, t) -> ref.set(toValueFn.apply(t)))
                .andExportFinish(MutableReference::get);
    }

}
