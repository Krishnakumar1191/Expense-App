package com.example.expense.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "userId"]
)
data class GroupMemberEntity(
    val groupId: String,
    val userId: String
)
