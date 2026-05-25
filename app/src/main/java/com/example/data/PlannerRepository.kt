package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class PlannerRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val eventDao = db.eventDao()
    private val travelDao = db.travelSegmentDao()
    private val accommodationDao = db.accommodationDao()
    private val taskDao = db.taskDao()
    private val vendorDao = db.vendorDao()
    private val guestDao = db.guestDao()

    // --- User Profile / Session Simulated State ---
    suspend fun registerUser(user: UserEntity): Long {
        // Simple duplicate email check
        val existing = userDao.getByEmail(user.email)
        if (existing != null) {
            throw IllegalArgumentException("User with this email already exists")
        }
        return userDao.insert(user)
    }

    suspend fun loginUser(email: String, passwordHash: String): UserEntity? {
        val user = userDao.getByEmail(email) ?: return null
        return if (user.passwordHash == passwordHash) user else null
    }

    fun getUserById(id: Int): Flow<UserEntity?> {
        return userDao.getById(id)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    // --- Events ---
    fun getEventsForUser(userId: Int): Flow<List<EventEntity>> {
        return eventDao.getEventsForUser(userId)
    }

    fun getEventById(id: Int): Flow<EventEntity?> {
        return eventDao.getEventById(id)
    }

    suspend fun insertEvent(event: EventEntity): Long {
        return eventDao.insert(event)
    }

    suspend fun updateEvent(event: EventEntity) {
        eventDao.update(event)
    }

    suspend fun deleteEvent(event: EventEntity) {
        eventDao.delete(event)
    }

    suspend fun deleteEventById(id: Int) {
        eventDao.deleteById(id)
    }

    // Duplication Business Logic
    suspend fun duplicateEvent(eventId: Int): Long {
        val sourceEvent = eventDao.getEventByIdOneShot(eventId) ?: return -1

        // 1. Create duplication copy
        val duplicatedEvent = sourceEvent.copy(
            id = 0,
            name = "Copy of ${sourceEvent.name}",
            isDraft = true // default duplicated to Draft so they can make changes
        )
        val newEventId = eventDao.insert(duplicatedEvent).toInt()

        if (newEventId > 0) {
            // 2. Fetch all child items of the original event and copy them
            val segments = travelDao.getSegmentsForEvent(eventId).first()
            val accommodations = accommodationDao.getAccommodationsForEvent(eventId).first()
            val tasks = taskDao.getTasksForEvent(eventId).first()
            val vendors = vendorDao.getVendorsForEvent(eventId).first()
            val guests = guestDao.getGuestsForEvent(eventId).first()

            segments.forEach {
                travelDao.insert(it.copy(id = 0, eventId = newEventId))
            }
            accommodations.forEach {
                accommodationDao.insert(it.copy(id = 0, eventId = newEventId))
            }
            tasks.forEach {
                taskDao.insert(it.copy(id = 0, eventId = newEventId))
            }
            vendors.forEach {
                vendorDao.insert(it.copy(id = 0, eventId = newEventId))
            }
            guests.forEach {
                guestDao.insert(it.copy(id = 0, eventId = newEventId))
            }
        }
        return newEventId.toLong()
    }

    // --- Travel Segments ---
    fun getSegmentsForEvent(eventId: Int): Flow<List<TravelSegmentEntity>> {
        return travelDao.getSegmentsForEvent(eventId)
    }

    suspend fun insertTravelSegment(segment: TravelSegmentEntity): Long {
        return travelDao.insert(segment)
    }

    suspend fun updateTravelSegment(segment: TravelSegmentEntity) {
        travelDao.update(segment)
    }

    suspend fun deleteTravelSegment(segment: TravelSegmentEntity) {
        travelDao.delete(segment)
    }

    // --- Accommodations ---
    fun getAccommodationsForEvent(eventId: Int): Flow<List<AccommodationEntity>> {
        return accommodationDao.getAccommodationsForEvent(eventId)
    }

    suspend fun insertAccommodation(accommodation: AccommodationEntity): Long {
        return accommodationDao.insert(accommodation)
    }

    suspend fun updateAccommodation(accommodation: AccommodationEntity) {
        accommodationDao.update(accommodation)
    }

    suspend fun deleteAccommodation(accommodation: AccommodationEntity) {
        accommodationDao.delete(accommodation)
    }

    // --- Tasks ---
    fun getTasksForEvent(eventId: Int): Flow<List<TaskEntity>> {
        return taskDao.getTasksForEvent(eventId)
    }

    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insert(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.update(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.delete(task)
    }

    // --- Vendors ---
    fun getVendorsForEvent(eventId: Int): Flow<List<VendorEntity>> {
        return vendorDao.getVendorsForEvent(eventId)
    }

    suspend fun insertVendor(vendor: VendorEntity): Long {
        return vendorDao.insert(vendor)
    }

    suspend fun updateVendor(vendor: VendorEntity) {
        vendorDao.update(vendor)
    }

    suspend fun deleteVendor(vendor: VendorEntity) {
        vendorDao.delete(vendor)
    }

    // --- Guests ---
    fun getGuestsForEvent(eventId: Int): Flow<List<GuestEntity>> {
        return guestDao.getGuestsForEvent(eventId)
    }

    suspend fun insertGuest(guest: GuestEntity): Long {
        return guestDao.insert(guest)
    }

    suspend fun updateGuest(guest: GuestEntity) {
        guestDao.update(guest)
    }

    suspend fun deleteGuest(guest: GuestEntity) {
        guestDao.delete(guest)
    }
}
