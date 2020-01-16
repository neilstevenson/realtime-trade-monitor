import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;

public class StartServer {

    public static void main(String[] args) {
    	JetInstance jetInstance = Jet.newJetInstance();
       	jetInstance.getMap("counterparty");
        jetInstance.getMap("jsessionid");
    	jetInstance.getMap("query1_Results");
    	jetInstance.getMap("symbols");
    	jetInstance.getMap("trades");
    }
}
