package io.github.t3r1jj.fcms.external

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.GetMetadataErrorException
import com.dropbox.core.v2.users.FullAccount
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger


class Dropbox(private val accessToken: String) : Storage {
    companion object : Loggable {
        private val logger = logger()
    }

    private var client: DbxClientV2? = null
    private var account: FullAccount? = null

    override fun login() {
        val config = DbxRequestConfig.newBuilder("fcms").build()
        client = DbxClientV2(config, accessToken)
        account = client!!.users().currentAccount
    }

    override fun isLogged(): Boolean {
        return account?.name?.displayName?.isNotBlank() ?: false
    }

    override fun upload(record: Record) {
        client!!.files().uploadBuilder(record.path)
                .uploadAndFinish(record.data)
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

    override fun delete(filePath: String) {
        client!!.files().deleteV2(filePath)
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
        return StorageInfo("Dropbox",
                BigInteger.valueOf(spaceUsage.allocation.individualValue.allocated),
                BigInteger.valueOf(spaceUsage.used)
        )
    }

    override fun logout() {
        client = null
        account = null
    }

}