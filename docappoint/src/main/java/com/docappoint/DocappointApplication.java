package com.docappoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class DocappointApplication {

    public static void main(String[] args) {

        System.out.println("DATABASE_URL present: " +
                System.getenv("DATABASE_URL") != null &&
                !System.getenv("DATABASE_URL").isBlank());

        System.out.println("DATABASE_USERNAME present: " +
                System.getenv("DATABASE_USERNAME") != null &&
                !System.getenv("DATABASE_USERNAME").isBlank());

        System.out.println("DATABASE_PASSWORD present: " +
                System.getenv("DATABASE_PASSWORD") != null &&
                !System.getenv("DATABASE_PASSWORD").isBlank());

        System.out.println("DOCTOR_REGISTER_SECRET present: " +
                System.getenv("DOCTOR_REGISTER_SECRET") != null &&
                !System.getenv("DOCTOR_REGISTER_SECRET").isBlank());

        System.out.println("SECRET_KEY present: " +
                System.getenv("SECRET_KEY") != null &&
                !System.getenv("SECRET_KEY").isBlank());

        SpringApplication.run(DocappointApplication.class, args);
    }
}