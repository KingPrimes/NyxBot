package com.nyx.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NxyBotApplication {
    /*
    * There is no tracking information for the current branch.
fatal: 'irigin' does not appear to be a git repository
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.


    * */

    public static void main(String[] args) {
        SpringApplication.run(NxyBotApplication.class, args);
    }

}
