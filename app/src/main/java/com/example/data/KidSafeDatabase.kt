package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ChildProfile::class, ActivityLog::class, TimeRequest::class], version = 1, exportSchema = false)
abstract class KidSafeDatabase : RoomDatabase() {
    abstract fun kidSafeDao(): KidSafeDao

    companion object {
        @Volatile
        private var INSTANCE: KidSafeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KidSafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KidSafeDatabase::class.java,
                    "kidsafe_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.kidSafeDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: KidSafeDao) {
                // Populate default Child profiles
                val arifId = dao.insertProfile(
                    ChildProfile(
                        name = "Arif",
                        age = 10,
                        screenTimeLimitMinutes = 60,
                        currentUsedMinutes = 24,
                        parentPin = "2580",
                        appBlockList = "TikTok,Instagram,FreeFire"
                    )
                )
                val tasfiaId = dao.insertProfile(
                    ChildProfile(
                        name = "Tasfia",
                        age = 13,
                        screenTimeLimitMinutes = 120,
                        currentUsedMinutes = 75,
                        parentPin = "2580",
                        appBlockList = "Snapchat,TikTok"
                    )
                )

                // Populate some sample logs for Arif
                dao.insertLog(ActivityLog(childId = arifId, appName = "Free Fire", type = "Gaming", durationMinutes = 15, description = "Attempted launch of blocked application", isFlagged = true))
                dao.insertLog(ActivityLog(childId = arifId, appName = "YouTube Kids", type = "Education", durationMinutes = 20, description = "Watched educational math cartoon", isFlagged = false))
                dao.insertLog(ActivityLog(childId = arifId, appName = "Google Chrome", type = "Web", durationMinutes = 4, description = "Searched for 'cool science experiments'", isFlagged = false))

                // Populate some sample logs for Tasfia
                dao.insertLog(ActivityLog(childId = tasfiaId, appName = "TikTok", type = "Flagged", durationMinutes = 0, description = "Launch of blocked app TikTok blocked dynamically", isFlagged = true))
                dao.insertLog(ActivityLog(childId = tasfiaId, appName = "Wikipedia", type = "Education", durationMinutes = 35, description = "Read history article on Bangladesh", isFlagged = false))
                dao.insertLog(ActivityLog(childId = tasfiaId, appName = "Facebook", type = "Social", durationMinutes = 40, description = "Browsed family social updates", isFlagged = false))

                // Populate sample screen time requests
                dao.insertRequest(TimeRequest(childId = arifId, requestedExtraMinutes = 15, reason = "To finish my math puzzle game!", status = "Pending"))
                dao.insertRequest(TimeRequest(childId = tasfiaId, requestedExtraMinutes = 30, reason = "Group research with classmates", status = "Pending"))
            }
        }
    }
}
