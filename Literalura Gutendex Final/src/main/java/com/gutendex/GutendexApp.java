package com.gutendex;

import com.gutendex.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GutendexApp implements CommandLineRunner {

    @Autowired
    private MenuService menuService;

    public static void main(String[] args) {
        SpringApplication.run(GutendexApp.class, args);
    }

    @Override
    public void run(String... args) {
        menuService.mostrarMenu();
    }
}
