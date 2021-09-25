package com.ap.greenpole.usermodule.controller;

import com.ap.greenpole.usermodule.annotation.PreAuthorizePermission;
import com.ap.greenpole.usermodule.model.*;
import com.ap.greenpole.usermodule.service.EmailVerificationService;
import com.ap.greenpole.usermodule.service.UserService;
import com.ap.greenpole.usermodule.util.JwtTokenUtil;
import io.github.thecarisma.FatalObjCopierException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Adewale Azeez <azeezadewale98@gmail.com>
 * @date 13-Aug-20 03:46 AM
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    EmailVerificationService emailVerificationService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PreAuthorizePermission({"PERMISSION_VIEW_USER"})
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> viewUser(@PathVariable long id) {
        Optional<User> user = userService.getUserById(id);
        if (!user.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No user with the id '" + id + "' found", ""),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new GenericResponse<>("00", "", user.get()),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_VIEW_USER"})
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listUsers(Pageable pageable) {
        Page<User> users = userService.listUsers(pageable);
        return new ResponseEntity<>(new GenericResponse<>("00", "", users),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody User.Request requestBody) throws NoSuchAlgorithmException {
        Optional<User> user = userService.authenticateUser(requestBody);
        if (!user.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The email and password does not exist in the system", requestBody),
                    HttpStatus.UNAUTHORIZED);
        }
        User.Response response = userService.getAuthorizedUserResponse(user.get());
        return new ResponseEntity<>(new GenericResponse<>("00", response.getMessage(), response),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/register_admin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody User user) {
        if (userService.emailExisted(user.getEmail())) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The email address has been used", ""),
                    HttpStatus.CONFLICT);
        }
        boolean success = userService.createNewAdmin(user);
        if (!success) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to register new admin at this time, confirm the adminRegisterKey is valid", ""),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(
                new GenericResponse<>("00", "The administrator has been successfully registered", ""),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_CREATE_USER"})
    @RequestMapping(value = "/register_user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userService.emailExisted(user.getEmail())) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The email address has been used", ""),
                    HttpStatus.CONFLICT);
        }
        boolean success = userService.createNewUser(user);
        if (!success) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to register new user at this time", ""),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(
                new GenericResponse<>("00", "The user has been successfully registered", ""),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_SUPER_USER,ROLE_ADMIN')")
    @RequestMapping(value = "/profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> viewProfile(@RequestHeader("Authorization") String authorization) {
        Optional<User> userResult = userService.memberFromAuthorization(authorization);
        if (!userResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to find member account. Ensure you are logged into the system", ""),
                    HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(new GenericResponse<>("00", "", userResult.get()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_SUPER_USER,ROLE_ADMIN')")
    @RequestMapping(value = "/profile", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> viewProfile(@RequestHeader("Authorization") String authorization, @RequestBody User user) throws FatalObjCopierException {
        Optional<User> userResult = userService.memberFromAuthorization(authorization);
        if (!userResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to find member account. Ensure you are logged into the system", ""),
                    HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(new GenericResponse<>("00", "", userService.updateUser(userResult.get(), user)), HttpStatus.OK);
    }

    @RequestMapping(value = "/send_password_reset_email", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> requestPasswordChangeEmail(@RequestParam(value = "email") String email,
                                                    @RequestParam(value = "prefix_url") String prefixUrl) throws NoSuchAlgorithmException, UnsupportedEncodingException, MessagingException {
        Optional<User> userResult = userService.getMemberByEmail(email);
        if (!userResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to find member account. Ensure you the email address is correct", null),
                    HttpStatus.FORBIDDEN);
        }
        userService.sendPasswordResetEmail(userResult.get(), prefixUrl);
        return new ResponseEntity<>(new GenericResponse<>("00", "The password reset detail has been sent to your email", null),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPassword resetPassword) {
        Optional<EmailVerification> emailVerification = emailVerificationService.getByToken(resetPassword.getToken());
        if (!emailVerification.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The password reset token is invalid", null),
                    HttpStatus.NOT_FOUND);
        }
        if (emailVerificationService.hasTokenExpired(resetPassword.getToken())) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The verification token has expired", null),
                    HttpStatus.FORBIDDEN);
        }
        if (emailVerification.get().isTokenUsed()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The verification token has already been used", null),
                    HttpStatus.ALREADY_REPORTED);
        }
        Optional<User> user = userService.userRepository.findById(emailVerification.get().getUserId());
        if (!user.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to find member account", null),
                    HttpStatus.FORBIDDEN);
        }
        if (!userService.passwordAdhereToPolicy(resetPassword.getPassword())) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The password does not adhere to the system policy", null),
                    HttpStatus.BAD_REQUEST);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.get().setPassword(passwordEncoder.encode(resetPassword.getPassword()));
        userService.userRepository.saveAndFlush(user.get());
        emailVerificationService.changeVerificationStatus(emailVerification.get(), true);
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Your password has been reset successfully", null),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/change_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authorization,
                                            @Valid @RequestBody ChangePassword changePassword) {
        Optional<User> userResult = userService.memberFromAuthorization(authorization);
        if (!userResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to find member account. Ensure you are logged into the system", ""),
                    HttpStatus.FORBIDDEN);
        }
        if (!userService.passwordAdhereToPolicy(changePassword.getNewPassword())) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The password does not adhere to the system policy", ""),
                    HttpStatus.BAD_REQUEST);
        }

        User.Request requestBody = new User.Request();
        requestBody.setPassword(changePassword.getOldPassword());
        requestBody.setEmail(userResult.get().getEmail());
        Optional<User> user = userService.authenticateUser(requestBody);
        if (!user.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The password does not match for any user in the system", ""),
                    HttpStatus.UNAUTHORIZED);
        }
        User.Response resp = userService.changePassword(userResult.get(), changePassword);
        return new ResponseEntity<>(new GenericResponse<>("00", "", resp),
                HttpStatus.OK);
    }

}
