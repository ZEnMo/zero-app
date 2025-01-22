package com.zenmo.orm.user.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType

object UserTable: Table("user") {
    /**
     * ID from Keycloak.
     * This can be a human user or a service account associated with OAuth client credentials.
     */
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    /**
     * Set the users name here for convenience.
     * This is mostly because there is no GUI yet and no logic to display the name from Keycloak.
     */
    val note = varchar("note", 255).default("")

     /**
     * Admin flag to determine if the user has admin privileges.
     */
    val isAdmin = bool("is_admin").default(false)
}
