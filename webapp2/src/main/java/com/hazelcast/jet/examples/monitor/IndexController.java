package com.hazelcast.jet.examples.monitor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;

@Controller
public class IndexController {

	private final DecimalFormat decimalFormatter = new DecimalFormat("###,###,###,###,###,###");
	
	@Autowired
	private HazelcastInstance hazelcastInstance;
	
	@GetMapping("/")
    public ModelAndView index(HttpSession httpSession, HttpServletRequest httpServletRequest) {
		System.out.printf("index(), session=%s%n", httpSession.getId());

        IMap<String, Tuple3<Long,Long,Long>> counterpartyMap = 
        		this.hazelcastInstance.getMap("counterparty");

        ModelAndView modelAndView = new ModelAndView("index");

        Set<String> keys = 
                        counterpartyMap.keySet()
                        .stream()
                        .collect(Collectors.toCollection(TreeSet::new));

        List<List<String>> data = new ArrayList<>();
        
        for (String key : keys) {
        	Tuple3<Long,Long,Long> value = counterpartyMap.get(key);	
        	
        	List<String> datum = new ArrayList<>();
        	
        	datum.add(key);
        	datum.add(decimalFormatter.format(value.f0()));
        	datum.add(decimalFormatter.format(value.f1()));
        	datum.add(String.valueOf(value.f2()));

        	data.add(datum);
        }
                
        modelAndView.addObject("data", data);
                
        return modelAndView;
    }

	@GetMapping("/index2")
    public ModelAndView index2(HttpSession httpSession, HttpServletRequest httpServletRequest) {
		String j_counterparty_name = httpServletRequest.getParameter("j_counterparty_name");
        if (j_counterparty_name==null) {
        	j_counterparty_name="";
        }
		String j_counterparty_timestamp = httpServletRequest.getParameter("j_counterparty_timestamp");
        if (j_counterparty_timestamp==null) {
        	j_counterparty_timestamp="";
        }
		
		System.out.printf("index2(), session=%s name=%s, timestamp=%s%n", 
				httpSession.getId(), j_counterparty_name, j_counterparty_timestamp);

        IMap<String, HazelcastJsonValue> tradesMap = 
        		this.hazelcastInstance.getMap("trades");

        ModelAndView modelAndView = new ModelAndView("index2");
        modelAndView.addObject("j_counterparty_name", j_counterparty_name);
        Date fromDate =  new Date(Long.parseLong(j_counterparty_timestamp));
        modelAndView.addObject("j_counterparty_timestamp", fromDate.toString());

        List<String> data = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
		Predicate<String, HazelcastJsonValue> predicate = 
        		new SqlPredicate("counterparty = '" +  j_counterparty_name + "' AND timestamp > " + j_counterparty_timestamp);

        long before = System.currentTimeMillis();
        Collection<HazelcastJsonValue> records = tradesMap.values(predicate);
        long after = System.currentTimeMillis();

        String j_elapsed;
        if (after == before) {
            j_elapsed = "Retrieved " + records.size() + " in 0 seconds";
        } else {
            j_elapsed = "Retrieved " + records.size() + " in " + decimalFormatter.format((after - before)/1000) + " seconds";
        }
        modelAndView.addObject("j_elapsed", j_elapsed);
        
        records.stream().forEach(hazelcastJsonValue -> data.add(hazelcastJsonValue.toString()));
        
        modelAndView.addObject("data", data);
                
        return modelAndView;
    }

}
