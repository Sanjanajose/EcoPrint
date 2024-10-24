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
package com.ecoprint.printmanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
<<<<<<< HEAD
=======
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
>>>>>>> 982c1c6 (Initial commit)
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecoprint.printmanagement.security.JwtAuthenticationEntryPoint;
import com.ecoprint.printmanagement.security.JwtAuthenticationFilter;
import com.ecoprint.printmanagement.service.CustomUserDetailsService;

@Profile("!dev")
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

	private final CustomUserDetailsService userDetailsService;

	private final JwtAuthenticationEntryPoint jwtEntryPoint;

	@Autowired
	public WebSecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationEntryPoint jwtEntryPoint) {
		this.userDetailsService = userDetailsService;
		this.jwtEntryPoint = jwtEntryPoint;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

<<<<<<< HEAD
		http.csrf().disable()
				.authorizeHttpRequests(authorizeRequests -> authorizeRequests.requestMatchers("/swagger-ui/**")
						.permitAll().requestMatchers("/v3/api-docs/**").permitAll()
						.requestMatchers("/swagger-resources/**").permitAll().requestMatchers("/swagger-resources")
						.permitAll().requestMatchers("/api/auth/**").permitAll().anyRequest().authenticated())
				.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtEntryPoint))
				.sessionManagement(
						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();

	};
=======
	    http.csrf(csrf -> csrf.disable())
	            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
	                .requestMatchers("/swagger-ui/**").permitAll()
	                .requestMatchers("/v3/api-docs/**").permitAll()
	                .requestMatchers("/swagger-resources/**").permitAll()
	                .requestMatchers("/swagger-resources").permitAll()
	                .requestMatchers("/api/auth/**").permitAll()
	                .requestMatchers("/h2-console/**").permitAll()
	                .anyRequest().authenticated()
	            )
	            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtEntryPoint))
	            .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	            .headers(header -> header.frameOptions(frame -> frame.sameOrigin()));

	    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

>>>>>>> 982c1c6 (Initial commit)

	@Bean
	public AuthenticationManager authManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		return authenticationManagerBuilder.build();
	}
<<<<<<< HEAD
=======
	
	//@Bean
	//public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
	  //  return authenticationConfiguration.getAuthenticationManager();
	//}

	
	
>>>>>>> 982c1c6 (Initial commit)

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public void configure(WebSecurity web) {
		web.ignoring().requestMatchers("/v3/api-docs", "/configuration/ui/", "/swagger-resources/**",
				"/configuration/**", "/swagger-ui/**", "/webjars/**");
	}
}
