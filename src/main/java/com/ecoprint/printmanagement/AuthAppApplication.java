/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ecoprint.printmanagement;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ecoprint.printmanagement.service.RoleService;

import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.ecoprint.printmanagement") // Scans this package and sub-packages
@EnableAsync
@EnableJpaRepositories(basePackages = "com.ecoprint.printmanagement.repository")
@EnableScheduling
public class AuthAppApplication {
	private final RoleService roleService ;
	
	public AuthAppApplication(RoleService roleService) {
        this.roleService = roleService;
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthAppApplication.class, args);
    }

    //@Bean
    //CommandLineRunner initRoles() {
      //  return args -> roleService.initializeRoles();
    //}
}







    