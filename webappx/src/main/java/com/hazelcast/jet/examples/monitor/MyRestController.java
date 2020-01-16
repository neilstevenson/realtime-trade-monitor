package com.hazelcast.jet.examples.monitor;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.IMap;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.datamodel.Tuple3;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MyRestController {
	
	private static final String NEWLINE = System.getProperty("line.separator");
	
	@Autowired
	private JetInstance jetInstance;

	@GetMapping("/")
	public String index() {
		log.info("index()");
		
		IMap<String, Tuple3<Long, Long, Integer>> query1_Results = this.jetInstance.getMap("query1_Results");

		Set<String> keys = new TreeSet<>(query1_Results.keySet());
		
		String htmlTable = 
				"<table>" + NEWLINE +
				" <tr><th>Stock</th><th>Count</th><th>Sum</th><th>Latest</th></tr>" + NEWLINE;
		
		for (String key : keys) {
			Tuple3<Long, Long, Integer> value = query1_Results.get(key);
			
			htmlTable += " <tr>";
			htmlTable += "<td>" + key + "</td>";
			htmlTable += "<td>" + value.f0() + "</td>";
			htmlTable += "<td>" + value.f1() + "</td>";
			htmlTable += "<td>" + value.f2() + "</td>";
			htmlTable += "</tr>" + NEWLINE;			
		}
		
		
		htmlTable += "</table>";
		
		return htmlTable;
	}

}
