package com.example.commovmanageit.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.commovmanageit.db.dao.CustomerDao
import com.example.commovmanageit.db.dao.LogsDao
import com.example.commovmanageit.db.dao.MediaDao
import com.example.commovmanageit.db.dao.PermissionDao
import com.example.commovmanageit.db.dao.ProjectDao
import com.example.commovmanageit.db.dao.ProjectUserDao
import com.example.commovmanageit.db.dao.ReportDao
import com.example.commovmanageit.db.dao.RoleDao
import com.example.commovmanageit.db.dao.TaskDao
import com.example.commovmanageit.db.dao.TaskUserDao
import com.example.commovmanageit.db.dao.UserDao
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.db.entities.Logs
import com.example.commovmanageit.db.entities.Media
import com.example.commovmanageit.db.entities.Permission
import com.example.commovmanageit.db.entities.Project
import com.example.commovmanageit.db.entities.ProjectUser
import com.example.commovmanageit.db.entities.Report
import com.example.commovmanageit.db.entities.Role
import com.example.commovmanageit.db.entities.Task
import com.example.commovmanageit.db.entities.TaskUser
import com.example.commovmanageit.db.entities.User
import kotlinx.datetime.Instant

@Database(
    entities = [
        Permission::class,
        Role::class,
        User::class,
        Customer::class,
        Project::class,
        ProjectUser::class,
        Task::class,
        TaskUser::class,
        Report::class,
        Media::class,
        Logs::class

    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun permissionDao(): PermissionDao
    abstract fun roleDao(): RoleDao
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectUserDao(): ProjectUserDao
    abstract fun taskDao(): TaskDao
    abstract fun taskUserDao(): TaskUserDao
    abstract fun reportDao(): ReportDao
    abstract fun mediaDao(): MediaDao
    abstract fun logsDao(): LogsDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}