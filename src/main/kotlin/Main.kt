import kotlinx.coroutines.*


sealed class TaskStatus {
    object Todo : TaskStatus()
    object InProgress : TaskStatus()
    object Completed : TaskStatus()
}


interface Describable {
    fun describe(): String
}


open class Task(
    val id: Int,
    val title: String,
    val priority: Int,
    val hours: Double
) : Describable {

    override fun describe(): String {
        return "[$id] $title | Priority: $priority | Hours: $hours"
    }
}


class WorkTask(
    id: Int,
    title: String,
    priority: Int,
    hours: Double,
    val company: String
) : Task(id, title, priority, hours) {

    override fun describe(): String {
        return "${super.describe()} | Work: $company"
    }
}

class PersonalTask(
    id: Int,
    title: String,
    priority: Int,
    hours: Double
) : Task(id, title, priority, hours) {

    override fun describe(): String {
        return "${super.describe()} | Personal"
    }
}


data class TaskInfo(
    val task: Task,
    var status: TaskStatus
)


class TaskManager {

    private val tasks = mutableListOf<TaskInfo>()

    private var nextId = 1

    fun addTask(task: Task, status: TaskStatus = TaskStatus.Todo) {
        tasks.add(TaskInfo(task, status))
        println("Task added successfully!")
    }

    fun showTasks() {

        if (tasks.isEmpty()) {
            println("\nThere are no tasks.")
            return
        }

        println("\n===== ALL TASKS =====")

        for (taskInfo in tasks) {

            println(
                "${taskInfo.task.describe()} | Status: ${
                    getStatusName(taskInfo.status)
                }"
            )
        }
    }

    fun completeTask(id: Int) {

        val taskInfo = tasks.find {
            it.task.id == id
        }

        if (taskInfo != null) {

            if (taskInfo.status == TaskStatus.Completed) {
                println("Task is already completed.")
            } else {
                taskInfo.status = TaskStatus.Completed
                println("Task completed!")
            }

        } else {
            println("Task not found.")
        }
    }

    fun showHighPriorityTasks() {

        val highPriorityTasks = tasks.filter {
            it.task.priority >= 4
        }

        println("\n===== HIGH PRIORITY TASKS =====")

        if (highPriorityTasks.isEmpty()) {
            println("No high priority tasks.")
        } else {

            highPriorityTasks.forEach {
                println(it.task.describe())
            }
        }
    }

    fun showTaskTitles() {

        val titles = tasks.map {
            it.task.title
        }

        println("\n===== TASK TITLES =====")

        titles.forEach {
            println("- $it")
        }
    }

    fun showTotalHours() {

        if (tasks.isEmpty()) {
            println("No tasks.")
            return
        }

        val totalHours = tasks
            .map { it.task.hours }
            .reduce { total, hours ->
                total + hours
            }

        println("Total estimated hours: $totalHours")
    }

    fun showPriorities() {

        val priorities: Set<Int> = tasks
            .map { it.task.priority }
            .toSet()

        println("Unique priorities: $priorities")
    }

    fun showTasksByStatus() {

        val tasksByStatus: Map<String, List<TaskInfo>> =
            tasks.groupBy {
                getStatusName(it.status)
            }

        println("\n===== TASKS BY STATUS =====")

        for ((status, taskList) in tasksByStatus) {
            println("$status: ${taskList.size} task(s)")
        }
    }

    fun processTasks(action: (TaskInfo) -> Unit) {

        for (task in tasks) {
            action(task)
        }
    }

    fun getNextId(): Int {
        return nextId++
    }

    private fun getStatusName(status: TaskStatus): String {

        return when (status) {
            TaskStatus.Todo -> "TODO"
            TaskStatus.InProgress -> "IN PROGRESS"
            TaskStatus.Completed -> "COMPLETED"
        }
    }
}


suspend fun loadTasks(manager: TaskManager) {

    println("Loading example tasks...")

    delay(1000)

    manager.addTask(
        WorkTask(
            manager.getNextId(),
            "Create Kotlin project",
            5,
            3.0,
            "University"
        ),
        TaskStatus.InProgress
    )

    manager.addTask(
        WorkTask(
            manager.getNextId(),
            "Write backend API",
            4,
            5.0,
            "Company"
        )
    )

    manager.addTask(
        PersonalTask(
            manager.getNextId(),
            "Go to the gym",
            2,
            1.5
        )
    )

    println("Example tasks loaded!\n")
}


fun createTask(manager: TaskManager) {

    println("\n===== ADD TASK =====")

    print("Task title: ")
    val title = readLine() ?: ""

    print("Priority (1-5): ")
    val priority = readLine()?.toIntOrNull() ?: 1

    print("Estimated hours: ")
    val hours = readLine()?.toDoubleOrNull() ?: 1.0

    println("1. Work task")
    println("2. Personal task")

    print("Task type: ")
    val type = readLine()?.toIntOrNull() ?: 2

    val id = manager.getNextId()

    if (type == 1) {

        print("Company name: ")
        val company = readLine() ?: "Unknown"

        val task = WorkTask(
            id,
            title,
            priority,
            hours,
            company
        )

        manager.addTask(task)

    } else {

        val task = PersonalTask(
            id,
            title,
            priority,
            hours
        )

        manager.addTask(task)
    }
}


fun showStatistics(manager: TaskManager) {

    println("\n===== STATISTICS =====")

    manager.showTotalHours()

    manager.showPriorities()

    manager.showTaskTitles()

    manager.showTasksByStatus()

    println("\nImportant tasks:")

    manager.processTasks { taskInfo ->

        if (taskInfo.task.priority >= 4) {
            println("- ${taskInfo.task.title}")
        }
    }
}


fun main() = runBlocking {

    val manager = TaskManager()

    launch {
        loadTasks(manager)
    }.join()

    var running = true

    while (running) {

        println("\n==========================")
        println("       TASK MANAGER")
        println("==========================")

        println("1. Add task")
        println("2. Show all tasks")
        println("3. Complete task")
        println("4. Show high priority tasks")
        println("5. Show statistics")
        println("0. Exit")

        print("\nChoose an option: ")

        val choice = readLine()?.toIntOrNull()

        when (choice) {

            1 -> {
                createTask(manager)
            }

            2 -> {
                manager.showTasks()
            }

            3 -> {

                print("Enter task ID: ")

                val id = readLine()?.toIntOrNull()

                if (id != null) {
                    manager.completeTask(id)
                } else {
                    println("Invalid ID.")
                }
            }

            4 -> {
                manager.showHighPriorityTasks()
            }

            5 -> {
                showStatistics(manager)
            }

            0 -> {
                running = false
                println("Goodbye!")
            }

            else -> {
                println("Invalid option. Try again.")
            }
        }
    }
}