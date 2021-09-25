package com.ap.greenpole.usermodule.controller;

import com.ap.greenpole.usermodule.annotation.PreAuthorizePermission;
import com.ap.greenpole.usermodule.model.GenericResponse;
import com.ap.greenpole.usermodule.model.MultipleValuesRequest;
import com.ap.greenpole.usermodule.model.Permission;
import com.ap.greenpole.usermodule.model.Role;
import com.ap.greenpole.usermodule.repository.PermissionRepository;
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
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    PermissionRepository permissionRepository;

    @PreAuthorizePermission({"PERMISSION_VIEW_PERMISSION"})
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll() {
        List<Permission> roles = permissionRepository.findAll();
        return new ResponseEntity<>(new GenericResponse<>("00", "", roles), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable(value = "id") long userId) {
        Optional<Permission> roleResult = permissionRepository.findById(userId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No permission with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new GenericResponse<>("00", "", roleResult.get()), HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_CREATE_PERMISSION"})
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody MultipleValuesRequest multipleValuesRequest) {
        for (String value : multipleValuesRequest.getValues()) {
            Optional<Permission> permissionResult = permissionRepository.findByValue(value);
            if (!permissionResult.isPresent()) {
                Permission permission = new Permission();
                permission.setValue(value);
                permissionRepository.saveAndFlush(permission);
            }
        }
        
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully added the new permissions", null),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_MODIFY_PERMISSION"})
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable(value = "id") long roleId, @Valid @RequestBody Permission permission) {
        Optional<Permission> roleResult = permissionRepository.findById(roleId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The permission does not exist", ""),
                    HttpStatus.NOT_FOUND);
        }
        permission.setId(roleResult.get().getId());
        permission = permissionRepository.saveAndFlush(permission);
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully updated the permission", permission),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_MODIFY_PERMISSION"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable(value = "id") long roleId) {
        Optional<Permission> roleResult = permissionRepository.findById(roleId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No permission with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        permissionRepository.deleteById(roleId);
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully deleted the permission", roleResult.get()),
                HttpStatus.OK);
    }

}
