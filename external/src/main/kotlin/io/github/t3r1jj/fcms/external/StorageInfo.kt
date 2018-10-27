package io.github.t3r1jj.fcms.external

import java.math.BigInteger

data class StorageInfo(val name: String,
                       val totalSpace: BigInteger,
                       val usedSpace: BigInteger)