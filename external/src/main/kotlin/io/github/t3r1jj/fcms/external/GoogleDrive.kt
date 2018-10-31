package io.github.t3r1jj.fcms.external

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigInteger

//TODO: path is stored as description while we operate on ids
class GoogleDrive(private val clientId: String,
                  private val clientSecret: String,
                  private val refreshToken: String) : AbstractStorage() {
    companion object {
        private const val FILE_FIELDS_DEFAULT_DESCRIPTION = "kind,incompleteSearch,files(kind,id,name,mimeType,description)"
        private const val FILE_FIELDS_DEFAULT_DESCRIPTION_SIZE = "kind,incompleteSearch,files(kind,id,name,mimeType,description,size)"
        private const val ABOUT_FIELDS_QUOTA = "storageQuota(limit,usage)"
    }

    private var credential: Credential? = null
    private var drive: Drive? = null

    override fun login() {
        val httpTransport = NetHttpTransport()
        val jsonFactory = JacksonFactory()
        credential = GoogleCredential.Builder()
                .setJsonFactory(jsonFactory)
                .setTransport(httpTransport)
                .setClientSecrets(clientId, clientSecret)
                .build()
        credential!!.refreshToken = refreshToken
        drive = Drive.Builder(
                httpTransport, jsonFactory, credential)
                .setApplicationName("mydriveapp")
                .build()
        try {
            drive?.files()?.list()
                    ?.setPageSize(1)
                    ?.execute()
        } catch (e: TokenResponseException) {
            throw StorageException("Exception during login", e)
        }
    }

    override fun isLogged(): Boolean {
        return credential != null && drive != null && credential!!.accessToken != null
    }

    override fun upload(record: Record) {
        val fileMeta = File()
        fileMeta.name = record.name
        fileMeta.description = record.path
        val fileContent = FileContent("application/octet-stream", stream2file(record.data))
        drive!!.files()
                .create(fileMeta, fileContent)
                .execute()
    }

    private fun stream2file(`in`: InputStream): java.io.File {
        val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
        tempFile.deleteOnExit()
        FileOutputStream(tempFile).use { out -> IOUtils.copy(`in`, out) }
        return tempFile
    }

    override fun download(filePath: String): Record {
        val fileMeta = drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION)
                .execute().files.last { it.description == filePath }
        val data = drive!!.files().get(fileMeta.id).executeMediaAsInputStream()
        return Record(fileMeta.name, filePath, data)
    }

    override fun findAll(filePath: String): List<RecordMeta> {
        return drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION_SIZE)
                .execute()
                .files
                .map {
                    RecordMeta(it.name,
                            if (it.description != null) it.description else "",
                            if (it.getSize() != null) it.getSize() else 0)
                }
    }

    override fun delete(filePath: String) {
        drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION)
                .execute().files
                .filter { it.description == filePath }
                .map { it.id }
                .forEach { drive!!.files().delete(it).execute() }
    }

    override fun isPresent(filePath: String): Boolean {
        return drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION)
                .execute().files
                .any { it.description == filePath }
    }

    override fun getInfo(): StorageInfo {
        val about = drive!!.about()
                .get()
                .setFields(ABOUT_FIELDS_QUOTA)
                .execute()
        return StorageInfo(this.toString(),
                BigInteger.valueOf(about.storageQuota.limit),
                BigInteger.valueOf(about.storageQuota.usage))
    }

    override fun logout() {
        credential = null
        drive = null
    }
}