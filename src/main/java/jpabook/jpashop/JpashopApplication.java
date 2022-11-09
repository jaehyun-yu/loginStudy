package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpashopApplication {
	public static void main(String[] args) {

		int[] nums ={1,2,3,1};
		int k = 3;
		boolean answer = false;
		for(int i = 0; i < nums.length; i++) {
			for(int j = i+1; j < nums.length; j++) {
				if(nums[i] == nums[j] && Math.abs(nums[i]-nums[j]) <= k){
					answer = true;
				} else {
					answer = false;
				}
			}
		}
		System.out.println(answer);
		SpringApplication.run(JpashopApplication.class, args);
	}

}
