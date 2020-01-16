import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sources;

import java.util.Map.Entry;

public class ExcelExport {

    public static void excelExport(JetInstance jet) {
        try {
            JobConfig jobConfig = new JobConfig()
                    .setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE)
                    .setName(ExcelExport.class.getSimpleName())
                    .addClass(ExcelExport.class)
            		.addClass(ExcelSink.class)
            		;

            jet.newJobIfAbsent(createPipeline(), jobConfig);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private static Pipeline createPipeline() {
        Pipeline p = Pipeline.create();
        
        long now = System.currentTimeMillis();

        BatchStage<Entry<String, Tuple3<Long, Long, Integer>>> source =
        		p.drawFrom(Sources.map("query1_Results"));

        source.drainTo(ExcelSink.buildExcelSink(now));

        return p;
    }

}
