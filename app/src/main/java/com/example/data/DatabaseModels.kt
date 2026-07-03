package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "DIRECTOR" or "CREW"
    val name: String,
    val title: String, // e.g. "Cameraman", "Actor", "Ward Boy", "Director"
    val experienceYears: Int,
    val bio: String,
    val sampleWorkUrl: String,
    val email: String,
    val phone: String
)

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val directorId: Long,
    val directorName: String,
    val title: String,
    val roleType: String, // "Ward Boy", "Actor", "Cameraman", "Other"
    val productionName: String,
    val salaryRange: String,
    val description: String,
    val experienceRequired: String,
    val location: String,
    val postedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "applications")
data class JobApplication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val crewId: Long,
    val crewName: String,
    val crewRole: String,
    val experience: String,
    val sampleWork: String,
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "DECLINED"
    val timestamp: Long = System.currentTimeMillis()
)
