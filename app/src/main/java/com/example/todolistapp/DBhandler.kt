package com.example.todolistapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBhandler(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    private val table_name = "task_table"


    override fun onCreate(db: SQLiteDatabase?) {
        val qry1 = "CREATE TABLE " + table_name + " (title VARCHAR(50), " +
                "description VARCHAR, pendingStatus BOOLEAN, dueDate VARCHAR)"
        db!!.execSQL(qry1)
    }



    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + table_name)
        onCreate(db)
    }



    fun insertTask(taskTitle: String, taskDescription: String, taskPendingStatus: Boolean, taskDueDate: String) {
        val record = ContentValues()
        record.put("title", taskTitle)
        record.put("description", taskDescription)
        record.put("pendingStatus", taskPendingStatus)
        record.put("dueDate", taskDueDate)

        val db = this.writableDatabase
        db.insert(table_name, null, record)
        db.close()
    }



    fun retrieveTasks(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM "+ table_name,null)
    }



    fun updateTitle(newTitle: String, task: Task) {
        val db = this.writableDatabase

        val cv = ContentValues()
        cv.put("title", newTitle)

        val clause = "title = ? AND description = ? AND pendingStatus = ? AND dueDate = ?"

        val args = arrayOf(
            task.title,
            task.description,
            if (task.pendingStatus) "1" else "0",
            task.dueDate
        )

        db.update(table_name, cv, clause, args)
    }



    fun updateDescription(newDescription: String, task: Task) {
        val db = this.writableDatabase

        val cv = ContentValues()
        cv.put("description", newDescription)

        val clause = "title = ? AND description = ? AND pendingStatus = ? AND dueDate = ?"

        val args = arrayOf(
            task.title,
            task.description,
            if (task.pendingStatus) "1" else "0",
            task.dueDate
        )

        db.update(table_name, cv, clause, args)
    }



    fun toggle(task: Task) {
        val db = this.writableDatabase

        val newStatus = !task.pendingStatus
        val cv = ContentValues()
        cv.put("pendingStatus", if (newStatus) 1 else 0)

        val clause = "title = ? AND description = ? AND pendingStatus = ? AND dueDate = ?"

        val args = arrayOf(
            task.title,
            task.description,
            if (task.pendingStatus) "1" else "0",
            task.dueDate
        )

        db.update(table_name, cv, clause, args)
    }



    fun deleteTask(task: Task) {
        val db = this.writableDatabase

        db.use {
            val clause = "title = ? AND description = ? AND pendingStatus = ? AND dueDate = ?"
            val args = arrayOf(
                task.title,
                task.description,
                if (task.pendingStatus) "1" else "0",
                task.dueDate
            )

            db.delete(table_name, clause, args)
        }
    }
}