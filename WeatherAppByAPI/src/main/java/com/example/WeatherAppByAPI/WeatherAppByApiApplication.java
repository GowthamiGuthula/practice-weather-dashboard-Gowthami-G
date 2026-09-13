package com.example.WeatherAppByAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class WeatherAppByApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherAppByApiApplication.class, args);
	}
System.out.println("1 - New York");
        System.out.println("2 - St Louis");
        System.out.println("3 - Key West");

        for (int i = 1; i <= 5; i++) {
		//TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
		// for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
		System.out.println("i = " + i);
	}

	Scanner scanner = new Scanner(System.in);
        System.out.print("Select City: ");
	String name = scanner.nextLine();
        System.out.println("Hello, " + name + "!");

}
