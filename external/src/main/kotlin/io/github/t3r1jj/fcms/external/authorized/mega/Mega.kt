package io.github.t3r1jj.fcms.external.authorized.mega

import com.github.eliux.mega.MegaSession
import com.github.eliux.mega.MegaUtils
import com.github.eliux.mega.auth.MegaAuthCredentials
import com.github.eliux.mega.cmd.AbstractMegaCmdPathHandler
import com.github.eliux.mega.error.*
import io.github.t3r1jj.fcms.external.*
import io.github.t3r1jj.fcms.external.authorized.Storage
import io.github.t3r1jj.fcms.external.data.Record
import io.github.t3r1jj.fcms.external.data.RecordMeta
import io.github.t3r1jj.fcms.external.data.StorageException
import io.github.t3r1jj.fcms.external.data.StorageInfo
import net.bytebuddy.ByteBuddy
import net.bytebuddy.agent.ByteBuddyAgent
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Paths

class Mega(private val userName: String, private val password: String) : NamedStorage(), Storage {
    companion object {
        init {
            ByteBuddyAgent.install()
            ByteBuddy()
                    .redefine(MegaUtils::class.java)
                    .method(ElementMatchers.named("handleResult"))
                    .intercept(MethodDelegation.to(Mega::class.java))
                    .make()
                    .load(Mega::class.java.classLoader, ClassReloadingStrategy.fromInstalledAgent())
        }

        @Suppress("unused")
        @JvmStatic
        fun handleResult(code: Int) {
            val fixedCode = -code
            when (fixedCode) {
                0 -> {
                }
                -51 -> throw MegaWrongArgumentsException()
                -52 -> throw MegaInvalidEmailException()
                -53 -> throw MegaResourceNotFoundException()
                -54 -> throw MegaInvalidStateException()
                -55 -> throw MegaInvalidTypeException()
                -56 -> throw MegaOperationNotAllowedException()
                -57 -> throw MegaLoginRequiredException()
                -58 -> throw MegaNodesNotFetchedException()
                -59 -> throw MegaUnexpectedFailureException()
                -60 -> throw MegaConfirmationRequiredException()
                else -> throw MegaUnexpectedFailureException()
            }
        }
    }

    private var session: MegaSession? = null

    override fun login() {
        session = try {
            com.github.eliux.mega.Mega.currentSession()
        } catch (e: MegaException) {
            try {
                MegaAuthCredentials(userName, password).login()
            } catch (e: MegaException) {
                throw StorageException("Exception during login", e)
            }
        }
    }

    override fun isLogged(): Boolean {
        return try {
            com.github.eliux.mega.Mega.currentSession()
            true
        } catch (e: MegaException) {
            false
        }
    }

    override fun upload(record: Record): RecordMeta {
        val file = stream2file(record.data)
        session!!.uploadFile(file.absolutePath, record.path)
                .createRemoteIfNotPresent<AbstractMegaCmdPathHandler>()
                .run()
        return RecordMeta(record.name, record.path, file.length())
    }

    private fun stream2file(`in`: InputStream): java.io.File {
        val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
        tempFile.deleteOnExit()
        FileOutputStream(tempFile).use { out -> IOUtils.copy(`in`, out) }
        return tempFile
    }

    override fun download(filePath: String): Record {
        val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
        tempFile.delete()
        session!!.get(filePath)
                .setLocalPath(tempFile.absolutePath)
                .run()
        val path = Paths.get(filePath)
        return Record(path.fileName.toString(), filePath, FileInputStream(tempFile.absolutePath))
    }

    override fun findAll(filePath: String): List<RecordMeta> {
        session!!
        return try {
            MegaCmdRecursiveList(filePath).recursiveCall()
        } catch (notFound: MegaResourceNotFoundException) {
            emptyList()
        }
    }

    override fun delete(meta: RecordMeta) {
        session!!.remove(meta.path).run()
    }

    override fun isPresent(filePath: String): Boolean {
        return session!!.exists(filePath)
    }

    override fun getInfo(): StorageInfo {
        session!!
        return MegaCmdDu().call()
    }

    override fun logout() {
        session?.logout()
    }
}