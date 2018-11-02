package io.github.t3r1jj.fcms.external.authorized

import com.dropbox.core.BadRequestException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.GetMetadataErrorException
import com.dropbox.core.v2.users.FullAccount
import io.github.t3r1jj.fcms.external.*
import io.github.t3r1jj.fcms.external.data.Record
import io.github.t3r1jj.fcms.external.data.RecordMeta
import io.github.t3r1jj.fcms.external.data.StorageException
import io.github.t3r1jj.fcms.external.data.StorageInfo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger


class Dropbox(private val accessToken: String) : NamedStorage(), Storage {
    companion object : Loggable {
        private val logger = logger()
    }

    private var client: DbxClientV2? = null
    private var account: FullAccount? = null

    override fun login() {
        try {
            val config = DbxRequestConfig.newBuilder("fcms").build()
            client = DbxClientV2(config, accessToken)
            account = client!!.users().currentAccount
        } catch (e: BadRequestException) {
            throw StorageException("Exception during login", e)
        }
    }

    override fun isLogged(): Boolean {
        return account?.name?.displayName?.isNotBlank() ?: false
    }

    override fun upload(record: Record): RecordMeta {
        val result = client!!.files()
                .uploadBuilder(record.path)
                .uploadAndFinish(record.data)
        return RecordMeta(record.name, record.path, result.size)
                .apply { result.id }
    }

    override fun download(filePath: String): Record {
        val os = ByteArrayOutputStream()
        val meta = client!!.files().downloadBuilder(filePath)
                .download(os)
        return Record(meta.name, meta.pathLower, ByteArrayInputStream(os.toByteArray()))
    }

    override fun findAll(filePath: String): List<RecordMeta> {
        val meta = ArrayList<RecordMeta>()
        var result = client!!.files()
                .listFolderBuilder(filePath)
                .withRecursive(true)
                .start()
        do {
            for (metadata in result.entries) {
                if (metadata is FileMetadata) {
                    meta.add(RecordMeta(metadata.name, metadata.pathLower, metadata.size))
                }
            }
            result = client!!.files().listFolderContinue(result.cursor)
        } while (result.hasMore)
        return meta
    }

    override fun delete(meta: RecordMeta) {
        client!!.files().deleteV2(meta.path)
    }

    override fun isPresent(filePath: String): Boolean {
        return try {
            !client!!.files().getMetadata(filePath).name.isEmpty()
        } catch (ex: GetMetadataErrorException) {
            if (ex.errorValue.isPath) {
                logger.info("File {} not present in storage ", filePath, ex)
                false
            } else {
                throw ex
            }
        } catch (il: IllegalArgumentException) {
            logger.info("File {} not present in storage ", filePath, il)
            false
        }
    }

    override fun getInfo(): StorageInfo {
        val spaceUsage = client!!.users().spaceUsage
        return StorageInfo(this.toString(),
                BigInteger.valueOf(spaceUsage.allocation.individualValue.allocated),
                BigInteger.valueOf(spaceUsage.used)
        )
    }

    override fun logout() {
        client = null
        account = null
    }

}