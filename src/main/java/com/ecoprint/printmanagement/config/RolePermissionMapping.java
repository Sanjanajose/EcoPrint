package com.ecoprint.printmanagement.config;
import java.util.EnumMap;
import java.util.Set;

import com.ecoprint.printmanagement.model.Permission;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;

import java.util.EnumSet;


public class RolePermissionMapping {
	
	public static final EnumMap<RoleName, Set<Permission>> rolePermissions = new EnumMap<>(RoleName.class);

    static {
        rolePermissions.put(RoleName.ROLE_ADMIN, EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.MANAGE_USERS,
                Permission.MANAGE_TECH_TASKS,
                Permission.VIEW_TECH_INFO,
                Permission.MANAGE_ROLES,
                Permission.ACCESS_PUBLIC_DATA,
                Permission.LOGIN));

        rolePermissions.put(RoleName.ROLE_TECHNICIAN, EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.MANAGE_TECH_TASKS,
                Permission.VIEW_TECH_INFO,
                Permission.LOGIN));

        rolePermissions.put(RoleName.ROLE_USER, EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.ACCESS_PUBLIC_DATA,
                Permission.LOGIN));

        rolePermissions.put(RoleName.ROLE_GUESTUSER, EnumSet.of(
                Permission.ACCESS_PUBLIC_DATA,
                Permission.LOGIN));
    }

}
