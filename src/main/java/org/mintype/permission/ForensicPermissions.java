package org.mintype.permission;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public final class ForensicPermissions {

    public static final Permission INSPECT =
            new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);

    private ForensicPermissions() {}
}