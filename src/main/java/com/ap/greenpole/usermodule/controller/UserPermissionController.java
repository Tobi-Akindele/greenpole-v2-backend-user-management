package com.ap.greenpole.usermodule.controller;

import com.ap.greenpole.usermodule.annotation.PreAuthorizePermission;
import com.ap.greenpole.usermodule.model.*;
import com.ap.greenpole.usermodule.repository.PermissionRepository;
import com.ap.greenpole.usermodule.repository.UserPermissionRepository;
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
@RequestMapping("/user_permission")
public class UserPermissionController {

    @Autowired
    UserPermissionRepository userPermissionRepository;

    @Autowired
    PermissionRepository permissionRepository;
    
    @Autowired
    UserService userService;

    @PreAuthorizePermission({"PERMISSION_VIEW_USER_PERMISSION"})
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestParam(value = "userId") long userId) {
        List<UserPermission> roles = userPermissionRepository.findAllByUserId(userId);
        return new ResponseEntity<>(new GenericResponse<>("00", "", roles), HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_VIEW_USER_PERMISSION"})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable(value = "id") long userId) {
        Optional<UserPermission> roleResult = userPermissionRepository.findById(userId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No permission with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new GenericResponse<>("00", "", roleResult.get()), HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_ASSIGN_USER_PERMISSION"})
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
                    new GenericResponse<>("01", "You cannot assign a permission to this user, the user is yet to set a password", ""),
                    HttpStatus.OK);
        }
        StringBuilder msg = new StringBuilder();
        for (String value : multipleValuesRequest.getValues()) {
            Optional<UserPermission> roleResult = userPermissionRepository.findByValueAndUserId(value, user.get().getId());
            if (!roleResult.isPresent()) {
                Optional<Permission> role = permissionRepository.findByValue(value);
                if (role.isPresent()) {
                    UserPermission userRole = new UserPermission();
                    userRole.setUserId(user.get().getId());
                    userRole.setPermissionId(role.get().getId());
                    userRole.setValue(value);
                    userPermissionRepository.saveAndFlush(userRole);
                } else{
                    msg.append("The permission ").append(value).append(" does not exist, ");
                }
            }
        }

        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully added the new permission for the user", msg.toString()),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_MODIFY_USER_PERMISSION"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable(value = "id") long roleId) {
        Optional<UserPermission> roleResult = userPermissionRepository.findById(roleId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No permission with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        userPermissionRepository.deleteById(roleId);
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully deleted the permission", roleResult.get()),
                HttpStatus.OK);
    }

}
