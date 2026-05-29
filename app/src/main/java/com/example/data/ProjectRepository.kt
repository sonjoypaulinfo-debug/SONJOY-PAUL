package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val userDao: UserDao,
    private val notificationDao: NotificationDao
) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    fun getProjectById(id: Int): Flow<Project?> = projectDao.getProjectById(id)

    suspend fun insert(project: Project): Long = projectDao.insertProject(project)

    suspend fun update(project: Project) = projectDao.updateProject(project)

    suspend fun delete(project: Project) = projectDao.deleteProject(project)

    suspend fun deleteById(id: Int) = projectDao.deleteProjectById(id)

    suspend fun seedProjects(projects: List<Project>) = projectDao.insertProjectsList(projects)

    // User Operations
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun insertUser(user: User) = userDao.insertUser(user)

    // Notification Operations
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()
    suspend fun insertNotification(notification: Notification): Long = notificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Int) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()
    suspend fun deleteNotification(id: Int) = notificationDao.deleteNotificationById(id)
    suspend fun clearAllNotifications() = notificationDao.clearAllNotifications()
}
