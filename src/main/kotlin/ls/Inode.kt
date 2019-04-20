package ls

data class Inode(val type: InodeType, val lastEditedTime: Long, val size: Long)