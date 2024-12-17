package com.example.todolistapp

data class Task (
    var title: String,
    var description: String,
    var pendingStatus: Boolean,
    var dueDate: String,
)