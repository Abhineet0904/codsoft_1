package com.example.todolistapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("UseSwitchCompatOrMaterialCode")
class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskAction: (Task, TaskAction) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle = itemView.findViewById<TextView>(R.id.taskTitle)
        val taskDescription = itemView.findViewById<TextView>(R.id.taskDescription)
        val taskPendingStatus = itemView.findViewById<Switch>(R.id.taskPendingStatus)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.deleteButton)
        val taskDueDate = itemView.findViewById<TextView>(R.id.taskDueDate)


        fun bind(task: Task) {
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskPendingStatus.isChecked = task.pendingStatus
            taskDueDate.text = task.dueDate

            taskTitle.setOnClickListener {
                onTaskAction(task, TaskAction.CHANGE_TITLE)
            }

            taskDescription.setOnClickListener {
                onTaskAction(task, TaskAction.CHANGE_DESCRIPTION)
            }

            taskPendingStatus.setOnCheckedChangeListener { _, _ ->
                onTaskAction(task, TaskAction.TOGGLE)
            }

            deleteButton.setOnClickListener {
                onTaskAction(task, TaskAction.DELETE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }
}