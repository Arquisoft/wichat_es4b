package com.uniovi.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.uniovi.components.CustomAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/player/admin/**")
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/css/**", "/img/**", "/script/**").permitAll()
                                .requestMatchers("/home/**").authenticated()
                                .requestMatchers("/signup/**").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/game/**").authenticated()
                                .requestMatchers("/multiplayerGame/**").authenticated()
                                .requestMatchers("/startMultiplayerGame/**", "/endGameList/**").authenticated()
                                .requestMatchers("/lobby/**").authenticated()
                                .requestMatchers("/ranking/playerRanking").authenticated()
                                .requestMatchers("/player/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/**").permitAll()
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .failureHandler(customAuthenticationFailureHandler)
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/home")
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                );
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * Method to check if the user is authenticated
     * @return boolean
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }
}
