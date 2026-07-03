package com.example.data

import kotlinx.coroutines.flow.Flow

class FilmCrewRepository(db: AppDatabase) {
    private val userDao = db.userDao()
    private val jobDao = db.jobDao()
    private val applicationDao = db.applicationDao()

    // Users
    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)
    fun getAllCrews(): Flow<List<User>> = userDao.getAllCrews()
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    // Jobs
    fun getAllJobs(): Flow<List<Job>> = jobDao.getAllJobs()
    fun getJobsByDirector(directorId: Long): Flow<List<Job>> = jobDao.getJobsByDirector(directorId)
    fun getJobById(jobId: Long): Flow<Job?> = jobDao.getJobById(jobId)
    suspend fun insertJob(job: Job): Long = jobDao.insertJob(job)
    suspend fun deleteJobById(jobId: Long) = jobDao.deleteJobById(jobId)

    // Applications
    fun getApplicationsForJob(jobId: Long): Flow<List<JobApplication>> = applicationDao.getApplicationsForJob(jobId)
    fun getApplicationsForCrew(crewId: Long): Flow<List<JobApplication>> = applicationDao.getApplicationsForCrew(crewId)
    suspend fun insertApplication(application: JobApplication): Long = applicationDao.insertApplication(application)
    suspend fun updateApplicationStatus(id: Long, status: String) = applicationDao.updateApplicationStatus(id, status)
}
