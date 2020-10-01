package net.virtela.console;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class TestRunner {
	
	static ForkJoinPool pool = new ForkJoinPool(5);

	public static void main(String[] args) {
		
		List<Integer> numList = new ArrayList<>();
		numList.add(1);
		numList.add(2);
		numList.add(3);
		numList.add(4);
		numList.add(5);
		numList.add(6);
		numList.add(7);
		numList.add(8);
		numList.add(9);
		numList.add(10);
		
		pool.submit(() -> 
		numList.parallelStream()
		       .forEach(x -> {
		    	   try {
					TimeUnit.SECONDS.sleep(1);
					System.out.println(x);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		       } )).quietlyJoin();
		
		pool.shutdown();

	}

}
