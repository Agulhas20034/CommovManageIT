package com.example.commovmanageit.db.repositories
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.remote.dto.MediaRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.MediaDao
import com.example.commovmanageit.db.entities.Media
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class MediaRepository(
    private val MediaDao: MediaDao,
    private val connectivityMonitor: ConnectivityMonitor,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    init {
        connectivityMonitor.registerListener { online ->
            if (online) {
                coroutineScope.launch {
                    syncChanges()
                }
            }
        }
    }

    suspend fun insertLocal(Media: Media): Media {
        MediaDao.insert(Media)
        syncIfConnected()
        return Media
    }

    suspend fun updateLocal(Media: Media) {
        Media.updatedAt = Clock.System.now()
        MediaDao.update(Media)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String,type: String) {
        MediaDao.softDelete(id)
        if(type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Media? = MediaDao.getById(id)
    suspend fun getAllLocal(): List<Media> = MediaDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(Media: Media): String {
        val remoteMedia = Media.toRemote()
        val result = SupabaseManager.insertMedia<MediaRemote>(remoteMedia)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(Media: Media): MediaRemote {
        val remoteMedia = Media.toRemote()
        Log.d("MediaRepository", "Updating remote Media: ${Media.id}")
        return SupabaseManager.updateMedia<MediaRemote>(Media.serverId ?: Media.id, remoteMedia)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("media", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(Media: Media): Media {
        val newMedia = Media.copy(
            id = Media.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newMedia.id
                val syncedMedia = newMedia.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedMedia)
                insertLocal(syncedMedia)

                syncedMedia
            } catch (e: Exception) {
                MediaDao.insert(newMedia)
                newMedia
            }
        } else {
            MediaDao.insert(newMedia)
            newMedia
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(Media: Media) {
        val updatedMedia = Media.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedMedia.serverId != null) {
                    updateRemote(updatedMedia)
                    Log.d("MediaRepository", "Updated remote Media: ${updatedMedia.id}")
                    MediaDao.update(updatedMedia.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedMedia)
                    MediaDao.update(updatedMedia.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                MediaDao.update(updatedMedia)
            }
        } else {
            MediaDao.update(updatedMedia)
        }
    }

    suspend fun delete(id: String) {
        try {
            val Media = MediaDao.getById(id)
            Media?.let {
                MediaDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId)
                    MediaDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Media not found")
        } catch (e: Exception) {
            println("Delete operation partially failed: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncIfConnected() {
        if (connectivityMonitor.isConnected) {
            try {
                syncChanges()
            } catch (e: Exception) {
                Log.d("MediaRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        MediaDao.getUnsyncedCreatedOrUpdated().forEach { media ->
            try {
                if (media.serverId == null) {
                    val serverId = insertRemote(media)
                    MediaDao.markAsSynced(media.id, serverId)
                } else {
                    updateRemote(media)
                    MediaDao.updateSyncStatus(media.id, true)
                }
            } catch (e: Exception) {
                Log.d("MediaRepository", "Erro ao sincronizar cliente ${media.id}", e)
            }
        }

        MediaDao.getUnsyncedDeleted().forEach { media ->
            try {
                Log.d("MediaRepository", "Deleting remote Media: ${media.id}")
                media.serverId?.let { deleteRemote(it) }
                MediaDao.updateSyncStatus(media.id, true)
            } catch (e: Exception) {
            }
        }

        MediaDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<Media>> = MediaDao.observeAllActive()
    fun observeById(id: String): Flow<Media?> = MediaDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<Media>> = MediaDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<Media>> = MediaDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = MediaDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = MediaDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<Media> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remoteMedias: List<MediaRemote> = SupabaseManager.getAll<MediaRemote>("media")

            remoteMedias.map { remote ->
                Media(
                    id = remote.id,
                    serverId = remote.id,
                    name = remote.name,
                    createdAt = Instant.parse(remote.created_at),
                    updatedAt = Instant.parse(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Instant.parse(remote.deleted_at) },
                    isSynced = true,
                    projectId = remote.project_id,
                    reportId = remote.report_id,
                    type = remote.type,
                    path = remote.path
                )
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Failed to fetch remote Medias", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): MediaRemote? {
        return try {
            val remoteMedia = SupabaseManager.fetchById<MediaRemote>("media", id)

            remoteMedia?.let { Media ->
                MediaDao.update(Media.toLocal())
            }

            return remoteMedia
        } catch (e: Exception) {
            Log.e("MediaRepository", "Error fetching remote Media(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        MediaDao.deleteAll()
    }
}