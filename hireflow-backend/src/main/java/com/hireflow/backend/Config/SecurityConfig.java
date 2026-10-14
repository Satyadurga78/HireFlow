
package com.hireflow.backend.config;

import com.hireflow.backend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.DispatcherType;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                Arrays.asList(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                Arrays.asList(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow Spring Boot error dispatches
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        // Allow CORS preflight requests
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Public APIs
                        .requestMatchers(
                                "/api/users",
                                "/api/auth/login"
                        ).permitAll()

                        // Candidate resume upload
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/resume"
                        ).hasRole("CANDIDATE")

                        // Candidate application APIs
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/applications/**"
                        ).hasRole("CANDIDATE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/my-applications"
                        ).hasRole("CANDIDATE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/applications/**"
                        ).hasRole("CANDIDATE")

                        // Recruiter applicant APIs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/job/**"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/all"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/applications/*/status"
                        ).hasRole("RECRUITER")

                        // Recruiter: view candidate photo
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/*/candidate-photo"
                        ).hasRole("RECRUITER")

                        // Interviews: recruiters schedule, both roles can view their own
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/interviews"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/interviews"
                        ).hasAnyRole("CANDIDATE", "RECRUITER")

                        // Recruiter job creation
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/jobs"
                        ).hasRole("RECRUITER")

                        // Authenticated users can view jobs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/jobs"
                        ).authenticated()

                        // Candidate and Recruiter profile details
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/profile"
                        ).hasAnyRole("CANDIDATE", "RECRUITER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/profile"
                        ).hasAnyRole("CANDIDATE", "RECRUITER")

                        // Profile photo upload
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/profile/photo"
                        ).hasAnyRole("CANDIDATE", "RECRUITER")

                        // Profile photo retrieval
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/profile/photo"
                        ).hasAnyRole("CANDIDATE", "RECRUITER")

                        // Profile photo removal
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/profile/photo"
                        ).hasAnyRole("CANDIDATE", "RECRUITER")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}