package com.example.todolistapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat", "NotifyDataSetChanged", "SetTextI18n", "UseSwitchCompatOrMaterialCode")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : AppCompatActivity() {
    private val tasks = mutableListOf<Task>()
    private lateinit var taskList: RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    private lateinit var addTaskButton: Button

    private val db = DBhandler(this,"task_db",null, 1)

    private val filter = InputFilter.LengthFilter(15)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        taskList = findViewById(R.id.taskList)
        taskAdapter = TaskAdapter(tasks) { task, taskAction ->
            handleTaskAction(task, taskAction)
        }
        taskList.layoutManager = LinearLayoutManager(this)
        taskList.adapter = this.taskAdapter

        displayScheduledTasks()

        addTaskButton = findViewById(R.id.addTask)
        addTaskButton.setOnClickListener {
            addNewTask()
        }
    }



    private fun displayScheduledTasks() {

        val cur = db.retrieveTasks()

        if (cur.moveToNext())
        {
            do
            {
                val title = cur.getString(cur.getColumnIndexOrThrow("title"))
                val description = cur.getString(cur.getColumnIndexOrThrow("description"))
                val pendingStatus = cur.getInt(cur.getColumnIndexOrThrow("pendingStatus")) == 1
                val dueDate = cur.getString(cur.getColumnIndexOrThrow("dueDate"))

                tasks.add(Task(title, description, pendingStatus, dueDate))
            }
            while (cur.moveToNext())
            cur.close()

            tasks.sortBy { it.dueDate }
            taskAdapter.notifyDataSetChanged()
        }
    }



    private fun addNewTask() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Task")


        val titleInput = EditText(this)
        titleInput.hint = "Title"
        titleInput.filters = arrayOf(filter)

        val descriptionInput = EditText(this)
        descriptionInput.hint = "Description"

        val dueDateInput = EditText(this)
        dueDateInput.hint = "Due date and time"
        dueDateInput.keyListener = null
        dueDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(this,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(this,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)

                        dueDateInput.setText(SimpleDateFormat("HH:mm dd/MM/yyyy").format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        layout.addView(titleInput)
        layout.addView(descriptionInput)
        layout.addView(dueDateInput)


        builder.setView(layout)


        builder.setPositiveButton("Ok") { dialog, _ ->
            val title = titleInput.text.toString()
            val description = descriptionInput.text.toString()
            val dueDate = dueDateInput.text.toString()

            if (title.isEmpty())
            {
                titleInput.setText("Enter task title")
            }
            else if (dueDate.isEmpty())
            {
                dueDateInput.setText("Select due date and time")
            }
            else if (title.isNotEmpty() && dueDate.isNotEmpty())
            {
                setTask(title, description, dueDate)
                Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }


        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }



    private fun setTask(title: String, description: String, dueDate: String) {
        db.insertTask(title, description, true, dueDate)

        tasks.add(Task(title, description, true, dueDate))

        tasks.sortBy { it.dueDate }
        taskAdapter.notifyDataSetChanged()
    }



    private fun handleTaskAction(task: Task, taskAction: TaskAction) {

        when (taskAction)
        {
            TaskAction.CHANGE_TITLE ->
            {
                val titleEditBuilder = AlertDialog.Builder(this)
                titleEditBuilder.setTitle("Edit title")

                val titleInput = EditText(this)
                titleInput.hint = "Enter new title"
                titleInput.filters = arrayOf(filter)

                val description = TextView(this)
                description.text = task.description

                val pendingStatus = TextView(this)
                pendingStatus.text = if (task.pendingStatus) "Status : Active" else "Status : Completed"

                val dueDate = TextView(this)
                dueDate.text = "Due : " + task.dueDate


                val layout = LinearLayout(this)
                layout.orientation = LinearLayout.VERTICAL
                layout.setPadding(50, 40, 50, 10)
                layout.addView(titleInput)
                layout.addView(dueDate)
                layout.addView(pendingStatus)
                layout.addView(description)

                titleEditBuilder.setView(layout)

                titleEditBuilder.setPositiveButton("Ok") { dialog, _ ->
                    val newTitle = titleInput.text.toString()

                    if (newTitle.isEmpty())
                    {
                        return@setPositiveButton
                    }
                    else if (newTitle.isNotEmpty())
                    {
                        db.updateTitle(newTitle, task)
                        task.title = newTitle
                        taskAdapter.notifyDataSetChanged()

                        Toast.makeText(this, "Title updated", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }

                titleEditBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

                titleEditBuilder.show()
            }


            TaskAction.CHANGE_DESCRIPTION ->
            {
                val descriptionEditBuilder = AlertDialog.Builder(this)
                descriptionEditBuilder.setTitle("Edit description")

                val title = TextView(this)
                title.text = task.title

                val descriptionInput = EditText(this)
                descriptionInput.hint = "Enter new description"

                val pendingStatus = TextView(this)
                pendingStatus.text = if (task.pendingStatus) "Status : Active" else "Status : Completed"

                val dueDate = TextView(this)
                dueDate.text = "Due : " + task.dueDate


                val layout = LinearLayout(this)
                layout.orientation = LinearLayout.VERTICAL
                layout.setPadding(50, 40, 50, 10)
                layout.addView(title)
                layout.addView(dueDate)
                layout.addView(pendingStatus)
                layout.addView(descriptionInput)

                descriptionEditBuilder.setView(layout)

                descriptionEditBuilder.setPositiveButton("Ok") { dialog, _ ->
                    var newDescription = descriptionInput.text.toString()

                    if (newDescription.isEmpty())
                    {
                        newDescription = ""
                        db.updateDescription(newDescription, task)
                        task.description = newDescription
                        taskAdapter.notifyDataSetChanged()
                        Toast.makeText(this, "Description updated", Toast.LENGTH_SHORT).show()
                    }
                    else if (newDescription.isNotEmpty())
                    {
                        db.updateDescription(newDescription, task)
                        task.description = newDescription
                        taskAdapter.notifyDataSetChanged()
                        Toast.makeText(this, "Description updated", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }

                descriptionEditBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

                descriptionEditBuilder.show()
            }


            TaskAction.TOGGLE ->
            {
                db.toggle(task)
                task.pendingStatus = !task.pendingStatus
                if (!task.pendingStatus)
                {
                    Toast.makeText(this, "Task completed", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(this, "Task is yet to be completed", Toast.LENGTH_SHORT).show()
                }
            }


            TaskAction.DELETE ->
            {
                tasks.remove(task)
                db.deleteTask(task)
                taskAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}