package com.han.pwac.pinguins.backend.configuration;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.authentication.SessionFilter;
import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.services.*;
import com.han.pwac.pinguins.backend.services.contract.IFileService;
import com.han.pwac.pinguins.backend.services.contract.IStudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.core.context.SecurityContext;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserTokenService tokenService;
    private final IStudentService studentService;
    private final UserService userService;
    private final IFileService fileService;
    private final InviteKeyService inviteKeyService;
    private final BusinessService businessService;
    private final TeacherService teacherService;

    private static final String SUPERVISOR_ROLE = VerificationType.SUPERVISOR.name();
    private static final String STUDENT_ROLE = VerificationType.STUDENT.name();
    private static final String TEACHER_ROLE = VerificationType.TEACHER.name();
    private static final String INVALID = VerificationType.INVALID.name();

    @Value("${frontend.url}")
    private String frontendUrl;

    public SecurityConfig(UserTokenService tokenService, IStudentService studentService, UserService userService, IFileService fileService, InviteKeyService inviteKeyService, BusinessService businessService, TeacherService teacherService) {
        this.tokenService = tokenService;
        this.studentService = studentService;
        this.userService = userService;
        this.fileService = fileService;
        this.inviteKeyService = inviteKeyService;
      this.businessService = businessService;
      this.teacherService = teacherService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler(frontendUrl + "/home");

        security
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(new Http403ForbiddenEntryPoint()))
                .authorizeHttpRequests(
                        authentication -> authentication
                                .requestMatchers("/login/oauth2/code/**", "/logout", "/verify", "/webjars/**", "/files/**").permitAll()

                                .requestMatchers(HttpMethod.GET, "/invite").permitAll()
                                .requestMatchers(HttpMethod.GET, "/registrations/existing-user-registrations").hasAuthority(STUDENT_ROLE)
                                .requestMatchers(HttpMethod.POST, "/registrations/**").hasAuthority(STUDENT_ROLE)
                                .requestMatchers(HttpMethod.PUT, "/student/**").hasAuthority(STUDENT_ROLE)

                                .requestMatchers(HttpMethod.GET, "/registrations/{taskId}", "/students/email").hasAuthority(SUPERVISOR_ROLE)
                                .requestMatchers(HttpMethod.PUT, "/business/**").hasAuthority(SUPERVISOR_ROLE)
                                .requestMatchers(HttpMethod.GET, "/business/email/all").hasAuthority(SUPERVISOR_ROLE)
                                .requestMatchers(HttpMethod.POST, "/business").hasAuthority(TEACHER_ROLE)
                                .requestMatchers(HttpMethod.POST, "/projects/**", "/tasks/**").hasAuthority(SUPERVISOR_ROLE)
                                .requestMatchers(HttpMethod.PATCH, "/registrations/**").hasAuthority(SUPERVISOR_ROLE)
                                .requestMatchers(HttpMethod.POST, "/skills").hasAuthority(SUPERVISOR_ROLE)

                                .requestMatchers(HttpMethod.GET, "/teacher/**", "/invite/**").hasAuthority(TEACHER_ROLE)
                                .requestMatchers(HttpMethod.POST, "/invite/**").hasAnyAuthority(TEACHER_ROLE, SUPERVISOR_ROLE)

                                .requestMatchers(HttpMethod.PATCH, "/set-email").hasAuthority(INVALID)

                                //For future work related to the teachers
                                //.requestMatchers(HttpMethod.DELETE,  "/skills/**", "/business/**").hasAuthority(TEACHER_ROLE)

                                .anyRequest().hasAnyAuthority(SUPERVISOR_ROLE, STUDENT_ROLE, TEACHER_ROLE)
                )
                .httpBasic(Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(new SessionFilter(tokenService), BasicAuthenticationFilter.class)
                .logout(logout -> logout.logoutSuccessUrl("/logout"))
                .anonymous(AbstractHttpConfigurer::disable)
                .oauth2Login(o ->
                        o.failureHandler(this::onOAuthFailure)
                            .successHandler((request, response, authentication) ->
                                onOAuthAuthenticationSuccess(successHandler, request, response, authentication))
                );

        return security.build();
    }

    protected void onOAuthAuthenticationSuccess(SimpleUrlAuthenticationSuccessHandler successHandler, HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        Provider provider = Provider.valueOf(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId().toUpperCase());
        UserInfo userInfo = provider.getUserInfo(oAuth2AuthenticationToken.getPrincipal());

        Optional<User> user = userService.findByProviderId(userInfo.id());

        if (request.getSession().getAttribute("code") != null) {
            if (user.isPresent()) {
                // User already exists
                request.getSession().removeAttribute("code");
                response.sendRedirect(frontendUrl + "/?error=Je hebt al een account, log in zonder code");
                return;
            }

            String code = request.getSession().getAttribute("code").toString();

            InviteKey inviteKey;
            try {
                inviteKey = inviteKeyService.getInviteKeyFromCode(code);
            } catch (NotFoundException e) {
                request.getSession().removeAttribute("code");
                response.sendRedirect(frontendUrl + "/?error=" + e.getMessage());
                return;
            } catch (Exception e) {
                // If something goes wrong, delete the code, so it doesn't error the next time
                request.getSession().removeAttribute("code");
                response.sendRedirect(frontendUrl + "/?error=Er ging iets fout, probeer later opnieuw");
                return;
            }

            // User doesn't exist: Make a new one
            String avatar = userInfo.avatarUrl();
            URL url = new URL(avatar);
            String file = fileService.downloadFile(url);

            studentService.add(new StudentRepositoryDto(-1, userInfo.id(), userInfo.name(), "", new FileDto(Optional.of(file)), new FileDto(Optional.empty()), userInfo.email()));

            user = userService.findByProviderId(userInfo.id());

            if (inviteKey.getBusinessId() != null && inviteKey.getBusinessId() > 0) {
                // Create supervisor
                businessService.addUserToBusiness(inviteKey.getBusinessId(), user.get().getId());
                successHandler.setDefaultTargetUrl(frontendUrl + "/business/" + inviteKey.getBusinessId());
            } else {
                // Create teacher
                teacherService.addTeacher(user.get().getId());
            }
            inviteKeyService.deleteKey(inviteKey.getKey());
        } else if (user.isEmpty()) {
            // User doesn't exist: Make a new one
            String avatar = userInfo.avatarUrl();
            URL url = new URL(avatar);
            String file = fileService.downloadFile(url);

            studentService.add(new StudentRepositoryDto(-1, userInfo.id(), userInfo.name(), "", new FileDto(Optional.of(file)), new FileDto(Optional.empty()), userInfo.email()));
        }

        request.getSession().removeAttribute("code");

        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    protected void onOAuthFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String message = exception.getMessage();
        if (message.trim().toLowerCase().startsWith("[access_denied]")) {
            message = "Inloggen niet gelukt, ergens is de toegang geweigerd";
        } else {
            message = "Er ging iets fout bij het inloggen. Probeer het later opnieuw";
        }
        response.sendRedirect(frontendUrl + "/?error=" + message);
    }

    @Bean
    public FilterRegistrationBean<FrameOptionsFilter> frameOptionsFilterRegistration() {
        FilterRegistrationBean<FrameOptionsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new FrameOptionsFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}