package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FilmCrewRepository
import com.example.data.Job
import com.example.data.JobApplication
import com.example.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenState {
    object Onboarding : ScreenState()
    object Dashboard : ScreenState()
    object CreateProfile : ScreenState()
    object PostJob : ScreenState()
    object ApplicationsList : ScreenState()
    object MyProfile : ScreenState()
    data class JobDetails(val job: Job) : ScreenState()
    data class CrewDetails(val crew: User) : ScreenState()
}

class FilmCrewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FilmCrewRepository(AppDatabase.getDatabase(application))

    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Onboarding)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _activeUserId = MutableStateFlow<Long?>(null)
    val activeUserId: StateFlow<Long?> = _activeUserId.asStateFlow()

    val activeUser: StateFlow<User?> = _activeUserId.flatMapLatest { id ->
        if (id != null) {
            repository.getUserById(id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCrews: StateFlow<List<User>> = repository.getAllCrews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<Job>> = repository.getAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of applications submitted by active user (crew role)
    val myApplications: StateFlow<List<JobApplication>> = _activeUserId.flatMapLatest { id ->
        if (id != null) {
            repository.getApplicationsForCrew(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected job for application view
    private val _selectedJobForApps = MutableStateFlow<Long?>(null)
    val selectedJobForAppsApplications: StateFlow<List<JobApplication>> = _selectedJobForApps.flatMapLatest { jobId ->
        if (jobId != null) {
            repository.getApplicationsForJob(jobId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        prepopulateStarterDataIfNeeded()
    }

    private fun prepopulateStarterDataIfNeeded() {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                if (users.isEmpty()) {
                    // Prepopulate some default film crew looking for work
                    val crew1Id = repository.insertUser(User(
                        role = "CREW",
                        name = "Aarav Sharma",
                        title = "Cameraman",
                        experienceYears = 5,
                        bio = "Cinematographer with 5 years experience shooting indie shorts and commercial ads. Expert in RED, Arri Alexa, drone camera operations, and professional lighting setup. Passionate about narrative visual style.",
                        sampleWorkUrl = "https://vimeo.com/aarav_cinematic_showreel",
                        email = "aarav.dp@cinema.com",
                        phone = "+91 98765 43210"
                    ))

                    val crew2Id = repository.insertUser(User(
                        role = "CREW",
                        name = "Priya Sen",
                        title = "Lead Actress",
                        experienceYears = 3,
                        bio = "Professional actress with classical theater training. Played leading roles in 3 award-winning short films. Specializes in dramatic realism, emotional depth, and action sequences. Fluent in English and Hindi.",
                        sampleWorkUrl = "https://youtube.com/priya_sen_acting_monologue",
                        email = "priya.actor@cinema.com",
                        phone = "+91 87654 32109"
                    ))

                    val crew3Id = repository.insertUser(User(
                        role = "CREW",
                        name = "Raju Prasad",
                        title = "Ward Boy & Production Hand",
                        experienceYears = 7,
                        bio = "Hardworking on-set assistant with over 7 years of production support experience. Skilled in wardrobe management, costume upkeep, quick set changes, and equipment logistics. Known for dedication and long hours.",
                        sampleWorkUrl = "N/A - Hard work and recommendations are my sample work.",
                        email = "raju.prasad@cinema.com",
                        phone = "+91 76543 21098"
                    ))

                    val directorId = repository.insertUser(User(
                        role = "DIRECTOR",
                        name = "Sanjay Leela",
                        title = "Director & Producer",
                        experienceYears = 12,
                        bio = "Independent film director. Looking for talented, energetic crew members to build a dedicated film crew for an upcoming high-budget short film and commercial campaigns.",
                        sampleWorkUrl = "https://vimeo.com/sanjay_films_portfolio",
                        email = "sanjay.director@cinema.com",
                        phone = "+91 99988 77766"
                    ))

                    // Insert some starter jobs posted by Director
                    val job1Id = repository.insertJob(Job(
                        directorId = directorId,
                        directorName = "Sanjay Leela",
                        title = "Cameraman for Sci-Fi Short Film",
                        roleType = "Cameraman",
                        productionName = "Project: Parallel Dreams",
                        salaryRange = "$250 / Day",
                        description = "Looking for a cinematographer/DOP to shoot a 15-minute science fiction short film. Must have experience with anamorphic lenses and handheld high-contrast dark style shooting.",
                        experienceRequired = "3+ Years with camera reel",
                        location = "Mumbai, Studio 4"
                    ))

                    val job2Id = repository.insertJob(Job(
                        directorId = directorId,
                        directorName = "Sanjay Leela",
                        title = "Ward Boy / Dress Assistant Needed",
                        roleType = "Ward Boy",
                        productionName = "Project: Parallel Dreams",
                        salaryRange = "$100 / Day",
                        description = "Immediate requirement for a dependable ward boy/dress assistant to manage costumes for 5 lead characters. Responsibilities include ironing, laundry, dressing setup, and set maintenance.",
                        experienceRequired = "No experience needed, just hard working attitude",
                        location = "Mumbai, Studio 4"
                    ))

                    // Prepopulate an application
                    repository.insertApplication(JobApplication(
                        jobId = job1Id,
                        crewId = crew1Id,
                        crewName = "Aarav Sharma",
                        crewRole = "Cameraman",
                        experience = "Shot several dark mood short films, perfect fit for your high-contrast Sci-Fi project.",
                        sampleWork = "https://vimeo.com/aarav_cinematic_showreel",
                        status = "PENDING"
                    ))

                    // Automatically login as the first user (the Director) for demonstration
                    _activeUserId.value = directorId
                    _currentScreen.value = ScreenState.Dashboard
                } else {
                    // If DB is not empty, set active user to the first one available
                    if (_activeUserId.value == null && users.isNotEmpty()) {
                        _activeUserId.value = users.first().id
                        _currentScreen.value = ScreenState.Dashboard
                    }
                }
            }
        }
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun logout() {
        _activeUserId.value = null
        _currentScreen.value = ScreenState.Onboarding
    }

    fun loginAs(userId: Long) {
        _activeUserId.value = userId
        _currentScreen.value = ScreenState.Dashboard
    }

    fun selectJobForApplications(jobId: Long) {
        _selectedJobForApps.value = jobId
    }

    fun createProfile(
        role: String,
        name: String,
        title: String,
        experienceYears: Int,
        bio: String,
        sampleWorkUrl: String,
        email: String,
        phone: String
    ) {
        viewModelScope.launch {
            val newUser = User(
                role = role,
                name = name,
                title = title,
                experienceYears = experienceYears,
                bio = bio,
                sampleWorkUrl = sampleWorkUrl,
                email = email,
                phone = phone
            )
            val newId = repository.insertUser(newUser)
            _activeUserId.value = newId
            _currentScreen.value = ScreenState.Dashboard
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
            _currentScreen.value = ScreenState.Dashboard
        }
    }

    fun postJob(
        title: String,
        roleType: String,
        productionName: String,
        salaryRange: String,
        description: String,
        experienceRequired: String,
        location: String
    ) {
        val currentDir = activeUser.value ?: return
        viewModelScope.launch {
            val newJob = Job(
                directorId = currentDir.id,
                directorName = currentDir.name,
                title = title,
                roleType = roleType,
                productionName = productionName,
                salaryRange = salaryRange,
                description = description,
                experienceRequired = experienceRequired,
                location = location
            )
            repository.insertJob(newJob)
            _currentScreen.value = ScreenState.Dashboard
        }
    }

    fun deleteJob(jobId: Long) {
        viewModelScope.launch {
            repository.deleteJobById(jobId)
        }
    }

    fun applyForJob(
        jobId: Long,
        experience: String,
        sampleWork: String
    ) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            val application = JobApplication(
                jobId = jobId,
                crewId = user.id,
                crewName = user.name,
                crewRole = user.title,
                experience = experience,
                sampleWork = sampleWork,
                status = "PENDING"
            )
            repository.insertApplication(application)
            _currentScreen.value = ScreenState.Dashboard
        }
    }

    fun updateApplicationStatus(applicationId: Long, status: String) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, status)
        }
    }

    fun sendDirectInquiry(
        crewId: Long,
        crewName: String,
        crewRole: String,
        jobId: Long,
        message: String,
        directorName: String
    ) {
        viewModelScope.launch {
            val application = JobApplication(
                jobId = jobId,
                crewId = crewId,
                crewName = crewName,
                crewRole = crewRole,
                experience = message,
                sampleWork = "Direct Inquiry from Director: $directorName",
                status = "PENDING"
            )
            repository.insertApplication(application)
        }
    }
}
