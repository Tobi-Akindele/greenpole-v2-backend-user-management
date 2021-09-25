package com.ap.greenpole.usermodule.controller;

import com.ap.greenpole.usermodule.annotation.PreAuthorizePermission;
import com.ap.greenpole.usermodule.model.GenericResponse;
import com.ap.greenpole.usermodule.model.MultipleValuesRequest;
import com.ap.greenpole.usermodule.model.Role;
import com.ap.greenpole.usermodule.model.User;
import com.ap.greenpole.usermodule.repository.PermissionRepository;
import com.ap.greenpole.usermodule.repository.RoleRepository;
import com.ap.greenpole.usermodule.util.Helpers;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

/**
 * @author Adewale Azeez <azeezadewale98@gmail.com>
 * @date 18-Aug-20 04:58 AM
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    RoleRepository roleRepository;

    @PreAuthorizePermission({"PERMISSION_VIEW_ROLE"})
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll() {
        List<Role> roles = roleRepository.findAll();
        return new ResponseEntity<>(new GenericResponse<>("00", "", roles), HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_VIEW_ROLE"})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable(value = "id") long userId) {
        Optional<Role> roleResult = roleRepository.findById(userId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No role with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new GenericResponse<>("00", "", roleResult.get()), HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_CREATE_ROLE"})
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody MultipleValuesRequest multipleValuesRequest) {
        for (String value : multipleValuesRequest.getValues()) {
            Optional<Role> roleResult = roleRepository.findByValue(value);
            if (!roleResult.isPresent()) {
                Role role = new Role();
                role.setValue(value);
                roleRepository.saveAndFlush(role);
            }
        }

        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully added the new roles", null),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_MODIFY_ROLE"})
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable(value = "id") long roleId, @Valid @RequestBody Role role) {
        Optional<Role> roleResult = roleRepository.findById(roleId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "The role does not exist", ""),
                    HttpStatus.NOT_FOUND);
        }
        role.setId(roleResult.get().getId());
        role = roleRepository.saveAndFlush(role);
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully updated the role", role),
                HttpStatus.OK);
    }

    @PreAuthorizePermission({"PERMISSION_MODIFY_ROLE"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable(value = "id") long roleId) {
        Optional<Role> roleResult = roleRepository.findById(roleId);
        if (!roleResult.isPresent()) {
            return new ResponseEntity<>(
                    new GenericResponse<>("01", "No role with the specified id found", ""),
                    HttpStatus.NOT_FOUND);
        }
        roleRepository.deleteById(roleId);
        return new ResponseEntity<>(
                new GenericResponse<>("00", "Successfully deleted the role", roleResult.get()),
                HttpStatus.OK);
    }

}
