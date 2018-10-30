package io.github.t3r1jj.fcms.external

import com.github.eliux.mega.MegaSession
import com.github.eliux.mega.MegaUtils
import com.github.eliux.mega.auth.MegaAuthCredentials
import com.github.eliux.mega.cmd.AbstractMegaCmdCaller
import com.github.eliux.mega.cmd.AbstractMegaCmdPathHandler
import com.github.eliux.mega.cmd.FileInfo
import com.github.eliux.mega.cmd.MegaCmdList
import com.github.eliux.mega.error.*
import net.bytebuddy.ByteBuddy
import net.bytebuddy.agent.ByteBuddyAgent
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.nio.file.Paths
import java.util.*

class Mega(private val userName: String, private val password: String) : Storage {
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
            MegaAuthCredentials(userName, password).login()
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

    override fun upload(record: Record) {
        val file = stream2file(record.data)
        session!!.uploadFile(file.absolutePath, record.path)
                .createRemoteIfNotPresent<AbstractMegaCmdPathHandler>()
                .run()
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

    class MegaCmdRecursiveList(private val remotePath: String) : MegaCmdList("-r $remotePath") {
        fun recursiveCall(): MutableList<RecordMeta> {
            try {
                val meta = ArrayList<RecordMeta>()
                val results = MegaUtils.execCmdWithOutput(executableCommand())
                var relativePath = remotePath
                for (i in 1..results.lastIndex) {
                    when (fileInfoType(results[i])) {
                        FileInfoType.PATH -> relativePath = concatPath(results[i].substringBefore(":"), "")
                        FileInfoType.FILE -> {
                            val fileInfo = FileInfo.parseInfo(results[i])
                            if (fileInfo.isFile) {
                                val recordMeta = RecordMeta(fileInfo.name, concatPath(relativePath, fileInfo.name), fileInfo.size.orElse(0))
                                meta.add(recordMeta)
                            }
                        }
                        else -> {
                        }
                    }
                }
                return meta
            } catch (e: IOException) {
                throw MegaIOException("Error while listing $remotePath")
            }
        }

        private fun fileInfoType(fileInfoStr: String): FileInfoType {
            val tokens = trimSplit(fileInfoStr)
            return when (tokens.size) {
                1 -> FileInfoType.PATH
                6 -> FileInfoType.FILE
                else -> FileInfoType.INVALID
            }
        }

        private fun trimSplit(fileInfoStr: String): Array<out String> {
            return java.lang.String(java.lang.String(fileInfoStr)
                    .replace("\\t", "\\s"))
                    .split("\\s+")
        }

        enum class FileInfoType {
            FILE, PATH, INVALID
        }

        private fun concatPath(filePath: String, name: String) =
                if (filePath.endsWith('/')) (filePath + name) else ("$filePath/$name")
    }

    override fun delete(filePath: String) {
        session!!.remove(filePath).run()
    }

    override fun isPresent(filePath: String): Boolean {
        return session!!.exists(filePath)
    }

    override fun getInfo(): StorageInfo {
        session!!
        return MegaCmdDu().call()
    }

    class MegaCmdDu : AbstractMegaCmdCaller<StorageInfo>() {
        override fun getCmd(): String {
            return "du"
        }

        override fun call(): StorageInfo {
            try {
                val result = MegaUtils.execCmdWithOutput(executableCommand()).stream().skip(1)
                        .findFirst().orElse("/: 0")
                val results = trimSplit(result)
                return StorageInfo("Mega", BigInteger.valueOf(16106130000), results[1].toBigInteger()) //no endpoint for total storage space, using default 15GB quota
            } catch (e: IOException) {
                throw MegaIOException("Error while executing du")
            }

        }

        private fun trimSplit(fileInfoStr: String): Array<out String> {
            return java.lang.String(java.lang.String(fileInfoStr)
                    .replace("\\t", "\\s"))
                    .split("\\s+")
        }
    }


    override fun logout() {
        session?.logout()
    }
}