package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String, // simulated password storage
    val name: String,
    val countryCity: String,
    val preferredLanguage: String,
    val timeZone: String,
    val plannerType: String // e.g., "Individual", "Professional Planner", "Organization"
)

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val type: String, // Wedding, Funeral / Memorial, Birthday party, Corporate event, Social gathering, Custom event
    val startDate: String, // ISO String or Custom formatter
    val endDate: String,
    val locationCity: String,
    val locationCountry: String,
    val venueName: String,
    val address: String,
    val guestsCount: Int,
    val budgetMin: Double,
    val budgetMax: Double,
    val budgetCurrency: String,
    val notes: String,
    val isDraft: Boolean = false
)

@Entity(
    tableName = "travel_segments",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TravelSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val fromLocation: String,
    val toLocation: String,
    val travelDate: String,
    val mode: String, // Flight, Train, Bus, Car, Other
    val notes: String,
    val bookingReference: String = ""
)

@Entity(
    tableName = "accommodations",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AccommodationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val name: String,
    val checkInDate: String,
    val checkOutDate: String,
    val address: String,
    val bookingReference: String
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val title: String,
    val category: String, // Venue, Catering, Travel, Documents, Other etc.
    val dueDate: String,
    val status: String, // Not started, In progress, Done
    val assignedTo: String
)

@Entity(
    tableName = "vendors",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val name: String,
    val type: String, // Venue, Catering, Travel, Photography, Funeral home, Other etc.
    val country: String,
    val city: String,
    val phone: String,
    val email: String,
    val website: String,
    val notes: String,
    val linkedTaskId: Int? = null // Room supports optional fields nicely
)

@Entity(
    tableName = "guests",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val name: String,
    val email: String,
    val phone: String,
    val country: String,
    val rsvpStatus: String, // Invited, Confirmed, Declined, Maybe
    val notes: String
)
