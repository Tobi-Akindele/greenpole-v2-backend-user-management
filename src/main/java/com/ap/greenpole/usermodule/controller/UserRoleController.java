package com.ap.greenpole.usermodule.controller;

import com.ap.greenpole.usermodule.annotation.PreAuthorizePermission;
import com.ap.greenpole.usermodule.model.*;
import com.ap.greenpole.usermodule.repository.PermissionRepository;
import com.ap.greenpole.usermodule.repository.RoleRepository;
import com.ap.greenpole.usermodule.repository.UserPermissionRepository;
import com.ap.greenpole.usermodule.repository.UserRoleRepository;
import com.ap.greenpole.usermodule.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * @author Adewale Azeez <azeezadewale98@gmail.com>
 * @date 18-Aug-20 04:58 AM
 */
@RestController
@RequestMapping("/user_role")
public class UserRoleController {

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    UserPermissionRepository userPermissionRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    UserService userService;

    @PreAuthorizePermission({"PERMISSION_VIEW_USER_ROLE"})
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestParam(value = "userId") long userId) {
        List<UserRole> roles = userRoleRepository.findAllByUserId(userId);
        return new ResponseEntity<>(new GenericResponse<>("00", "", roles), HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_VIEW_USER_ROLE"})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable(value = "id") long userId) {
        Optional<UserRole> roleResult = userRoleRepository.findById(userId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No role with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new GenericResponse<>("00", "", roleResult.get()), HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_ASSIGN_USER_ROLE"})
    @RequestMapping(value = "/assign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody MultipleValuesRequest multipleValuesRequest) {
        Optional<User> user = userService.userRepository.findById(multipleValuesRequest.getUserId());
        if (!user.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "Unable to find a user with the specified userId", ""),
                    HttpStatus.FORBIDDEN);
        }
        if (user.get().isFirstTimeLogin()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "You cannot assign a new role to the user, the user is yet to set a password", ""),
                    HttpStatus.OK);
        }
        StringBuilder msg = new StringBuilder();
        for (String value : multipleValuesRequest.getValues()) {
            Optional<UserRole> roleResult = userRoleRepository.findByValueAndUserId(value, user.get().getId());
            if (!roleResult.isPresent()) {
                Optional<Role> role = roleRepository.findByValue(value);
                if (role.isPresent()) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.get().getId());
                    userRole.setRoleId(role.get().getId());
                    userRole.setValue(value);
                    userRoleRepository.saveAndFlush(userRole);
                    if (role.get().getValue().equals("ADMIN")) {
                        List<Permission> permissions = permissionRepository.findAll();
                        for (Permission permission : permissions) {
                            UserPermission userPermission = new UserPermission();
                            userPermission.setPermissionId(permission.getId());
                            userPermission.setUserId(user.get().getId());
                            userPermission.setValue(permission.getValue());
                            userPermissionRepository.save(userPermission);
                        }
                    }
                } else{
                    msg.append("The role ").append(value).append(" does not exist, ");
                }
            }
        }

        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully added the new roles", msg.toString()),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_MODIFY_USER_ROLE"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable(value = "id") long roleId) {
        Optional<UserRole> roleResult = userRoleRepository.findById(roleId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No role with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        userRoleRepository.deleteById(roleId);
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully deleted the role", roleResult.get()),
                HttpStatus.OK);
    }

}
