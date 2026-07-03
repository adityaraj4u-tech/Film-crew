package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE role = 'CREW' ORDER BY id DESC")
    fun getAllCrews(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<User>>
}

@Dao
interface JobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job): Long

    @Query("SELECT * FROM jobs ORDER BY postedTimestamp DESC")
    fun getAllJobs(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE directorId = :directorId ORDER BY postedTimestamp DESC")
    fun getJobsByDirector(directorId: Long): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :jobId LIMIT 1")
    fun getJobById(jobId: Long): Flow<Job?>

    @Query("DELETE FROM jobs WHERE id = :jobId")
    suspend fun deleteJobById(jobId: Long)
}

@Dao
interface ApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplication): Long

    @Query("SELECT * FROM applications WHERE jobId = :jobId ORDER BY timestamp DESC")
    fun getApplicationsForJob(jobId: Long): Flow<List<JobApplication>>

    @Query("SELECT * FROM applications WHERE crewId = :crewId ORDER BY timestamp DESC")
    fun getApplicationsForCrew(crewId: Long): Flow<List<JobApplication>>

    @Query("UPDATE applications SET status = :status WHERE id = :id")
    suspend fun updateApplicationStatus(id: Long, status: String)
}
