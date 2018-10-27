package io.github.t3r1jj.fcms.external

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.users.FullAccount
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.io.File
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

    override fun upload(file: File): String {
        return client!!.files().uploadBuilder(file.name)
                .uploadAndFinish(file.inputStream()).pathLower
    }

    override fun download(filePath: String): File {
        val os = ByteOutputStream()
        val meta = client!!.files().downloadBuilder(filePath)
                .download(os)
        val file = File.createTempFile(System.currentTimeMillis().toString(), null)
        os.write(file.inputStream())
        return file
    }

    override fun delete(filePath: String) {
        client!!.files().deleteV2(filePath)
    }

    override fun isPresent(filePath: String): Boolean {
        return !client!!.files().getMetadata(filePath).name.isEmpty()
    }

    override fun getInfo(): StorageInfo {
        val spaceUsage = client!!.users().spaceUsage
        return StorageInfo("Dropbox", BigInteger.valueOf(spaceUsage.allocation.individualValue.allocated), BigInteger.valueOf(spaceUsage.used))
    }

    override fun logout() {
        client = null
        account = null
    }

}